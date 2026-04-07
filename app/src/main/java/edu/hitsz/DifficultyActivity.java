package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        String gameMode = getIntent().getStringExtra("mode");
        boolean soundEnabled = getIntent().getBooleanExtra("soundEnabled", true);

        // 显示当前游戏模式提示
        TextView tvModeHint = findViewById(R.id.tv_mode_hint);
        tvModeHint.setText("multi".equals(gameMode) ? "联机模式" : "单机模式");

        Button btnEasy   = findViewById(R.id.btn_easy);
        Button btnNormal = findViewById(R.id.btn_normal);
        Button btnHard   = findViewById(R.id.btn_hard);
        Button btnBack   = findViewById(R.id.btn_back);

        btnEasy.setOnClickListener(v -> launchGame(gameMode, soundEnabled, "easy"));
        btnNormal.setOnClickListener(v -> launchGame(gameMode, soundEnabled, "normal"));
        btnHard.setOnClickListener(v -> launchGame(gameMode, soundEnabled, "hard"));

        btnBack.setOnClickListener(v -> finish());
    }

    private void launchGame(String gameMode, boolean soundEnabled, String difficulty) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("mode", gameMode);
        intent.putExtra("soundEnabled", soundEnabled);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}
