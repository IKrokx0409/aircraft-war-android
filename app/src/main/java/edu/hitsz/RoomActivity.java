package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        EditText etRoomId = findViewById(R.id.et_room_id);
        Button btnJoin = findViewById(R.id.btn_join);
        Button btnBack = findViewById(R.id.btn_back);

        Bundle extras = getIntent().getExtras();

        btnJoin.setOnClickListener(v -> {
            String roomId = etRoomId.getText().toString().trim();
            if (roomId.isEmpty()) {
                Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(RoomActivity.this, MainActivity.class);
            if (extras != null) intent.putExtras(extras);
            intent.putExtra("mode", "online");
            intent.putExtra("roomId", roomId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
