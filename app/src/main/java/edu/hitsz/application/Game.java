package edu.hitsz.application;

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
import edu.hitsz.factory.EliteEnemyFactory;
import edu.hitsz.factory.EnemyFactory;
import edu.hitsz.factory.MobEnemyFactory;
import edu.hitsz.factory.ElitePlusEnemyFactory;
import edu.hitsz.factory.BossFactory;
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
public class Game extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private int backGroundTop = 0;

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
    private final EnemyFactory bossFactory;

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
    private double eliteProb = 0.3;
    private double elitePlusProb = 0.15;

    // Boss机相关状态
    private final int bossScoreThreshold = 500;
    private int bossSpawnCount = 0;
    private boolean bossIsActive = false;

    private int score = 0;
    private int time = 0;
    private int cycleDuration = 600;
    private int heroShootCycleDuration = 160; //改成每10帧发射一次
    private int cycleTime = 0;
    private int heroShootCycleTime = 0;
    private boolean gameOverFlag = false;

    public Game(Context context) {
        super(context);

        ImageManager.initImage(context);

        // 初始化 Android 绘图组件
        this.paint = new Paint();
        this.surfaceHolder = this.getHolder();
        this.surfaceHolder.addCallback(this);
        this.setFocusable(true);

        heroAircraft = HeroAircraft.getInstance();
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
        // 在画布创建时初始化图片资源
        // ImageManager.initImage(getContext());
        mbLoop = true;
        new Thread(this).start(); // 启动游戏主线程
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 动态获取手机屏幕的宽高，替代原有的 AbstractFlyingObject.WINDOW_HEIGHT
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mbLoop = false; // 终止游戏循环
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
    }

    // ==========================================
    // 游戏核心逻辑 (剥离了定时器，纯逻辑计算)
    // ==========================================

    public void action() {
        time += timeInterval;
        if (timeCountAndNewCycleJudge()) {
            if (enemyAircrafts.size() < enemyMaxNumber) {
                double r = Math.random();
                if (r < elitePlusProb) {
                    enemyAircrafts.add(elitePlusEnemyFactory.createEnemy());
                } else if (r < eliteProb + elitePlusProb) {
                    enemyAircrafts.add(eliteEnemyFactory.createEnemy());
                } else {
                    enemyAircrafts.add(mobEnemyFactory.createEnemy());
                }
            }
            shootAction();
        }
        heroShootAction();
        bulletsMoveAction();
        aircraftsMoveAction();
        crashCheckAction();
        propsMoveAction();
        postProcessAction();

        // 游戏结束检查
        if (heroAircraft.getHp() <= 0) {
            mbLoop = false; // 停止主循环
            gameOverFlag = true;
            System.out.println("Game Over!");

            // 你的排行榜逻辑 (注意：Android中写文件需要修改DAO实现，这里先保留逻辑)
            GameRecordDao dao = new GameRecordDaoImpl();
            String playerName = "Player";
            GameRecord newRecord = new GameRecord(playerName, this.score, new Date());
            dao.addRecord(newRecord);

            List<GameRecord> records = dao.getAllRecords();
            records.sort(Comparator.comparingInt(GameRecord::getScore).reversed());

            System.out.println("          ---RANKING LIST---         ");
            int rank = 1;
            for (GameRecord record : records) {
                System.out.printf("rank%2d: %s, %6d, %s\n", rank++, record.getPlayerName(), record.getScore(), record.getFormattedTimestamp());
            }
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
                            props.addAll(enemyAircraft.dropProps());
                        } else if (enemyAircraft instanceof EliteEnemy) {
                            score += 20;
                            props.addAll(enemyAircraft.dropProps());
                        } else if (enemyAircraft instanceof BossEnemy) {
                            score += 100;
                            props.addAll(enemyAircraft.dropProps());
                            bossIsActive = false;
                        } else {
                            score += 10;
                        }

                        if (!bossIsActive && score / bossScoreThreshold > bossSpawnCount) {
                            newEnemies.add(bossFactory.createEnemy());
                            bossIsActive = true;
                            bossSpawnCount++;
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
        paint.setColor(Color.RED);
        paint.setTextSize(60); // Android 字体大小用 px，数值需要调大
        paint.setFakeBoldText(true);
        canvas.drawText("SCORE:" + this.score, x, y, paint);
        y = y + 70;
        canvas.drawText("LIFE:" + this.heroAircraft.getHp(), x, y, paint);
    }
}