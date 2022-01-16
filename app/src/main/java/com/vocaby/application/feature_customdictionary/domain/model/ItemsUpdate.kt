package com.vocaby.application.feature_customdictionary.domain.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

abstract class ItemsUpdate<T> : Parcelable {
    private val itemsAdded: MutableMap<String?, T>
    private val itemsDeleted: MutableMap<String?, T>
    private val itemsUpdated: MutableMap<String?, T>

    constructor(
        itemsAdded: MutableMap<String?, T>,
        itemsDeleted: MutableMap<String?, T>,
        itemsUpdated: MutableMap<String?, T>
    ) {
        this.itemsAdded = itemsAdded
        this.itemsDeleted = itemsDeleted
        this.itemsUpdated = itemsUpdated
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeMap(itemsAdded)
        out.writeMap(itemsDeleted)
        out.writeMap(itemsUpdated)
    }

    protected constructor(`in`: Parcel) {
        itemsAdded = HashMap()
        itemsDeleted = HashMap()
        itemsUpdated = LinkedHashMap()
        `in`.readMap(itemsAdded, javaClass.classLoader)
        `in`.readMap(itemsDeleted, javaClass.classLoader)
        `in`.readMap(itemsUpdated, javaClass.classLoader)
    }

    val addedItems: List<T> get() = ArrayList(itemsAdded.values)
    val addedKeySet: HashSet<String> get() = HashSet(itemsAdded.keys)
    val deletedItems: List<T> get() = ArrayList(itemsDeleted.values)
    val updatedItems: List<T> get() = ArrayList(itemsUpdated.values)

    fun getAddedItem(key: String?): T? {
        return itemsAdded[key]
    }

    fun getUpdatedItem(key: String?): T? {
        return itemsUpdated[key]
    }

    fun putItemAdded(key: String?, item: T): T? {
        return itemsAdded.put(key, item)
    }

    fun replaceItemAdded(key: String?, item: T): T? {
        return itemsAdded.replace(key, item)
    }

    fun removeItemAdded(key: String?): T? {
        return itemsAdded.remove(key)
    }

    fun hasItemAdded(key: String?): Boolean {
        return itemsAdded.containsKey(key)
    }

    fun putItemDeleted(key: String?, item: T): T? {
        return itemsDeleted.put(key, item)
    }

    fun removeItemDeleted(key: String?): T? {
        return itemsDeleted.remove(key)
    }

    fun hasItemDeleted(key: String?): Boolean {
        return itemsDeleted.containsKey(key)
    }

    fun putItemUpdated(key: String?, item: T) {
        if (!itemsAdded.containsKey(key) && !itemsDeleted.containsKey(key)) itemsUpdated[key] = item
    }

    fun removeItemUpdated(key: String?) {
        itemsUpdated.remove(key)
    }

    fun hasItemUpdated(key: String?): Boolean {
        return itemsUpdated.containsKey(key)
    }

    fun getItemsUpdatedSize(): Int {
        return itemsUpdated.size
    }

    fun addItem(key: String?, item: T): T? {
        if (itemsDeleted.containsKey(key)) {
            return removeItemDeleted(key)
        }
        itemsUpdated.remove(key)
        return putItemAdded(key, item)
    }

    fun removeItem(key: String?, item: T): T? {
        if (itemsAdded.containsKey(key)) {
            return removeItemAdded(key)
        }
        itemsUpdated.remove(key)
        return putItemDeleted(key, item)
    }

    fun hasChanges(): Boolean {
        return itemsUpdated.isNotEmpty() || itemsAdded.isNotEmpty() || itemsDeleted.isNotEmpty()
    }

    fun hasItem(key: String?): Boolean {
        return hasItemAdded(key) || hasItemDeleted(key) || hasItemUpdated(key)
    }
}