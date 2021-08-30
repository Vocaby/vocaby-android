package com.vocaby.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class WordPickerService {
    private final List<String> saves;
    private final Context context;
    private static final String SHARED_PICKS = "sharedPicks";
    private static final String PICK = "pick";

    public WordPickerService(List<String> saves, Context context) {
        this.saves = saves;
        this.context = context;
    }

    public int getRandomWordFromSaves() {
        return getRandomWordFromSaves(null);
    }

    public int getRandomWordFromSaves(String prevWord) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PICKS, Context.MODE_PRIVATE);
        int index = (int) (Math.random() * saves.size());
        int size = saves.size();

        if (size == 0) {
            return -1;
        } else if(size == 1) {
            index = 0;
        } else {
            int prevPick = prevWord == null ? sharedPreferences.getInt(PICK, 0) : saves.indexOf(prevWord);
            while(prevPick == index) {
                index = (int) (Math.random() * saves.size());
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PICK, index);
        editor.apply();

        return index;
    }
}
