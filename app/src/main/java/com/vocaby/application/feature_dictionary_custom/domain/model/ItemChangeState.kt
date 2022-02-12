package com.vocaby.application.feature_dictionary_custom.domain.model

import android.os.Parcel
import android.os.Parcelable

open class ItemChangeState<T> (
    var id: Int = -1,
    private var itemsAdded: LinkedHashMap<String, T> = LinkedHashMap(),
    private var itemsDeleted: LinkedHashMap<Int, T> = LinkedHashMap(),
    private var itemsUpdated: LinkedHashMap<Int, T> = LinkedHashMap()
): Parcelable {
    val deletedItems get() = itemsDeleted.values.toList()
    val addedItems get() = itemsAdded.values.toList()
    val updatedItems get() = itemsUpdated.values.toList()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(id)
        out.writeMap(itemsAdded)
        out.writeMap(itemsDeleted)
        out.writeMap(itemsUpdated)
    }

    protected constructor(`in`: Parcel) : this() {
        id = `in`.readInt()
        itemsAdded = LinkedHashMap()
        itemsDeleted = LinkedHashMap()
        itemsUpdated = LinkedHashMap()
        `in`.readMap(itemsAdded, javaClass.classLoader)
        `in`.readMap(itemsDeleted, javaClass.classLoader)
        `in`.readMap(itemsUpdated, javaClass.classLoader)
    }


    companion object CREATOR : Parcelable.Creator<ItemChangeState<Parcelable>> {
        override fun createFromParcel(parcel: Parcel): ItemChangeState<Parcelable> {
            return ItemChangeState(parcel)
        }

        override fun newArray(size: Int): Array<ItemChangeState<Parcelable>?> {
            return arrayOfNulls(size)
        }
    }

    fun putItemAdded(key: String, item: T): T? {
        return itemsAdded.put(key, item)
    }

    fun removeItemAdded(key: String): T? {
        return itemsAdded.remove(key)
    }

    fun hasItemAdded(key: String): Boolean {
        return itemsAdded.containsKey(key)
    }

    private fun putItemDeleted(key: Int, item: T): T? {
        return itemsDeleted.put(key, item)
    }

    fun removeItemDeleted(key: Int): T? {
        return itemsDeleted.remove(key)
    }

    fun putItemUpdated(key: Int, item: T): T? {
        return itemsUpdated.put(key, item)
    }

    fun removeItemUpdated(key: Int): T? {
        return itemsUpdated.remove(key)
    }

    fun addNew(key: String, item: T): T? {
        return putItemAdded(key, item)
    }

    fun updateNew(oldKey: String, item: T, newKey: String = oldKey): T? {
        if (itemsAdded.containsKey(oldKey))
            removeItemAdded(oldKey)
        return putItemAdded(newKey, item)
    }

    fun removeNew(key: String): T? {
        return removeItemAdded(key)
    }

    fun updateExisting(oldKey: Int, item: T, newKey: Int = oldKey): T? {
        if (itemsDeleted.containsKey(oldKey))
            removeItemDeleted(oldKey)
        return putItemUpdated(newKey, item)
    }

    fun removeExisting(key: Int, item: T): T? {
        if (itemsUpdated.containsKey(key))
            removeItemUpdated(key)
        return putItemDeleted(key, item)
    }

    fun hasChanges(): Boolean {
        return itemsUpdated.isNotEmpty() || itemsAdded.isNotEmpty() || itemsDeleted.isNotEmpty()
    }

    override fun toString(): String {
        return "Here are the changes:\n\n" +
                "Items Added: $itemsAdded\n\n" +
                "Items Removed: $itemsDeleted\n\n" +
                "Items Updated: $itemsUpdated\n\n"
    }
}