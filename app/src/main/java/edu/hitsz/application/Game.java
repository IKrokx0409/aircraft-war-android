package edu.hitsz.application;

import android.content.Intent;  // ← 添加这行
import edu.hitsz.EndActivity;  // ← 添加这行
import edu.hitsz.manager.GameManager;  // ← 添加这行

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombListener;
import edu.hitsz.prop.BombProp;
import edu.hitsz.factory.BossFactory;
import edu.hitsz.factory.EliteEnemyFactory;
import edu.hitsz.factory.ElitePlusEnemyFactory;
import edu.hitsz.factory.EnemyFactory;
import edu.hitsz.factory.MobEnemyFactory;
import edu.hitsz.dao.GameRecord;
import edu.hitsz.dao.GameRecordDao;
import edu.hitsz.dao.GameRecordDaoImpl;

import java.util.Date;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 游戏主面板，游戏启动 (Android SurfaceView 重构版)
 */
public class Game extends SurfaceView implements SurfaceHolder.Callback, Runnable, BombListener {

    private String gameMode = "single";
    private boolean soundEnabled = true;
    private String difficulty = "normal"; // "easy" | "normal" | "hard"
    private MusicManager musicManager;

    public void setGameMode(String mode) { this.gameMode = mode; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getGameMode() { return gameMode; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public String getDifficulty() { return difficulty; }
    private int backGroundTop = 0;

    // 游戏管理器
    private GameManager gameManager;

    // 游戏界面回调接口
    private OnGameEndListener onGameEndListener;

    // 回调接口定义
    public interface OnGameEndListener {
        void onGameEnd(int score);
    }

    // 设置游戏管理器
    public void setGameManager(GameManager manager) {
        this.gameManager = manager;
    }

    // 设置游戏结束监听
    public void setOnGameEndListener(OnGameEndListener listener) {
        this.onGameEndListener = listener;
    }

    // Android 绘图与线程控制组件
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;
    private boolean mbLoop = false; // 控制绘制线程的标志位
    private int screenWidth;
    private int screenHeight;

    private final EnemyFactory mobEnemyFactory;
    private final EnemyFactory eliteEnemyFactory;
    private final EnemyFactory elitePlusEnemyFactory;
    private final BossFactory bossFactory;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 16;

    private final HeroAircraft heroAircraft;
    private final List<AbstractEnemyAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;

    private int enemyMaxNumber = 5;

    // Normal 基准概率（easy/normal 固定使用；hard 以此为起点动态增长）
    private static final double BASE_ELITE_PROB      = 0.30;
    private static final double BASE_ELITE_PLUS_PROB = 0.15;

    // Boss机相关状态
    private int bossScoreThreshold = 500; // easy忽略；normal=500；hard=300
    private int bossSpawnCount = 0;
    private boolean bossIsActive = false;

    private int score = 0;
    private int time = 0;
    private int cycleDuration = 600;
    private int heroShootCycleDuration = 160;
    private int cycleTime = 0;
    private int heroShootCycleTime = 0;
    private boolean gameOverFlag = false;

    /** 火力 buff 到期的系统时间戳（ms），-1 表示未激活 */
    private static final int FIRE_BUFF_DURATION = 6000;
    private long fireBuffEndTime = -1;

    // 在现有的成员变量中添加这些
    private Thread gameThread;  // ← 添加：保存游戏线程的引用
    private final Object threadLock = new Object();  // ← 添加：线程锁

    public Game(Context context, boolean isMusicOn) {
        super(context);
        this.musicManager = new MusicManager(context, isMusicOn);

        ImageManager.initImage(context);

        // 初始化 Android 绘图组件
        this.paint = new Paint();
        this.surfaceHolder = this.getHolder();
        this.surfaceHolder.addCallback(this);
        this.setFocusable(true);

        heroAircraft = HeroAircraft.getInstance();
        heroAircraft.reset(); //在构造时立刻重置
        this.mobEnemyFactory = new MobEnemyFactory();
        this.eliteEnemyFactory = new EliteEnemyFactory();
        this.elitePlusEnemyFactory = new ElitePlusEnemyFactory();
        this.bossFactory = new BossFactory();

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        // 注册触摸事件，替代原有的 HeroController
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    heroAircraft.setLocation(event.getX(), event.getY());
                }
                return true;
            }
        });
    }

    // ==========================================
    // SurfaceView 生命周期与线程控制
    // ==========================================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        musicManager.playBGM();
        // 在画布创建时初始化图片资源
        synchronized (threadLock){
            //如果旧进程还在运行，先等待它停止
            if (gameThread != null && gameThread.isAlive()){
                mbLoop = false;
                try{
                    gameThread.join(2000); //最多等待2秒
                    if (gameThread.isAlive()){
                        System.err.println("Warning: Game thread did not stop in time!");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        heroAircraft.reset(); //启动线程前，确保英雄机状态正确
        applyDifficultySettings();
        mbLoop = true;
        gameThread = new Thread(this);
        gameThread.start(); // 启动游戏主线程
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 动态获取手机屏幕的宽高，替代原有的 AbstractFlyingObject.WINDOW_HEIGHT
        this.screenWidth = width;
        this.screenHeight = height;

        edu.hitsz.basic.AbstractFlyingObject.WINDOW_WIDTH = width;
        edu.hitsz.basic.AbstractFlyingObject.WINDOW_HEIGHT = height;

        if (ImageManager.BACKGROUND_IMAGE != null) {
            // 使用方法 createScaledBitmap拉伸图片
            // 参数依次为：原图、目标宽度、目标高度、是否启用滤镜
            ImageManager.BACKGROUND_IMAGE = Bitmap.createScaledBitmap(
                    ImageManager.BACKGROUND_IMAGE, width, height, true);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mbLoop = false; // 终止游戏循环
        if (musicManager != null) {
            musicManager.releaseAll();
        }
    }

    @Override
    public void run() {
        // 游戏主循环
        while (mbLoop) {
            // 1. 逻辑计算
            action();

            // 2. 画面绘制 (双缓冲机制)
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas); // 提交画布
                }
            }

            // 3. 控制帧率
            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //线程结束时清理
        synchronized (threadLock) {
            gameThread = null;
        }
    }

    // ==========================================
    // 游戏核心逻辑 (剥离了定时器，纯逻辑计算)
    // ==========================================

    public void action() {
        time += timeInterval;

        // Hard 模式：敌机召唤/射击周期 和 英雄射击周期 随时间加速
        // cycleDuration：400ms → 最低 200ms（约 3.3 分钟达到下限）
        // heroShootCycleDuration：160ms → 最低 60ms（约 3.3 分钟达到下限）
        if ("hard".equals(difficulty)) {
            cycleDuration = Math.max(400 - time / 1000, 200);
            heroShootCycleDuration = Math.max(160 - time / 2000, 60);
        }

        if (timeCountAndNewCycleJudge()) {
            if (enemyAircrafts.size() < enemyMaxNumber) {
                double currentEliteProb;
                double currentElitePlusProb;
                if ("hard".equals(difficulty)) {
                    // Hard 模式：精英概率随时间线性增长，每分钟约增加 0.1/0.05，分别上限 0.65/0.45
                    currentEliteProb      = Math.min(BASE_ELITE_PROB      + time / 400000.0, 0.65);
                    currentElitePlusProb  = Math.min(BASE_ELITE_PLUS_PROB + time / 600000.0, 0.45);
                } else {
                    currentEliteProb      = BASE_ELITE_PROB;
                    currentElitePlusProb  = BASE_ELITE_PLUS_PROB;
                }
                double r = Math.random();
                if (r < currentElitePlusProb) {
                    enemyAircrafts.add(elitePlusEnemyFactory.createEnemy());
                } else if (r < currentEliteProb + currentElitePlusProb) {
                    enemyAircrafts.add(eliteEnemyFactory.createEnemy());
                } else {
                    enemyAircrafts.add(mobEnemyFactory.createEnemy());
                }
            }
            shootAction();
        }
        // 火力 buff 倒计时：到期时恢复直射
        if (fireBuffEndTime > 0 && System.currentTimeMillis() >= fireBuffEndTime) {
            fireBuffEndTime = -1;
            heroAircraft.setStrategy(new edu.hitsz.strategy.DirectShoot());
        }
        heroShootAction();
        bulletsMoveAction();
        aircraftsMoveAction();
        crashCheckAction();
        propsMoveAction();
        postProcessAction();

        // 游戏结束检查
        if (time > 100 && heroAircraft.getHp() <= 0) { //只在游戏运行一小段时间后才检查（避免刚启动就结束）
            mbLoop = false; // 停止主循环
            gameOverFlag = true;
            System.out.println("Game Over!");

            if (musicManager != null) {
                musicManager.playGameOverSound(); // 播放 Game Over 音效
                musicManager.pauseBGM();          // 【新增】暂停背景音乐，让环境安静下来
            }

            // 改为通过回调通知 MainActivity，而不是直接启动 EndActivity
            if (getContext() instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) getContext();
                activity.runOnUiThread(() -> {
                    // 调用回调接口，让 MainActivity 处理界面跳转
                    if (onGameEndListener != null) {
                        onGameEndListener.onGameEnd(score);
                    }
                });
            }

            // 保存本局记录到沙箱文件
            GameRecordDao dao = new GameRecordDaoImpl(getContext());
            GameRecord newRecord = new GameRecord("Player", this.score, new Date(), this.difficulty);
            dao.addRecord(newRecord);
            return; //游戏结束后停止处理
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    private void shootAction() {
        for (AbstractAircraft enemy : enemyAircrafts) {
            enemyBullets.addAll(enemy.shoot());
        }
    }

    private void heroShootAction() {
        heroShootCycleTime += timeInterval;
        if (heroShootCycleTime >= heroShootCycleDuration) {
            heroShootCycleTime %= heroShootCycleDuration;
            if(!gameOverFlag){
                heroBullets.addAll(heroAircraft.shoot());
                if (musicManager != null) {
                    musicManager.playShootSound();
                }
            }
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    private void crashCheckAction() {
        List<AbstractEnemyAircraft> newEnemies = new LinkedList<>();

        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
                if (musicManager != null) {
                    musicManager.playHitSound();
                }
            }
        }

        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) continue;
            for (AbstractEnemyAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) continue;
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        if (enemyAircraft instanceof ElitePlusEnemy) {
                            score += 30;
                            addPropsWithListener(enemyAircraft.dropProps());
                        } else if (enemyAircraft instanceof EliteEnemy) {
                            score += 20;
                            addPropsWithListener(enemyAircraft.dropProps());
                        } else if (enemyAircraft instanceof BossEnemy) {
                            score += 100;
                            addPropsWithListener(enemyAircraft.dropProps());
                            bossIsActive = false;

                            if (musicManager != null) {
                                musicManager.stopBossBGM();
                            }
                        } else {
                            score += 10;
                        }

                        if (!bossIsActive && !"easy".equals(difficulty)
                                && score / bossScoreThreshold > bossSpawnCount) {
                            // Hard 模式：每波 Boss 血量递增（1000, 1500, 2000...）
                            int bossHp = "hard".equals(difficulty)
                                    ? 1000 + bossSpawnCount * 500
                                    : 1000;
                            newEnemies.add(bossFactory.createEnemy(bossHp));
                            bossIsActive = true;
                            bossSpawnCount++;

                            if (musicManager != null) {
                                musicManager.playBossBGM();
                            }
                        }
                    }
                }
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop)) {
                prop.activate(heroAircraft);

                if (prop instanceof edu.hitsz.prop.BombProp) {
                    musicManager.playBombSound();
                } else {
                    musicManager.playGetSupplySound();
                    // 火力道具：启动/重置 6s 倒计时
                    if (prop instanceof edu.hitsz.prop.FireProp
                            || prop instanceof edu.hitsz.prop.FirePlusProp) {
                        fireBuffEndTime = System.currentTimeMillis() + FIRE_BUFF_DURATION;
                    }
                }
            }
        }
        enemyAircrafts.addAll(newEnemies);
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }

    // ==========================================
    // 画面绘制 (Android Canvas 适配)
    // ==========================================

    public void draw(Canvas canvas) {
        super.draw(canvas);

        // 绘制背景，动态获取屏幕高度避免硬编码
        if (ImageManager.BACKGROUND_IMAGE != null) {
            canvas.drawBitmap(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - screenHeight, paint);
            canvas.drawBitmap(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, paint);
            this.backGroundTop += 2; // Android刷新率高，步长可稍微调大
            if (this.backGroundTop >= screenHeight) {
                this.backGroundTop = 0;
            }
        }

        // 绘制各层对象
        paintImageWithPositionRevised(canvas, enemyBullets);
        paintImageWithPositionRevised(canvas, heroBullets);
        paintImageWithPositionRevised(canvas, props);
        paintImageWithPositionRevised(canvas, enemyAircrafts);

        // 绘制英雄机
        if (ImageManager.HERO_IMAGE != null) {
            canvas.drawBitmap(ImageManager.HERO_IMAGE,
                    heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                    heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, paint);
        }

        paintScoreAndLife(canvas);
    }

    private void paintImageWithPositionRevised(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) return;

        for (AbstractFlyingObject object : objects) {
            Bitmap image = object.getImage();
            if (image == null) continue;
            canvas.drawBitmap(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, paint);
        }
    }

    private void paintScoreAndLife(Canvas canvas) {
        int x = 30;
        int y = 80;
        paint.setFakeBoldText(true);
        paint.setTextSize(60);
        paint.setColor(Color.RED);
        canvas.drawText("SCORE:" + this.score, x, y, paint);
        y += 70;
        canvas.drawText("LIFE:" + this.heroAircraft.getHp(), x, y, paint);
        y += 70;
        paint.setTextSize(44);
        switch (difficulty) {
            case "easy":   paint.setColor(Color.GREEN);  canvas.drawText("[EASY]",   x, y, paint); break;
            case "hard":   paint.setColor(Color.RED);    canvas.drawText("[HARD]",   x, y, paint); break;
            default:       paint.setColor(Color.YELLOW); canvas.drawText("[NORMAL]", x, y, paint); break;
        }
        // 火力 buff 倒计时调试显示（确认功能后可删除）
        if (fireBuffEndTime > 0) {
            long remaining = fireBuffEndTime - System.currentTimeMillis();
            if (remaining > 0) {
                y += 60;
                paint.setTextSize(40);
                paint.setColor(Color.CYAN);
                canvas.drawText("FIRE:" + (remaining / 1000) + "s", x, y, paint);
            }
        }
    }

    /**
     * 重置游戏状态（用于重新开始游戏）必须在线程完全停止后调用
     */
    public void reset() {
        synchronized (threadLock) {
            // 重置计时器和标志
            score = 0;
            time = 0;
            cycleTime = 0;
            heroShootCycleTime = 0;
            gameOverFlag = false;

            // 清空所有列表
            enemyAircrafts.clear();
            heroBullets.clear();
            enemyBullets.clear();
            props.clear();

            // Boss 相关状态重置
            bossSpawnCount = 0;
            bossIsActive = false;
            fireBuffEndTime = -1;
            applyDifficultySettings();

            // 重置英雄飞机
            heroAircraft.reset();

            // 游戏管理器重置
            if (gameManager != null) {
                gameManager.reset();
            }

            if (musicManager != null) {
                // 1. 如果死的时候正好在打 Boss，先把 Boss 音乐停了（这个方法里自带了恢复普通 BGM 的逻辑）
                musicManager.stopBossBGM();
                // 2. 重新大声放起普通背景音乐！
                musicManager.playBGM();
            }

            System.out.println("Game Reset!");
        }
    }

    /**
     * 暂停游戏（停止游戏循环，暂停 BGM，不释放资源）
     */
    public void pause() {
        mbLoop = false;
        if (musicManager != null) {
            musicManager.pauseBGM();
        }
    }

    /**
     * 恢复游戏（重新启动游戏线程，恢复 BGM）
     */
    public void resume() {
        synchronized (threadLock) {
            if (!mbLoop && (gameThread == null || !gameThread.isAlive())) {
                mbLoop = true;
                gameThread = new Thread(this);
                gameThread.start();
                if (musicManager != null) {
                    musicManager.resumeBGM();
                }
            }
        }
    }

    /**
     * 清理游戏资源
     */
    public void cleanup() {
        synchronized (threadLock) {
            mbLoop = false;  // 停止游戏循环
            //等待线程完全停止
            if (gameThread != null && gameThread.isAlive()){
                try {
                    gameThread.join(3000); //最多等待3秒
                    if (gameThread.isAlive()) {
                        System.err.println("Warning: Game thread did not stop!");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // 清空所有列表
            enemyAircrafts.clear();
            heroBullets.clear();
            enemyBullets.clear();
            props.clear();

            // 游戏管理器清理
            if (gameManager != null) {
                gameManager.cleanup();
            }
            if (musicManager != null) {
                musicManager.releaseAll();
                musicManager = null; // 防止 surfaceDestroyed / onDestroy 重复释放
            }
        }

        System.out.println("Game Cleanup Complete!");
    }

    /**
     * 将新道具加入列表，并为 BombProp 注册爆炸监听（观察者注册点）
     */
    private void addPropsWithListener(List<AbstractProp> newProps) {
        for (AbstractProp prop : newProps) {
            if (prop instanceof BombProp) {
                ((BombProp) prop).setBombListener(this);
            }
        }
        props.addAll(newProps);
    }

    /**
     * 炸弹爆炸效果（BombListener 实现）：
     * - 普通/精英敌机：立即消灭，获得对应分数
     * - 超级精英敌机：扣除 50 点 HP；若因此死亡同样得分
     * - Boss 敌机：不受影响
     * - 所有敌机子弹：清除
     */
    @Override
    public void onBombExplode() {
        // 清除所有敌机子弹
        for (BaseBullet bullet : enemyBullets) {
            bullet.vanish();
        }

        // 处理各类敌机
        for (AbstractEnemyAircraft enemy : enemyAircrafts) {
            if (enemy.notValid()) continue;

            if (enemy instanceof BossEnemy) {
                // Boss 不受炸弹影响
                continue;
            } else if (enemy instanceof ElitePlusEnemy) {
                // 超级精英：扣 50 HP
                enemy.decreaseHp(50);
                if (enemy.notValid()) {
                    score += 30;
                }
            } else if (enemy instanceof EliteEnemy) {
                enemy.vanish();
                score += 20;
            } else {
                // MobEnemy
                enemy.vanish();
                score += 10;
            }
        }

        if (musicManager != null) {
            musicManager.playBombSound();
        }
    }

    private void setEnemySpeedMultiplier(double multiplier) {
        mobEnemyFactory.setSpeedMultiplier(multiplier);
        eliteEnemyFactory.setSpeedMultiplier(multiplier);
        elitePlusEnemyFactory.setSpeedMultiplier(multiplier);
    }

    /**
     * 根据当前难度初始化静态参数：Boss 阈值、最大敌机数、背景图。
     * 动态概率/发射率在 action() 中实时计算。
     */
    private void applyDifficultySettings() {
        switch (difficulty) {
            case "easy":
                bossScoreThreshold = 500;   // easy 不会触发 Boss，占位
                enemyMaxNumber = 7;
                cycleDuration = 700;        // 敌机召唤/射击较慢
                heroShootCycleDuration = 160;
                setEnemySpeedMultiplier(1.0);
                break;
            case "hard":
                bossScoreThreshold = 300;
                enemyMaxNumber = 4;
                cycleDuration = 400;        // 初始较快，action() 中动态递减至 200ms
                heroShootCycleDuration = 160;
                setEnemySpeedMultiplier(1.6);
                break;
            default: // normal
                bossScoreThreshold = 500;
                enemyMaxNumber = 5;
                cycleDuration = 450;        // 比原来 600ms 明显加快，固定不变
                heroShootCycleDuration = 160;
                setEnemySpeedMultiplier(1.3);
                break;
        }

        // 加载对应难度背景图
        ImageManager.loadBackground(getContext(), difficulty);

        // 若屏幕已知尺寸（reset 时），立刻缩放背景
        if (screenWidth > 0 && screenHeight > 0 && ImageManager.BACKGROUND_IMAGE != null) {
            ImageManager.BACKGROUND_IMAGE = Bitmap.createScaledBitmap(
                    ImageManager.BACKGROUND_IMAGE, screenWidth, screenHeight, true);
        }
    }
}