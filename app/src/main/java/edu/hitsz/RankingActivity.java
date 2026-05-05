package edu.hitsz;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.hitsz.dao.GameRecord;
import edu.hitsz.dao.GameRecordDao;
import edu.hitsz.dao.GameRecordDaoCloud;
import edu.hitsz.dao.GameRecordDaoImpl;
import edu.hitsz.dao.RankingAdapter;

public class RankingActivity extends AppCompatActivity {

    private GameRecordDao dao;
    private List<GameRecord> allRecords;
    private List<GameRecord> shownRecords;
    private RankingAdapter adapter;

    private Button btnTabEasy, btnTabNormal, btnTabHard;
    private Button btnTabLocal, btnTabCloud;
    private String currentDifficulty = "easy";
    private boolean isCloudMode = false;
    private GameRecordDaoCloud cloudDao;

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
        btnTabLocal  = findViewById(R.id.btn_tab_local);
        btnTabCloud  = findViewById(R.id.btn_tab_cloud);
        Button btnBack = findViewById(R.id.btn_back);

        shownRecords = new ArrayList<>();
        allRecords = new ArrayList<>();

        adapter = new RankingAdapter(this, shownRecords, record -> {
            if (isCloudMode) {
                int globalIndex = allRecords.indexOf(record);
                if (globalIndex >= 0) {
                    dao.deleteRecord(globalIndex);
                    Toast.makeText(this, "已删除，刷新中...", Toast.LENGTH_SHORT).show();
                    requestCloudData();
                }
            } else {
                int globalIndex = allRecords.indexOf(record);
                if (globalIndex >= 0) {
                    dao.deleteRecord(globalIndex);
                    allRecords.remove(globalIndex);
                    refreshShownRecords();
                }
            }
        });
        listView.setAdapter(adapter);

        btnTabEasy.setOnClickListener(v   -> switchTab("easy"));
        btnTabNormal.setOnClickListener(v -> switchTab("normal"));
        btnTabHard.setOnClickListener(v   -> switchTab("hard"));
        btnTabLocal.setOnClickListener(v  -> switchToLocal());
        btnTabCloud.setOnClickListener(v  -> switchToCloud());
        btnBack.setOnClickListener(v -> finish());

        loadLocalRecords();
        switchTab("easy");
        highlightSourceButton(btnTabLocal);
    }

    // ==================== 本地 ====================

    private void switchToLocal() {
        isCloudMode = false;
        dao = new GameRecordDaoImpl(getApplicationContext());
        highlightSourceButton(btnTabLocal);
        loadLocalRecords();
        switchTab(currentDifficulty);
    }

    private void loadLocalRecords() {
        allRecords = dao.getAllRecords();
        allRecords.sort(Comparator.comparingInt(GameRecord::getScore).reversed());
    }

    // ==================== 云端 ====================

    private void switchToCloud() {
        isCloudMode = true;
        if (cloudDao == null) {
            cloudDao = new GameRecordDaoCloud();
            cloudDao.setOnDataChangedListener(new GameRecordDaoCloud.OnDataChangedListener() {
                @Override
                public void onDataChanged(List<GameRecord> records) {
                    allRecords = new ArrayList<>(records);
                    allRecords.sort(Comparator.comparingInt(GameRecord::getScore).reversed());
                    refreshShownRecords();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(RankingActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
        dao = cloudDao;
        highlightSourceButton(btnTabCloud);
        requestCloudData();
    }

    private void requestCloudData() {
        Toast.makeText(this, "加载中...", Toast.LENGTH_SHORT).show();
        cloudDao.requestAllRecords(currentDifficulty);
    }

    // ==================== 通用 ====================

    private void switchTab(String difficulty) {
        currentDifficulty = difficulty;

        btnTabEasy.setTextColor(  "easy".equals(difficulty)   ? COLOR_SELECTED : COLOR_UNSELECTED);
        btnTabNormal.setTextColor("normal".equals(difficulty) ? COLOR_SELECTED : COLOR_UNSELECTED);
        btnTabHard.setTextColor(  "hard".equals(difficulty)   ? COLOR_SELECTED : COLOR_UNSELECTED);

        if (isCloudMode) {
            requestCloudData();
        } else {
            refreshShownRecords();
        }
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

    private void highlightSourceButton(Button selected) {
        btnTabLocal.setTextColor(selected == btnTabLocal ? COLOR_SELECTED : COLOR_UNSELECTED);
        btnTabCloud.setTextColor(selected == btnTabCloud ? COLOR_SELECTED : COLOR_UNSELECTED);
    }
}
