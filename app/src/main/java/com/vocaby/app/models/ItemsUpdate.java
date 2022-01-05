package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.vocaby.app.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Can I use a SparseArray here?
public abstract class ItemsUpdate<T> implements Parcelable {
    private final Map<String, T> itemsAdded;
    private final Map<String, T> itemsDeleted;
    private final Map<String, T> itemsUpdated;

    public ItemsUpdate(Map<String, T> itemsAdded, Map<String, T> itemsDeleted,
                       Map<String, T> itemsUpdated) {
        this.itemsAdded = itemsAdded;
        this.itemsDeleted = itemsDeleted;
        this.itemsUpdated = itemsUpdated;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeMap(itemsAdded);
        out.writeMap(itemsDeleted);
        out.writeMap(itemsUpdated);
    }

    protected ItemsUpdate(Parcel in) {
        itemsAdded = new HashMap<>();
        itemsDeleted = new HashMap<>();
        itemsUpdated = new LinkedHashMap<>();

        in.readMap(itemsAdded, getClass().getClassLoader());
        in.readMap(itemsDeleted, getClass().getClassLoader());
        in.readMap(itemsUpdated, getClass().getClassLoader());
    }

    public List<T> getAddedItems() {
        return new ArrayList<>(itemsAdded.values());
    }

    public Set<String> getAddedKeySet() {
        return new HashSet<>(itemsAdded.keySet());
    }

    public T getAddedItem(String key) {
        return itemsAdded.get(key);
    }
    public T getUpdatedItem(String key) {
        return itemsUpdated.get(key);
    }


    public T putItemAdded(String key, T item) {
        return itemsAdded.put(key, item);
    }

    public T replaceItemAdded(String key, T item) { return itemsAdded.replace(key, item); }

    public T removeItemAdded(String key) {
        return itemsAdded.remove(key);
    }

    public boolean hasItemAdded(String key) {
        return itemsAdded.containsKey(key);
    }

    public List<T> getDeletedItems() {
        return new ArrayList<>(itemsDeleted.values());
    }

    public T putItemDeleted(String key, T item) {
        return itemsDeleted.put(key, item);
    }

    public T removeItemDeleted(String key) {
        return itemsDeleted.remove(key);
    }

    public boolean hasItemDeleted(String key) {
        return itemsDeleted.containsKey(key);
    }

    public List<T> getUpdatedItems() {
        return new ArrayList<>(itemsUpdated.values());
    }

    public void putItemUpdated(String key, T item) {
        if (!itemsAdded.containsKey(key) && !itemsDeleted.containsKey(key)) itemsUpdated.put(key, item);
    }

    public void removeItemUpdated(String key) {
        itemsUpdated.remove(key);
    }

    public boolean hasItemUpdated(String key) {
        return itemsUpdated.containsKey(key);
    }

    public int getItemsUpdatedSize() {
        return itemsUpdated.size();
    }

    public T addItem(String key, T item) {
        if (itemsDeleted.containsKey(key)) {
            return removeItemDeleted(key);
        }

        itemsUpdated.remove(key);
        return putItemAdded(key, item);
    }

    public T removeItem(String key, T item) {
        if (itemsAdded.containsKey(key)) {
            return removeItemAdded(key);
        }

        itemsUpdated.remove(key);
        return putItemDeleted(key, item);
    }

    public boolean hasChanges() {
        return !itemsUpdated.isEmpty() || !itemsAdded.isEmpty() || !itemsDeleted.isEmpty();
    }

    public boolean hasItem(String key) {
        return hasItemAdded(key) || hasItemDeleted(key) || hasItemUpdated(key);
    }

    public void printDebug() {
        for (String key : itemsAdded.keySet()) {
            Log.d(Constants.DEBUG_TAG, "Added Item: " + key);
        }

        for (String key : itemsDeleted.keySet()) {
            Log.d(Constants.DEBUG_TAG, "Deleted Item: " + key);
        }

        for (String key : itemsUpdated.keySet()) {
            Log.d(Constants.DEBUG_TAG, "Updated Item: " + key);
        }
    }
}
