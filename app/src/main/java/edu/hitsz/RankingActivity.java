package edu.hitsz;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.hitsz.dao.GameRecord;
import edu.hitsz.dao.GameRecordDao;
import edu.hitsz.dao.GameRecordDaoImpl;
import edu.hitsz.dao.RankingAdapter;

public class RankingActivity extends AppCompatActivity {

    private GameRecordDao dao;
    private List<GameRecord> allRecords;   // 全量数据（含所有难度）
    private List<GameRecord> shownRecords; // 当前 Tab 显示的过滤数据
    private RankingAdapter adapter;

    private Button btnTabEasy, btnTabNormal, btnTabHard;
    private String currentDifficulty = "easy";

    private static final int COLOR_SELECTED   = Color.parseColor("#FF6600");
    private static final int COLOR_UNSELECTED = Color.parseColor("#888888");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        dao = new GameRecordDaoImpl(getApplicationContext());

        ListView listView = findViewById(R.id.lv_ranking);
        btnTabEasy   = findViewById(R.id.btn_tab_easy);
        btnTabNormal = findViewById(R.id.btn_tab_normal);
        btnTabHard   = findViewById(R.id.btn_tab_hard);
        Button btnBack = findViewById(R.id.btn_back);

        shownRecords = new ArrayList<>();
        adapter = new RankingAdapter(this, shownRecords, record -> {
            // 删除：在全量列表中找到该 record 的实际索引再删
            int globalIndex = allRecords.indexOf(record);
            if (globalIndex >= 0) {
                dao.deleteRecord(globalIndex);
                allRecords.remove(globalIndex);
                refreshShownRecords();
            }
        });
        listView.setAdapter(adapter);

        btnTabEasy.setOnClickListener(v   -> switchTab("easy"));
        btnTabNormal.setOnClickListener(v -> switchTab("normal"));
        btnTabHard.setOnClickListener(v   -> switchTab("hard"));
        btnBack.setOnClickListener(v -> finish());

        loadAllRecords();
        switchTab("easy");
    }

    private void loadAllRecords() {
        allRecords = dao.getAllRecords();
        // 全量按分数降序，保证 deleteRecord(globalIndex) 的索引与文件一致
        allRecords.sort(Comparator.comparingInt(GameRecord::getScore).reversed());
    }

    private void switchTab(String difficulty) {
        currentDifficulty = difficulty;

        // 高亮选中 Tab
        btnTabEasy.setTextColor(  "easy".equals(difficulty)   ? COLOR_SELECTED : COLOR_UNSELECTED);
        btnTabNormal.setTextColor("normal".equals(difficulty) ? COLOR_SELECTED : COLOR_UNSELECTED);
        btnTabHard.setTextColor(  "hard".equals(difficulty)   ? COLOR_SELECTED : COLOR_UNSELECTED);

        refreshShownRecords();
    }

    private void refreshShownRecords() {
        shownRecords.clear();
        for (GameRecord r : allRecords) {
            String diff = r.getDifficulty() == null ? "normal" : r.getDifficulty();
            if (diff.equals(currentDifficulty)) {
                shownRecords.add(r);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
