package edu.hitsz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    public static final String PREF_FILE = "game_prefs";
    public static final String KEY_NAME  = "player_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etName    = findViewById(R.id.et_player_name);
        Button   btnConfirm = findViewById(R.id.btn_confirm);

        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String saved = prefs.getString(KEY_NAME, "");
        if (!saved.isEmpty()) {
            etName.setText(saved);
            etName.setSelection(saved.length());
        }

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "请输入玩家名称", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_NAME, name).apply();
            startActivity(new Intent(this, StartActivity.class));
            finish();
        });
    }
}
