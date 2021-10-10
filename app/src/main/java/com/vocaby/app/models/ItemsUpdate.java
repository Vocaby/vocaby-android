package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Can I use a SparseArray here?
public abstract class ItemsUpdate<T> implements Parcelable {
    private Map<String, T> itemsAdded;
    private Map<String, T> itemsDeleted;
    private Map<String, T> itemsUpdated;

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


    public void putItemAdded(String key, T item) {
        itemsAdded.put(key, item);
    }

    public void removeItemAdded(String key) {
        itemsAdded.remove(key);
    }

    public boolean hasItemAdded(String key) {
        return itemsAdded.containsKey(key);
    }

    public List<T> getDeletedItems() {
        return new ArrayList<>(itemsDeleted.values());
    }

    public void putItemDeleted(String key, T item) {
        itemsDeleted.put(key, item);
    }

    public void removeItemDeleted(String key) {
        itemsDeleted.remove(key);
    }

    public boolean hasItemDeleted(String key) {
        return itemsDeleted.containsKey(key);
    }

    public List<T> getUpdatedItems() {
        return new ArrayList<>(itemsUpdated.values());
    }

    public void putItemUpdated(String key, T item) {
        itemsUpdated.put(key, item);
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

    public void addItem(String key, T item) {
        if (hasItemDeleted(key)) {
            removeItemDeleted(key);
        } else {
            putItemAdded(key, item);
        }
    }

    public void removeItem(String key, T item) {
        if (hasItemAdded(key)) {
            removeItemAdded(key);
        } else {
            putItemDeleted(key, item);
        }
    }

    public boolean hasChanges() {
        return !itemsUpdated.isEmpty() || !itemsAdded.isEmpty() || !itemsDeleted.isEmpty();
    }
}
