package edu.hitsz.dao;

import android.content.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameRecordDaoImpl implements GameRecordDao {

    private static final String FILENAME = "records.dat";

    private final File recordFile;

    public GameRecordDaoImpl(Context context) {
        recordFile = new File(context.getFilesDir(), FILENAME);
    }

    @Override
    public void addRecord(GameRecord record) {
        List<GameRecord> records = getAllRecords();
        records.add(record);
        saveRecords(records);
    }

    @Override
    public List<GameRecord> getAllRecords() {
        if (!recordFile.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(recordFile))) {
            return (List<GameRecord>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteRecord(int index) {
        List<GameRecord> records = getAllRecords();
        if (index >= 0 && index < records.size()) {
            records.remove(index);
            saveRecords(records);
        }
    }

    private void saveRecords(List<GameRecord> records) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(recordFile))) {
            oos.writeObject(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
