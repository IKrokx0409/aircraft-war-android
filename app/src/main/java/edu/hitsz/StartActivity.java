package edu.hitsz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import edu.hitsz.application.Game;

public class StartActivity extends AppCompatActivity {

    private Button btnSingleMode, btnMultiMode;
    private SwitchCompat switchSound;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "GameSettings";
    private static final String KEY_SOUND = "sound_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 初始化控件
        btnSingleMode = findViewById(R.id.btn_single_mode);
        btnMultiMode = findViewById(R.id.btn_multi_mode);
        switchSound = findViewById(R.id.switch_sound);

        // 读取之前保存的音效状态
        boolean soundEnabled = sharedPreferences.getBoolean(KEY_SOUND, true);
        switchSound.setChecked(soundEnabled);

        // 音效开关监听
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_SOUND, isChecked);
            editor.apply();
            Toast.makeText(StartActivity.this,
                    isChecked ? "音效已开启" : "音效已关闭",
                    Toast.LENGTH_SHORT).show();
        });

        // 单机模式按钮
        btnSingleMode.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.putExtra("mode", "single");
            intent.putExtra("soundEnabled", switchSound.isChecked());
            startActivity(intent);
            //不调用 finish(), 这样 StartActivity 会留在栈中
            //finish();
        });

        // 联机模式按钮
        btnMultiMode.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.putExtra("mode", "multi");
            intent.putExtra("soundEnabled", switchSound.isChecked());
            startActivity(intent);
            // Toast.makeText(StartActivity.this, "联机功能开发中...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 返回到 StartActivity 时，重新读取音效状态
        boolean soundEnabled = sharedPreferences.getBoolean(KEY_SOUND, true);
        switchSound.setChecked(soundEnabled);
    }
}