package com.vocaby.app;

import android.content.Context;
import android.util.Log;

import com.vocaby.app.models.UserModel;
import com.vocaby.app.models.WordModel;

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
    private static final String USER_DATA_FILE_NAME = "uVocaby";
    private static final String HISTORY_DATA_FILE_NAME = "hVocaby";
    private static UserModel user;
    private static List<String> history;

    private DataManager(Context context) {
        ctx = context.getApplicationContext();

        File userFile = new File(ctx.getFilesDir(), USER_DATA_FILE_NAME);
        if(userFile.exists()) {
            try(FileInputStream fis = ctx.openFileInput(USER_DATA_FILE_NAME)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                user = (UserModel) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.d("DataManager", "Something went wrong in getInstance");
            }
        } else {
            user = new UserModel();
            try (FileOutputStream fos = ctx.openFileOutput(USER_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(user);
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


    public void setUser(UserModel newUser) throws IOException {
        user = newUser;
        try (FileOutputStream fos = ctx.openFileOutput(USER_DATA_FILE_NAME, Context.MODE_PRIVATE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
        }
    }

    public UserModel getUser() {
        return user;
    }
}
