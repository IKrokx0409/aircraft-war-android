package edu.hitsz.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameRecordDaoImpl implements GameRecordDao {

    private static final String FILENAME = "records.dat";

    @Override
    public void addRecord(GameRecord record) {
        List<GameRecord> records = getAllRecords();
        records.add(record);
        saveRecords(records);
    }

    @Override
    public List<GameRecord> getAllRecords() {
        File file = new File(FILENAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<GameRecord>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveRecords(List<GameRecord> records) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            oos.writeObject(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}