package edu.hitsz.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.hitsz.network.ServerConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameRecordDaoCloud implements GameRecordDao {

    private static final String TAG = "GameRecordDaoCloud";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String currentDifficulty = "normal";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class,
                    (JsonSerializer<Date>) (src, type, ctx) ->
                            new JsonPrimitive(src.getTime()))
            .registerTypeAdapter(Date.class,
                    (JsonDeserializer<Date>) (json, type, ctx) ->
                            new Date(json.getAsLong()))
            .create();

    public interface OnDataChangedListener {
        void onDataChanged(List<GameRecord> records);
        void onError(String message);
    }

    private OnDataChangedListener listener;

    public void setOnDataChangedListener(OnDataChangedListener l) {
        this.listener = l;
    }

    // ---- GameRecordDao 接口实现 ----

    @Override
    public void addRecord(GameRecord record) {
        new Thread(() -> {
            try {
                String json = gson.toJson(record);
                RequestBody body = RequestBody.create(json, JSON);
                Request req = new Request.Builder()
                        .url(ServerConfig.BASE_URL + "/api/records")
                        .post(body)
                        .build();
                client.newCall(req).execute().close();
            } catch (IOException e) {
                Log.e(TAG, "addRecord failed: " + e.getMessage(), e);
            }
        }).start();
    }

    @Override
    public List<GameRecord> getAllRecords() {
        throw new UnsupportedOperationException("Use requestAllRecords(difficulty) for async loading");
    }

    public void requestAllRecords(String difficulty) {
        this.currentDifficulty = difficulty;
        new Thread(() -> {
            try {
                Request req = new Request.Builder()
                        .url(ServerConfig.BASE_URL + "/api/records?difficulty=" + difficulty)
                        .get()
                        .build();
                Response resp = client.newCall(req).execute();
                String body = resp.body() != null ? resp.body().string() : "[]";
                resp.close();
                GameRecord[] arr = gson.fromJson(body, GameRecord[].class);
                List<GameRecord> list = new ArrayList<>(Arrays.asList(arr));
                mainHandler.post(() -> {
                    if (listener != null) listener.onDataChanged(list);
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
            }
        }).start();
    }

    @Override
    public void deleteRecord(int index) {
        new Thread(() -> {
            try {
                Request req = new Request.Builder()
                        .url(ServerConfig.BASE_URL + "/api/records?difficulty="
                                + currentDifficulty + "&id=" + index)
                        .delete()
                        .build();
                client.newCall(req).execute().close();
            } catch (IOException e) {
                Log.e(TAG, "deleteRecord failed: " + e.getMessage(), e);
            }
        }).start();
    }
}
