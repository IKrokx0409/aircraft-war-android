package edu.hitsz.dao;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;
    private Date timestamp;

    public GameRecord(String playerName, int score, Date timestamp) {
        this.playerName = playerName;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        return sdf.format(timestamp);
    }

    @Override
    public String toString() {
        return playerName + ", " + score + ", " + getFormattedTimestamp();
    }
}