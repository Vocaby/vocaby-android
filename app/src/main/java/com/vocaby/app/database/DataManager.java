package com.vocaby.app.database;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

public class DataManager {
    private static DataManager dataManager = null;
    private static Context ctx;
    private static final String HISTORY_DATA_FILE_NAME = "hVocaby";
    private static List<String> history;

    private DataManager(Context context) {
        ctx = context.getApplicationContext();
        File historyFile = new File(ctx.getFilesDir(), HISTORY_DATA_FILE_NAME);
        if(historyFile.exists()) {
            try(FileInputStream fis = ctx.openFileInput(HISTORY_DATA_FILE_NAME)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                history = (List<String>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.d("DataManager", "Something went wrong in getInstance");
            }
        } else {
            history = new LinkedList<>();
            try (FileOutputStream fos = ctx.openFileOutput(HISTORY_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(history);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static DataManager getInstance(Context context) {
        if(dataManager == null) {
            dataManager = new DataManager(context);
        }

        return dataManager;
    }

    public List<String> writeHistory(String word) {
        history.add(0, word);
        if(history.size() > 3) {
            history.remove(3);
        }

        new Thread(() -> {
            try (FileOutputStream fos = ctx.openFileOutput(HISTORY_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(history);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return history;
    }

    public List<String> getHistory() {
        return history;
    }
}
