package edu.hitsz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.Game;
import edu.hitsz.manager.GameManager;
import edu.hitsz.manager.OnlineGameManager;
import edu.hitsz.manager.SinglePlayerManager;
import edu.hitsz.network.SocketClient;

public class MainActivity extends AppCompatActivity implements Game.OnGameEndListener {

    private Game gameView;
    private GameManager gameManager;
    private static final int REQUEST_GAME_END = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String gameMode = intent.getStringExtra("mode");
        boolean soundEnabled = intent.getBooleanExtra("soundEnabled", true);
        String difficulty = intent.getStringExtra("difficulty");
        if (difficulty == null) difficulty = "normal";
        String roomId = intent.getStringExtra("roomId");

        if ("online".equals(gameMode) && roomId != null) {
            // 联机模式
            OnlineGameManager onlineMgr = new OnlineGameManager(roomId);
            gameManager = onlineMgr;
            gameManager.initialize();

            onlineMgr.getClient().connect(roomId, new SocketClient.MessageCallback() {
                @Override
                public void onWaiting() {
                    Toast.makeText(MainActivity.this, "等待对手加入...", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStart() {
                    Toast.makeText(MainActivity.this, "对手已加入，开始对战！", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onOpponentScore(int score) {
                    if (gameView != null) gameView.updateOpponentScore(score);
                }

                @Override
                public void onOpponentGameOver(int score) {
                    if (gameView != null) gameView.setOpponentGameOver(score);
                }

                @Override
                public void onOpponentDisconnect() {
                    if (gameView != null) gameView.onOpponentDisconnect();
                    Toast.makeText(MainActivity.this, "对手已断线", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String msg) {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        } else if ("multi".equals(gameMode)) {
            // 旧的 multi 标记（无 roomId，容错处理）
            Toast.makeText(this, "联机模式开发中...", Toast.LENGTH_SHORT).show();
            gameManager = new SinglePlayerManager();
            gameManager.initialize();
        } else {
            // 单机模式
            gameManager = new SinglePlayerManager();
            gameManager.initialize();
        }

        gameView = new Game(this, soundEnabled);
        gameView.setGameManager(gameManager);
        gameView.setGameMode("online".equals(gameMode) ? "online" : "single");
        gameView.setDifficulty(difficulty);
        gameView.setSoundEnabled(soundEnabled);
        gameView.setOnGameEndListener(this);

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.addView(gameView);

        ImageButton btnPause = findViewById(R.id.btn_pause);
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
                gameView.reset();
                gameView.resume();

            } else if (resultCode == EndActivity.RESULT_MENU) {
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
