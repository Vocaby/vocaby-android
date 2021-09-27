package com.vocaby.app.utils;

import android.util.Log;

import java.util.List;
import java.util.Locale;

public class VocabyAlgo {
    public static int BinarySearchPrefix(List<String> list, String search) {
        return BinarySearchPrefix(0, list.size()-1, search, list);
    }

    private static int BinarySearchPrefix(int start, int end, String search, List<String> list) {
        if(start <= end) {
            int mid = (start + end) / 2;
            String midEntry = list.get(mid);
            int compResult = search.compareTo(midEntry);
            if(compResult == 0) {
                return mid;
            }

            if(midEntry.startsWith(search)) {
                if(mid-1 == -1) {
                    return 0;
                } else if (mid-1 > -1) {
                    if(list.get(mid-1).startsWith(search)) {
                        return BinarySearchPrefix(start, mid-1, search, list);
                    } else {
                        return mid;
                    }
                }
            }

            if(compResult < 0) {
                return BinarySearchPrefix(start, mid-1, search, list);
            } else {
                return BinarySearchPrefix(mid+1, end, search, list);
            }
        }

        return -1;
    }
}
