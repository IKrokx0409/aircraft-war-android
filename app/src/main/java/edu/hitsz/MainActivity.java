package edu.hitsz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.Game;
import edu.hitsz.manager.GameManager;
import edu.hitsz.manager.SinglePlayerManager;

public class MainActivity extends AppCompatActivity implements Game.OnGameEndListener {

    private Game gameView;
    private GameManager gameManager;
    private static final int REQUEST_GAME_END = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取来自 DifficultyActivity 的参数
        Intent intent = getIntent();
        String gameMode = intent.getStringExtra("mode");
        boolean soundEnabled = intent.getBooleanExtra("soundEnabled", true);
        String difficulty = intent.getStringExtra("difficulty");
        if (difficulty == null) difficulty = "normal";

        // 根据模式创建对应的 GameManager
        if ("multi".equals(gameMode)) {
            Toast.makeText(this, "联机模式开发中...", Toast.LENGTH_SHORT).show();
        }
        gameManager = new SinglePlayerManager();
        gameManager.initialize();

        // 创建游戏视图并加入容器
        gameView = new Game(this, soundEnabled);
        gameView.setGameManager(gameManager);
        gameView.setGameMode(gameMode != null ? gameMode : "single");
        gameView.setDifficulty(difficulty);
        gameView.setSoundEnabled(soundEnabled);
        gameView.setOnGameEndListener(this);

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.addView(gameView);

        // 暂停按钮
        Button btnPause = findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(v -> showPauseDialog());
    }

    // ==========================================
    // 暂停对话框
    // ==========================================

    private void showPauseDialog() {
        gameView.pause();

        String[] options = {"继续", "重新开始", "回到主界面", "退出游戏"};

        new AlertDialog.Builder(this)
                .setTitle("游戏暂停")
                .setCancelable(false)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 继续
                            gameView.resume();
                            break;
                        case 1: // 重新开始（保持当前难度）
                            gameView.reset();
                            gameView.resume();
                            break;
                        case 2: // 回到主界面（StartActivity）
                            gameView.cleanup();
                            navigateToStart();
                            break;
                        case 3: // 退出游戏
                            gameView.cleanup();
                            finishAffinity();
                            break;
                    }
                })
                .show();
    }

    // ==========================================
    // 游戏结束回调
    // ==========================================

    @Override
    public void onGameEnd(int finalScore) {
        Intent intent = new Intent(MainActivity.this, EndActivity.class);
        intent.putExtra("score", finalScore);
        startActivityForResult(intent, REQUEST_GAME_END);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GAME_END) {
            if (resultCode == EndActivity.RESULT_RESTART) {
                // 重新开始：同难度，无需回到选择界面
                gameView.reset();
                gameView.resume();

            } else if (resultCode == EndActivity.RESULT_MENU) {
                // 返回主菜单：回到 StartActivity（选择单机/联机界面）
                gameView.cleanup();
                navigateToStart();

            } else if (resultCode == EndActivity.RESULT_EXIT) {
                gameView.cleanup();
                finishAffinity();
            }
        }
    }

    /** 清空回退栈，返回 StartActivity */
    private void navigateToStart() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ==========================================
    // 生命周期
    // ==========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.cleanup();
        }
    }
}
