package edu.hitsz.server.socket;

import edu.hitsz.server.GameRoomManager;

import java.io.*;
import java.net.Socket;

public class GameSocketHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameSocketHandler opponent;
    private String roomId;

    public GameSocketHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String firstLine = in.readLine();
            if (firstLine == null || !firstLine.startsWith("JOIN:")) {
                send("ERROR:expected JOIN:roomId");
                return;
            }
            roomId = firstLine.split(":", 2)[1].trim();
            if (roomId.isEmpty()) {
                send("ERROR:empty room id");
                return;
            }

            opponent = GameRoomManager.getInstance().joinRoom(roomId, this);

            if (opponent == null) {
                send("WAITING");
                synchronized (this) {
                    wait(60000);
                }
                if (opponent == null) {
                    send("TIMEOUT");
                    GameRoomManager.getInstance().removeRoom(roomId);
                    return;
                }
            } else {
                opponent.setOpponent(this);
                opponent.notifyOpponent();
                send("START");
                opponent.send("START");
            }

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("SCORE:")) {
                    String score = line.split(":", 2)[1];
                    opponent.send("OPPONENT:" + score);
                } else if (line.startsWith("GAMEOVER:")) {
                    String score = line.split(":", 2)[1];
                    opponent.send("OPPONENT_GAMEOVER:" + score);
                    break;
                }
            }

        } catch (IOException e) {
            if (opponent != null) {
                opponent.send("OPPONENT_DISCONNECT");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (roomId != null) {
                GameRoomManager.getInstance().removeRoom(roomId);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    synchronized void setOpponent(GameSocketHandler opp) {
        this.opponent = opp;
    }

    synchronized void notifyOpponent() {
        notify();
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}
