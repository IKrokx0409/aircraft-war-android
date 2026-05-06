package edu.hitsz.network;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running;
    private volatile MessageCallback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface MessageCallback {
        void onWaiting();
        void onStart();
        void onOpponentName(String name);
        void onOpponentScore(int score);
        void onOpponentGameOver(int score);
        void onOpponentDisconnect();
        void onError(String msg);
    }

    /** playerName 会随 JOIN 消息发送给服务器，服务器转发给对手 */
    public void connect(String roomId, String playerName, MessageCallback callback) {
        this.callback = callback;
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ServerConfig.SERVER_IP,
                        ServerConfig.SOCKET_PORT), 5000);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                out.println("JOIN:" + roomId + ":" + playerName);

                running = true;
                new Thread(() -> {
                    try {
                        String line;
                        while (running && (line = in.readLine()) != null) {
                            final String msg = line.trim();
                            mainHandler.post(() -> dispatch(msg));
                        }
                    } catch (IOException e) {
                        if (running) {
                            mainHandler.post(() -> {
                                MessageCallback cb = this.callback;
                                if (cb != null) cb.onError("Connection lost");
                            });
                        }
                    }
                }).start();

            } catch (IOException e) {
                mainHandler.post(() -> {
                    MessageCallback cb = this.callback;
                    if (cb != null) cb.onError("连接失败: " + e.getMessage());
                });
            }
        }).start();
    }

    public void setCallback(MessageCallback callback) {
        this.callback = callback;
    }

    private void dispatch(String line) {
        MessageCallback cb = this.callback;
        if (cb == null) return;
        if (line.equals("WAITING")) {
            cb.onWaiting();
        } else if (line.equals("START")) {
            cb.onStart();
        } else if (line.equals("TIMEOUT")) {
            cb.onError("等待超时，对手未加入");
        } else if (line.startsWith("OPPONENT_NAME:")) {
            cb.onOpponentName(line.split(":", 2)[1].trim());
        } else if (line.startsWith("OPPONENT_GAMEOVER:")) {
            cb.onOpponentGameOver(parseInt(line));
        } else if (line.startsWith("OPPONENT:")) {
            cb.onOpponentScore(parseInt(line));
        } else if (line.equals("OPPONENT_DISCONNECT")) {
            cb.onOpponentDisconnect();
        }
    }

    private int parseInt(String line) {
        try {
            return Integer.parseInt(line.split(":", 2)[1].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public void sendScore(int score) {
        if (out != null) out.println("SCORE:" + score);
    }

    public void sendGameOver(int score) {
        if (out != null) out.println("GAMEOVER:" + score);
    }

    public void disconnect() {
        running = false;
        callback = null;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
