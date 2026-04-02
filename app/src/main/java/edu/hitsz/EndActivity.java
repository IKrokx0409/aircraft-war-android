package edu.hitsz;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EndActivity extends AppCompatActivity {

    private TextView tvScore;
    private Button btnRestart, btnMainMenu, btnExit;
    private int finalScore;

    // 定义返回结果常量
    public static final int RESULT_RESTART = 100;
    public static final int RESULT_MENU = 101;
    public static final int RESULT_EXIT = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        // 初始化组件
        tvScore = findViewById(R.id.tv_score);
        btnRestart = findViewById(R.id.btn_restart);
        btnMainMenu = findViewById(R.id.btn_main_menu);
        btnExit = findViewById(R.id.btn_exit);

        // 获取游戏结束时的得分
        finalScore = getIntent().getIntExtra("score", 0);

        // 显示得分
        tvScore.setText(getString(R.string.final_score, finalScore));

        // 重新开始按钮
        btnRestart.setOnClickListener(v -> {
            setResult(RESULT_RESTART);
            finish();
        });

        // 返回主菜单按钮
        btnMainMenu.setOnClickListener(v -> {
            setResult(RESULT_MENU);
            finish();
        });

        // 退出游戏按钮
        btnExit.setOnClickListener(v -> {
            setResult(RESULT_EXIT);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // ✅ 如果用户按返回键，默认作为"返回主菜单"处理
        setResult(RESULT_MENU);
        super.onBackPressed();
    }
}