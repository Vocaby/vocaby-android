package com.example.vocaby;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataManager implements Serializable {
    private static DataManager dataManager = null;
    private static Context ctx;
    private static final String FILE_NAME = "dVocaby";
    private static Map<String, Word> wordMap;

    private DataManager(Context context) {
        ctx = context.getApplicationContext();
    }


    public static DataManager getInstance(Context context) {
        if(dataManager == null) {
            dataManager = new DataManager(context);
        }

        File dataFile = new File(ctx.getFilesDir(), FILE_NAME);
        if(dataFile.exists()) {
            try(FileInputStream fis = ctx.openFileInput(FILE_NAME)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                wordMap = (Map<String, Word>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.d("DataManager", "Something went wrong in getInstance");
            }
        } else {
            wordMap = new HashMap<>();
            try (FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(wordMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dataManager;
    }

    public void writeData(Word wordData) throws IOException {
        wordMap.put(wordData.getWord(), wordData);
        try (FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
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
