package com.vocaby.app;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static DataManager dataManager = null;
    private static Context ctx;
    private static final String WORD_DATA_FILE_NAME = "dVocaby";
    private static final String SAVE_DATA_FILE_NAME = "sVocaby";
    private static final String HISTORY_DATA_FILE_NAME = "hVocaby";
    private static Map<String, Word> wordMap;
    private static List<String> saves;
    private static List<String> history;

    private DataManager(Context context) {
        ctx = context.getApplicationContext();

        File dataFile = new File(ctx.getFilesDir(), WORD_DATA_FILE_NAME);
        if(dataFile.exists()) {
            try(FileInputStream fis = ctx.openFileInput(WORD_DATA_FILE_NAME)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                wordMap = (Map<String, Word>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.d("DataManager", "Something went wrong in getInstance");
            }
        } else {
            wordMap = new HashMap<>();
            try (FileOutputStream fos = ctx.openFileOutput(WORD_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(wordMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File saveFile = new File(ctx.getFilesDir(), SAVE_DATA_FILE_NAME);
        if(saveFile.exists()) {
            try(FileInputStream fis = ctx.openFileInput(SAVE_DATA_FILE_NAME)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                saves = (List<String>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.d("DataManager", "Something went wrong in getInstance");
            }
        } else {
            saves = new ArrayList<>();
            try (FileOutputStream fos = ctx.openFileOutput(SAVE_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(saves);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

    public void writeHistory(String word) {
        history.add(0, word);
        if(history.size() > 3) {
            history.remove(3);
        }
        try (FileOutputStream fos = ctx.openFileOutput(HISTORY_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(history);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<String> getHistory() {
        return history;
    }


    public void writeSave(String word) throws IOException {
        saves.add(0, word);
        try (FileOutputStream fos = ctx.openFileOutput(SAVE_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(saves);
        }
    }

    public void deleteSave(String word) {
        saves.remove(word);
        try (FileOutputStream fos = ctx.openFileOutput(SAVE_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(saves);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasSave(String word) {
        return saves.contains(word);
    }

    public List<String> getSaves() {
        return saves;
    }

    public void writeData(Word wordData) throws IOException {
        wordMap.put(wordData.getWord(), wordData);
        try (FileOutputStream fos = ctx.openFileOutput(WORD_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(wordMap);
        }
    }

    public Word getData(String word) {
        return wordMap.get(word);
    }

    public boolean hasWord(String word) {
        return wordMap.containsKey(word);
    }
}
