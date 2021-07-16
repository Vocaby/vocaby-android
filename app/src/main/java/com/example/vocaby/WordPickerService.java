package com.example.vocaby;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

public class WordPickerService {
    private final DataManager dataManager;
    private Context context;
    private static final String SHARED_PICKS = "sharedPicks";
    private static final String PICK = "pick";

    public WordPickerService(DataManager dataManager, Context context) {
        this.dataManager = dataManager;
        this.context = context;
    }

    public Word getRandomWordFromSaves() {
        List<String> saves = dataManager.getSaves();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PICKS, Context.MODE_PRIVATE);
        int index = (int) (Math.random() * saves.size());
        int size = saves.size();

        if (size == 0) {
            return null;
        } else if(size == 1) {
            index = 0;
        } else {
            int prevPick = sharedPreferences.getInt(PICK, 0);
            while(prevPick == index) {
                index = (int) (Math.random() * saves.size());
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PICK, index);
        editor.apply();

        return dataManager.getData(saves.get(index));
    }
}
