package edu.hitsz.dao;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameRecord implements Serializable {
    private static final long serialVersionUID = 2L;

    private String playerName;
    private int score;
    private Date timestamp;
    private String difficulty;

    public GameRecord(String playerName, int score, Date timestamp, String difficulty) {
        this.playerName = playerName;
        this.score = score;
        this.timestamp = timestamp;
        this.difficulty = difficulty;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public String getDifficulty() { return difficulty; }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        return sdf.format(timestamp);
    }

    @Override
    public String toString() {
        return playerName + ", " + score + ", " + difficulty + ", " + getFormattedTimestamp();
    }
}
