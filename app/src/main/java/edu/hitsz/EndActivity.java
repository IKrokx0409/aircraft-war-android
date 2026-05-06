package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EndActivity extends AppCompatActivity {

    public static final int RESULT_RESTART = 100;
    public static final int RESULT_MENU    = 101;
    public static final int RESULT_EXIT    = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        TextView tvTitle         = findViewById(R.id.tv_game_over);
        TextView tvScore         = findViewById(R.id.tv_score);
        TextView tvOpponentScore = findViewById(R.id.tv_opponent_score);
        Button btnRanking  = findViewById(R.id.btn_ranking);
        Button btnRestart  = findViewById(R.id.btn_restart);
        Button btnMainMenu = findViewById(R.id.btn_main_menu);
        Button btnExit     = findViewById(R.id.btn_exit);

        int     myScore       = getIntent().getIntExtra("score", 0);
        String  myName        = getIntent().getStringExtra("myName");
        int     opponentScore = getIntent().getIntExtra("opponentScore", 0);
        String  opponentName  = getIntent().getStringExtra("opponentName");
        boolean isOnline      = getIntent().getBooleanExtra("isOnline", false);

        if (myName == null || myName.isEmpty()) myName = "我";
        if (opponentName == null || opponentName.isEmpty()) opponentName = "对手";

        if (isOnline) {
            if (myScore > opponentScore) {
                tvTitle.setText("你赢了！");
                tvTitle.setTextColor(0xFF00AA00);
            } else if (myScore < opponentScore) {
                tvTitle.setText("你输了");
                tvTitle.setTextColor(0xFFCC0000);
            } else {
                tvTitle.setText("平局");
                tvTitle.setTextColor(0xFFFF8800);
            }

            tvScore.setText(myName + "：" + myScore + " 分");
            tvOpponentScore.setText(opponentName + "：" + opponentScore + " 分");
            tvOpponentScore.setVisibility(View.VISIBLE);

            btnRestart.setVisibility(View.GONE);
        } else {
            tvScore.setText(getString(R.string.final_score, myScore));
        }

        btnRanking.setOnClickListener(v ->
                startActivity(new Intent(this, RankingActivity.class)));

        btnRestart.setOnClickListener(v -> { setResult(RESULT_RESTART); finish(); });
        btnMainMenu.setOnClickListener(v -> { setResult(RESULT_MENU);    finish(); });
        btnExit.setOnClickListener(v ->     { setResult(RESULT_EXIT);    finish(); });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_MENU);
        super.onBackPressed();
    }
}
