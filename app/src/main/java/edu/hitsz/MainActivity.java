package edu.hitsz;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.application.Game;
import edu.hitsz.manager.GameManager;
import edu.hitsz.manager.OnlineGameManager;
import edu.hitsz.manager.SinglePlayerManager;
import edu.hitsz.network.SocketClient;

public class MainActivity extends AppCompatActivity implements Game.OnGameEndListener {

    private Game gameView;
    private GameManager gameManager;
    private String opponentName = "对手";

    private final ActivityResultLauncher<Intent> gameEndLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == EndActivity.RESULT_RESTART) {
                    gameView.reset();
                    gameView.resume();
                } else if (result.getResultCode() == EndActivity.RESULT_MENU) {
                    gameView.cleanup();
                    navigateToStart();
                } else if (result.getResultCode() == EndActivity.RESULT_EXIT) {
                    gameView.cleanup();
                    finishAffinity();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String gameMode   = intent.getStringExtra("mode");
        boolean soundEnabled = intent.getBooleanExtra("soundEnabled", true);
        String difficulty = intent.getStringExtra("difficulty");
        if (difficulty == null) difficulty = "normal";

        if ("online".equals(gameMode) && OnlineGameManager.pending != null) {
            opponentName = intent.getStringExtra("opponentName") != null
                    ? intent.getStringExtra("opponentName") : "对手";

            OnlineGameManager onlineMgr = OnlineGameManager.pending;
            OnlineGameManager.pending = null;
            gameManager = onlineMgr;

            onlineMgr.setCallback(new SocketClient.MessageCallback() {
                @Override public void onWaiting() {}
                @Override public void onStart()   {}
                @Override public void onOpponentName(String name) {}

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
            Toast.makeText(this, "联机模式开发中...", Toast.LENGTH_SHORT).show();
            gameManager = new SinglePlayerManager();
            gameManager.initialize();
        } else {
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

    private void showPauseDialog() {
        gameView.pause();
        boolean online = gameManager != null && gameManager.isOnline();
        String[] options = online
                ? new String[]{"继续", "回到主界面", "退出游戏"}
                : new String[]{"继续", "重新开始", "回到主界面", "退出游戏"};
        new AlertDialog.Builder(this)
                .setTitle("游戏暂停")
                .setCancelable(false)
                .setItems(options, (dialog, which) -> {
                    if (online) {
                        switch (which) {
                            case 0: gameView.resume(); break;
                            case 1: gameView.cleanup(); navigateToStart(); break;
                            case 2: gameView.cleanup(); finishAffinity(); break;
                        }
                    } else {
                        switch (which) {
                            case 0: gameView.resume(); break;
                            case 1: gameView.reset(); gameView.resume(); break;
                            case 2: gameView.cleanup(); navigateToStart(); break;
                            case 3: gameView.cleanup(); finishAffinity(); break;
                        }
                    }
                })
                .show();
    }

    @Override
    public void onGameEnd(int myScore, int opponentScore, boolean isOnline) {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE);
        String myName = prefs.getString(LoginActivity.KEY_NAME, "我");

        Intent intent = new Intent(MainActivity.this, EndActivity.class);
        intent.putExtra("score", myScore);
        intent.putExtra("myName", myName);
        intent.putExtra("opponentScore", opponentScore);
        intent.putExtra("opponentName", opponentName);
        intent.putExtra("isOnline", isOnline);
        gameEndLauncher.launch(intent);
    }

    private void navigateToStart() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) gameView.cleanup();
    }
}
