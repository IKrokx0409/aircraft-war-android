package edu.hitsz.manager;

import edu.hitsz.network.SocketClient;

public class OnlineGameManager implements GameManager {

    /**
     * RoomActivity 连接并等待 START 后，将自身存入此字段；
     * MainActivity 取走后立即置 null，防止泄漏。
     */
    public static volatile OnlineGameManager pending;

    private final SocketClient client = new SocketClient();

    public OnlineGameManager() {}

    public SocketClient getClient() {
        return client;
    }

    public void setCallback(SocketClient.MessageCallback callback) {
        client.setCallback(callback);
    }

    @Override
    public void initialize() {}

    @Override
    public void reset() {}

    @Override
    public void cleanup() {
        client.disconnect();
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public String getModeName() {
        return "online";
    }

    @Override
    public void sendScore(int score) {
        client.sendScore(score);
    }

    @Override
    public void sendGameOver(int score) {
        client.sendGameOver(score);
    }
}
