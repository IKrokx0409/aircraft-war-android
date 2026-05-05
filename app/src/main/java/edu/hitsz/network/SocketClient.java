package edu.hitsz.network;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiveThread;
    private volatile boolean running;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface MessageCallback {
        void onWaiting();
        void onStart();
        void onOpponentScore(int score);
        void onOpponentGameOver(int score);
        void onOpponentDisconnect();
        void onError(String msg);
    }

    public void connect(String roomId, MessageCallback callback) {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ServerConfig.SERVER_IP,
                        ServerConfig.SOCKET_PORT), 5000);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("JOIN:" + roomId);

                running = true;
                receiveThread = new Thread(() -> {
                    try {
                        String line;
                        while (running && (line = in.readLine()) != null) {
                            final String msg = line.trim();
                            mainHandler.post(() -> dispatch(msg, callback));
                        }
                    } catch (IOException e) {
                        if (running) {
                            mainHandler.post(() -> callback.onError("Connection lost"));
                        }
                    }
                });
                receiveThread.start();

            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("连接失败: " + e.getMessage()));
            }
        }).start();
    }

    private void dispatch(String line, MessageCallback cb) {
        if (line.equals("WAITING")) {
            cb.onWaiting();
        } else if (line.equals("START")) {
            cb.onStart();
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
        if (out != null) {
            out.println("SCORE:" + score);
        }
    }

    public void sendGameOver(int score) {
        if (out != null) {
            out.println("GAMEOVER:" + score);
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
