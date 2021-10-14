package com.vocaby.app.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.VocabyAlgo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class DataManager {
    private static DataManager dataManager = null;
    private static Context ctx;
    private static final String HISTORY_DATA_FILE_NAME = "hVocaby";
    private static List<String> history;
    private List<String> dictionaryEntries;

    private DataManager(Context context) {
        ctx = context.getApplicationContext();
        File historyFile = new File(ctx.getFilesDir(), HISTORY_DATA_FILE_NAME);
        if(historyFile.exists()) {
            try(FileInputStream fis = ctx.openFileInput(HISTORY_DATA_FILE_NAME)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                //noinspection unchecked
                history = (List<String>) ois.readObject();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                Log.d("DataManager", "Something went wrong in getInstance");
            }
        } else {
            history = new LinkedList<>();
            history.add("HISTORY");
            try (FileOutputStream fos = ctx.openFileOutput(HISTORY_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(history);
                oos.close();
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
        history.add(1, word);

        if(history.size() > 11) {
            history.remove(11);
        }

        new Thread(() -> {
            try (FileOutputStream fos = ctx.openFileOutput(HISTORY_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(history);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return history;
    }

    public List<String> getHistory() {
        return history;
    }

    public void replaceDictionaryEntries(List<String> newList) {
        SharedPreferences spf =
                PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());

        String entries = TextUtils.join(";", newList);
        SharedPreferences.Editor editor = spf.edit();
        editor.putString("dictionaryEntries", entries);
        editor.apply();
    }

    public Single<List<String>> populateDictionaryEntries() {
        SharedPreferences spf =
                PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        String entriesString = spf.getString("dictionaryEntries", "");
        if (!entriesString.isEmpty()) {
            dictionaryEntries = new ArrayList<>(Arrays.asList(entriesString.split(";")));
            return Single.just(dictionaryEntries);
        } else {
            VocabyRepository repo = new VocabyRepository((Application) ctx.getApplicationContext());
            return repo.getDictionaryEntriesFromDB()
                    .flatMap(list -> {
                        dictionaryEntries = list;
                        String entries = TextUtils.join(";", dictionaryEntries);
                        SharedPreferences.Editor editor = spf.edit();
                        editor.putString("dictionaryEntries", entries);
                        editor.apply();
                        return Single.just(dictionaryEntries);
                    });
        }
    }

    public List<String> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void addEntryToDictionary(String entry) {
        int index = VocabyAlgo.BinarySearchPrefix(dictionaryEntries, entry.substring(0, 1));
        for (int i = index; i < dictionaryEntries.size(); i++) {
            if (dictionaryEntries.get(i).equals(entry)) {
              break;
            } else if (dictionaryEntries.get(i).compareTo(entry) > 0) {
                dictionaryEntries.add(i, entry);
                break;
            }
        }

        replaceDictionaryEntries(dictionaryEntries);
    }

    public void deleteEntryFromDictionary(String entry) {
        int index = Collections.binarySearch(dictionaryEntries, entry);
        if (index != -1) dictionaryEntries.remove(index);

        replaceDictionaryEntries(dictionaryEntries);
    }
}
