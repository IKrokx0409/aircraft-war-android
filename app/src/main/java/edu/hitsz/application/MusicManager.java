package edu.hitsz.application;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import edu.hitsz.R;

public class MusicManager {
    private Context context;
    private boolean isMusicOn; // 音乐开关 [cite: 146]

    // 留声机 (长音频)
    private MediaPlayer bgmPlayer;
    private MediaPlayer bossBgmPlayer;
    private boolean bossIsPlaying = false; // 记录当前播放的是哪首 BGM

    // 锤子 (短音效)
    private SoundPool soundPool;
    private int bulletSoundId;  // 名字改成 bullet
    private int hitSoundId;
    private int bombSoundId;
    private int gameOverSoundId; // 【新增】游戏结束
    private int getSupplySoundId;

    public MusicManager(Context context, boolean isMusicOn) {
        this.context = context;
        this.isMusicOn = isMusicOn;
        initSoundPool();
        initMediaPlayer();
    }

    private void initSoundPool() {
        // 使用 Builder 模式创建 SoundPool [cite: 198, 199]
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5) // 设置最大并发播放数 [cite: 201]
                .setAudioAttributes(audioAttributes)
                .build();

        // 加载音频，获取全局唯一的 soundId [cite: 203, 204]
        // 注意：替换为你 res/raw 里的实际文件名
        bulletSoundId = soundPool.load(context, R.raw.bullet, 1);
        hitSoundId = soundPool.load(context, R.raw.bullet_hit, 1);
        bombSoundId = soundPool.load(context, R.raw.bomb_explosion, 1);
        gameOverSoundId = soundPool.load(context, R.raw.game_over, 1);
        getSupplySoundId = soundPool.load(context, R.raw.get_supply, 1);
    }

    private void initMediaPlayer() {
        // 播放本地小文件，直接使用 create 静态方法合并准备步骤 [cite: 192]
        bgmPlayer = MediaPlayer.create(context, R.raw.bgm);
        bgmPlayer.setLooping(true); // 设置循环播放

        bossBgmPlayer = MediaPlayer.create(context, R.raw.bgm_boss);
        bossBgmPlayer.setLooping(true);
    }

    // --- 播放控制方法 ---

    public void playBGM() {
        if (isMusicOn && bgmPlayer != null && !bgmPlayer.isPlaying()) {
            bgmPlayer.start(); // 启动播放 [cite: 185]
        }
    }

    public void playBossBGM() {
        if (isMusicOn && bossBgmPlayer != null) {
            if (bgmPlayer != null && bgmPlayer.isPlaying()) bgmPlayer.pause();
            bossBgmPlayer.start();
            bossIsPlaying = true;
        }
    }

    public void stopBossBGM() {
        if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) {
            bossBgmPlayer.pause();
            bossBgmPlayer.seekTo(0);
            bossIsPlaying = false;
            playBGM();
        }
    }

    /** 暂停后恢复：根据暂停前的状态恢复对应 BGM */
    public void resumeBGM() {
        if (!isMusicOn) return;
        if (bossIsPlaying) {
            if (bossBgmPlayer != null && !bossBgmPlayer.isPlaying()) bossBgmPlayer.start();
        } else {
            if (bgmPlayer != null && !bgmPlayer.isPlaying()) bgmPlayer.start();
        }
    }

    public void playShootSound() {
        if (isMusicOn) {
            // 传入 soundId, 音量, 优先级等参数控制播放 [cite: 206, 207]
            soundPool.play(bulletSoundId, 0.2f, 0.2f, 1, 0, 1.0f);
        }
    }

    public void playHitSound() {
        if (isMusicOn) soundPool.play(hitSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void playBombSound() {
        if (isMusicOn) soundPool.play(bombSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
    }

    public void playGameOverSound() {
        if (isMusicOn) soundPool.play(gameOverSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
    }

    public void playGetSupplySound() {
        if (isMusicOn) soundPool.play(getSupplySoundId, 0.5f, 0.5f, 1, 0, 1.0f);
    }


    public void pauseBGM() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) bgmPlayer.pause();
        if (bossBgmPlayer != null && bossBgmPlayer.isPlaying()) bossBgmPlayer.pause();
    }

    // 彻底释放资源（幂等：多次调用安全）
    public void releaseAll() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (bossBgmPlayer != null) {
            bossBgmPlayer.stop();
            bossBgmPlayer.release();
            bossBgmPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}