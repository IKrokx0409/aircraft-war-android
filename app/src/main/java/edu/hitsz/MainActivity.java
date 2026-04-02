package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
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

        // 获取来自 StartActivity 的参数
        Intent intent = getIntent();
        String gameMode = intent.getStringExtra("mode");
        boolean soundEnabled = intent.getBooleanExtra("soundEnabled", true);

        // 根据模式创建对应的 GameManager
        if ("multi".equals(gameMode)) {
            Toast.makeText(this, "联机模式开发中...", Toast.LENGTH_SHORT).show();
            gameManager = new SinglePlayerManager();
        } else {
            gameManager = new SinglePlayerManager();
        }

        // 初始化游戏管理器
        gameManager.initialize();

        // 创建游戏视图
        gameView = new Game(this);
        gameView.setGameManager(gameManager);
        gameView.setGameMode(gameMode != null ? gameMode : "single");
        gameView.setSoundEnabled(soundEnabled);
        gameView.setOnGameEndListener(this);

        // 设置为内容视图
        setContentView(gameView);
    }

    @Override
    public void onGameEnd(int finalScore) {
        // 游戏结束时，启动 EndActivity
        Intent intent = new Intent(MainActivity.this, EndActivity.class);
        intent.putExtra("score", finalScore);
        startActivityForResult(intent, REQUEST_GAME_END);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GAME_END) {
            if (resultCode == EndActivity.RESULT_RESTART) {
                // ✅ 重新开始：重置游戏并恢复运行
                gameView.reset();
                gameView.resume();

            } else if (resultCode == EndActivity.RESULT_MENU) {
                // ✅ 返回主菜单：清理游戏，关闭 MainActivity
                gameView.cleanup();  // ← 这会等待线程停止
                finish();  // ← 然后才关闭 Activity

            } else if (resultCode == EndActivity.RESULT_EXIT) {
                // ✅ 退出游戏：清理所有资源，关闭整个应用
                gameView.cleanup();  // ← 这会等待线程停止
                finishAffinity();  // ← 然后才关闭所有 Activity
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ⚠️ 不要在这里停止游戏，EndActivity 作为 Modal 弹出时不会触发 onPause
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 游戏继续运行
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ⚠️ 在这里清理游戏
        if (gameView != null) {
            gameView.cleanup();
        }
    }
}