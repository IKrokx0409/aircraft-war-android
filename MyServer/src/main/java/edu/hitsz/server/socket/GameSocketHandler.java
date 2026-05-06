package edu.hitsz.server.socket;

import edu.hitsz.server.GameRoomManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class GameSocketHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    // volatile 保证跨线程可见性：HandlerB 写入后 HandlerA 的轮询循环立即可见
    private volatile GameSocketHandler opponent;
    private String roomId;
    private String playerName = "Unknown";

    public GameSocketHandler(Socket socket) {
        this.socket = socket;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void run() {
        boolean gameOverSent = false;
        String tag = "[H@" + Integer.toHexString(System.identityHashCode(this)) + "]";
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String firstLine = in.readLine();
            System.out.println(tag + " recv: " + firstLine);
            if (firstLine == null || !firstLine.startsWith("JOIN:")) {
                send("ERROR:expected JOIN:roomId:playerName");
                return;
            }
            String[] parts = firstLine.split(":", 3);
            roomId = parts[1].trim();
            if (roomId.isEmpty()) {
                send("ERROR:empty room id");
                return;
            }
            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                playerName = parts[2].trim();
            }
            System.out.println(tag + " room=" + roomId + " name=" + playerName);

            GameSocketHandler existingOpponent = GameRoomManager.getInstance().joinRoom(roomId, this);
            System.out.println(tag + " joinRoom -> " + (existingOpponent == null ? "FIRST (waiting)" : "SECOND matched with " + existingOpponent.getPlayerName()));

            if (existingOpponent == null) {
                // 第一个加入：等待对手，同时轮询 socket 检测客户端断线
                send("WAITING");
                socket.setSoTimeout(1000); // 每 1 秒检查一次 opponent 或断线
                long deadline = System.currentTimeMillis() + 60_000;
                try {
                    while (opponent == null && System.currentTimeMillis() < deadline) {
                        try {
                            if (in.readLine() == null) {
                                // 客户端主动关闭连接，直接退出（finally 会清理房间）
                                System.out.println(tag + " client disconnected while waiting");
                                return;
                            }
                        } catch (SocketTimeoutException ignored) {
                            // 正常超时，继续轮询
                        }
                    }
                } finally {
                    socket.setSoTimeout(0); // 恢复阻塞模式
                }
                if (opponent == null) {
                    System.out.println(tag + " TIMEOUT");
                    send("TIMEOUT");
                    return;
                }
                System.out.println(tag + " opponent arrived, entering game loop");
            } else {
                // 第二个加入：配对
                existingOpponent.setOpponent(this);
                this.opponent = existingOpponent;
                // 互相通知对方名字，再发 START
                send("OPPONENT_NAME:" + existingOpponent.getPlayerName());
                existingOpponent.send("OPPONENT_NAME:" + playerName);
                send("START");
                existingOpponent.send("START");
                System.out.println(tag + " sent START to both players");
            }

            // 游戏主循环
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("SCORE:")) {
                    opponent.send("OPPONENT:" + line.split(":", 2)[1]);
                } else if (line.startsWith("GAMEOVER:")) {
                    opponent.send("OPPONENT_GAMEOVER:" + line.split(":", 2)[1]);
                    gameOverSent = true;
                }
            }

        } catch (IOException e) {
            System.out.println(tag + " IOException: " + e.getMessage());
            if (opponent != null && !gameOverSent) {
                opponent.send("OPPONENT_DISCONNECT");
            }
        } finally {
            System.out.println(tag + " cleanup room=" + roomId + " paired=" + (opponent != null));
            if (roomId != null && opponent == null) {
                // 只有未配对时才清理房间，避免把后来加入同房间的玩家踢出 map
                GameRoomManager.getInstance().removeRoom(roomId);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    void setOpponent(GameSocketHandler opp) {
        this.opponent = opp;
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}
