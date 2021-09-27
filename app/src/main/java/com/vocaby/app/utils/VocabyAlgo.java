package com.vocaby.app.utils;

import java.util.List;

public class VocabyAlgo {
    public static int BinarySearchPrefix(List<String> list, String search) {
        return BinarySearchPrefix(0, list.size()-1, search.toLowerCase(), list);
    }

    private static int BinarySearchPrefix(int start, int end, String search, List<String> list) {
        if(start <= end) {
            int mid = (start + end) / 2;
            String midEntry = list.get(mid).toLowerCase();
            int compResult = search.compareTo(midEntry);
            if(compResult == 0) {
                return mid;
            }

            if(midEntry.startsWith(search)) {
                if(mid-1 == -1) {
                    return 0;
                } else if (mid-1 > -1) {
                    if(list.get(mid-1).toLowerCase().startsWith(search)) {
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
