package edu.hitsz.server;

import edu.hitsz.server.socket.GameSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoomManager {

    private static final GameRoomManager INSTANCE = new GameRoomManager();

    public static GameRoomManager getInstance() {
        return INSTANCE;
    }

    private GameRoomManager() {}

    private final Map<String, GameSocketHandler> waitingMap = new ConcurrentHashMap<>();

    /**
     * @return 对手 handler（已配对），null 表示进入等待
     */
    public synchronized GameSocketHandler joinRoom(String roomId, GameSocketHandler self) {
        GameSocketHandler opponent = waitingMap.remove(roomId);
        if (opponent != null) {
            return opponent;
        } else {
            waitingMap.put(roomId, self);
            return null;
        }
    }

    public synchronized void removeRoom(String roomId) {
        waitingMap.remove(roomId);
    }
}
