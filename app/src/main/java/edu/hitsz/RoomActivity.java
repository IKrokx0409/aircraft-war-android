package edu.hitsz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.manager.OnlineGameManager;
import edu.hitsz.network.SocketClient;

public class RoomActivity extends AppCompatActivity {

    private EditText etRoomId;
    private Button btnJoin;
    private OnlineGameManager pendingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        etRoomId = findViewById(R.id.et_room_id);
        btnJoin  = findViewById(R.id.btn_join);
        Button btnBack = findViewById(R.id.btn_back);

        Bundle extras = getIntent().getExtras();

        btnJoin.setOnClickListener(v -> {
            String roomId = etRoomId.getText().toString().trim();
            if (roomId.isEmpty()) {
                Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
                return;
            }
            startMatching(roomId, extras);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void startMatching(String roomId, Bundle extras) {
        setWaitingUi(true);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE);
        String myName = prefs.getString(LoginActivity.KEY_NAME, "Unknown");

        pendingManager = new OnlineGameManager();
        OnlineGameManager.pending = pendingManager;

        // 用数组绕过 lambda 只能捕获 effectively-final 变量的限制
        String[] opponentName = {"对手"};

        pendingManager.getClient().connect(roomId, myName, new SocketClient.MessageCallback() {

            @Override
            public void onWaiting() {
                btnJoin.setText("等待对手加入...");
            }

            @Override
            public void onOpponentName(String name) {
                opponentName[0] = name;
            }

            @Override
            public void onStart() {
                Intent intent = new Intent(RoomActivity.this, MainActivity.class);
                if (extras != null) intent.putExtras(extras);
                intent.putExtra("mode", "online");
                intent.putExtra("roomId", roomId);
                intent.putExtra("opponentName", opponentName[0]);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(RoomActivity.this, msg, Toast.LENGTH_LONG).show();
                cancelMatching();
            }

            @Override public void onOpponentScore(int score) {}
            @Override public void onOpponentGameOver(int score) {}
            @Override public void onOpponentDisconnect() {}
        });
    }

    private void cancelMatching() {
        if (pendingManager != null) {
            pendingManager.cleanup();
            pendingManager = null;
        }
        OnlineGameManager.pending = null;
        setWaitingUi(false);
    }

    private void setWaitingUi(boolean waiting) {
        etRoomId.setEnabled(!waiting);
        btnJoin.setEnabled(!waiting);
        btnJoin.setText(waiting ? "连接中..." : "加入");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingManager != null && OnlineGameManager.pending == pendingManager) {
            pendingManager.cleanup();
            OnlineGameManager.pending = null;
        }
    }
}
