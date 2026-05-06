package edu.hitsz.server.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.hitsz.server.model.GameRecord;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecordHandler implements HttpHandler {

    private static final File DATA_FILE = new File("records.json");

    private final Map<String, List<GameRecord>> records;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public RecordHandler() {
        records = loadFromFile();
        System.out.println("RecordHandler: loaded " +
                records.values().stream().mapToInt(List::size).sum() + " record(s) from " + DATA_FILE.getAbsolutePath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET":    doGet(exchange);    break;
                case "POST":   doPost(exchange);   break;
                case "DELETE": doDelete(exchange); break;
                default:
                    exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "{\"error\":\"" + e.getMessage() + "\"}";
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, bytes.length);
            exchange.getResponseBody().write(bytes);
        } finally {
            exchange.close();
        }
    }

    private synchronized void doGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String difficulty = getParam(query, "difficulty");
        if (difficulty == null) difficulty = "normal";

        List<GameRecord> list = records.getOrDefault(difficulty, Collections.emptyList());
        List<GameRecord> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(GameRecord::getScore).reversed());

        String json = gson.toJson(sorted);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private synchronized void doPost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        GameRecord record = gson.fromJson(body, GameRecord.class);

        if (record.getDifficulty() == null) record.setDifficulty("normal");
        if (record.getPlayerName() == null) record.setPlayerName("Unknown");
        record.setTimestamp(System.currentTimeMillis());

        records.computeIfAbsent(record.getDifficulty(), k -> new ArrayList<>()).add(record);
        saveToFile();

        String json = "{\"success\":true}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private synchronized void doDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String difficulty = getParam(query, "difficulty");
        String idStr = getParam(query, "id");

        if (idStr == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        if (difficulty == null) difficulty = "normal";
        int id = Integer.parseInt(idStr);
        List<GameRecord> list = records.get(difficulty);
        if (list != null && id >= 0 && id < list.size()) {
            list.remove(id);
            saveToFile();
        }

        String json = "{\"success\":true}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private Map<String, List<GameRecord>> loadFromFile() {
        if (!DATA_FILE.exists()) return new ConcurrentHashMap<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(DATA_FILE), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, List<GameRecord>>>() {}.getType();
            Map<String, List<GameRecord>> loaded = gson.fromJson(reader, type);
            if (loaded == null) return new ConcurrentHashMap<>();
            return new ConcurrentHashMap<>(loaded);
        } catch (Exception e) {
            System.err.println("Failed to load records.json: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    private void saveToFile() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(DATA_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(records, writer);
        } catch (Exception e) {
            System.err.println("Failed to save records.json: " + e.getMessage());
        }
    }

    private String getParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }
}
