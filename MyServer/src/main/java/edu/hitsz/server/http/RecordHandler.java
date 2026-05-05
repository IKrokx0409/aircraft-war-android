package edu.hitsz.server.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.hitsz.server.model.GameRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecordHandler implements HttpHandler {

    private final Map<String, List<GameRecord>> records = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
                case "GET":
                    doGet(exchange); break;
                case "POST":
                    doPost(exchange); break;
                case "DELETE":
                    doDelete(exchange); break;
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

    private void doGet(HttpExchange exchange) throws IOException {
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

    private void doPost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        GameRecord record = gson.fromJson(body, GameRecord.class);

        if (record.getDifficulty() == null) record.setDifficulty("normal");
        if (record.getPlayerName() == null) record.setPlayerName("Unknown");
        record.setTimestamp(System.currentTimeMillis());

        records.computeIfAbsent(record.getDifficulty(),
                k -> Collections.synchronizedList(new ArrayList<>())).add(record);

        String json = "{\"success\":true}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private void doDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String difficulty = getParam(query, "difficulty");
        int id = Integer.parseInt(getParam(query, "id"));

        if (difficulty == null) difficulty = "normal";
        List<GameRecord> list = records.get(difficulty);
        if (list != null && id >= 0 && id < list.size()) {
            list.remove(id);
        }

        String json = "{\"success\":true}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
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
