package edu.hitsz.manager;

import edu.hitsz.network.SocketClient;

public class OnlineGameManager implements GameManager {

    private final SocketClient client = new SocketClient();
    private final String roomId;

    public OnlineGameManager(String roomId) {
        this.roomId = roomId;
    }

    public SocketClient getClient() {
        return client;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void reset() {
    }

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
