package edu.hitsz.dao;

import java.util.List;

public interface GameRecordDao {
    void addRecord(GameRecord record);
    List<GameRecord> getAllRecords();
}