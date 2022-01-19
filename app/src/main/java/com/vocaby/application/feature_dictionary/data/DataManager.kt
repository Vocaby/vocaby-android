package com.vocaby.application.feature_dictionary.data

import android.content.Context
import android.util.Log
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class DataManager(context: Context) {
    private val historyDataFileName = "hVocaby"
    private val ctx: Context = context.applicationContext
    var history: LinkedList<SimpleEntryModel>? = null
    var numHistoryItems = 6

    init {
        val historyFile = File(ctx.filesDir, historyDataFileName)
        if (historyFile.exists()) {
            try {
                ctx.openFileInput(historyDataFileName).use { fis ->
                    val ois = ObjectInputStream(fis)
                    history = ois.readObject() as LinkedList<SimpleEntryModel>?
                    ois.close()
                }
            } catch (e: IOException) {
                Log.d("DataManager", "Something went wrong in getInstance")
            } catch (e: ClassNotFoundException) {
                Log.d("DataManager", "Something went wrong in getInstance")
            }
        } else {
            history = LinkedList()
            history.let { newList ->
                try {
                    ctx.openFileOutput(historyDataFileName, Context.MODE_PRIVATE).use { fos ->
                        val oos = ObjectOutputStream(fos)
                        oos.writeObject(newList)
                        oos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
    }

    fun writeHistory(entry: SimpleEntryModel): List<SimpleEntryModel>? {
        history?.let { list ->
            list.add(0, entry)

            if (list.size > numHistoryItems) {
                list.removeAt(numHistoryItems)
            }

            try {
                ctx.openFileOutput(historyDataFileName, Context.MODE_PRIVATE).use { fos ->
                    val oos = ObjectOutputStream(fos)
                    oos.writeObject(list)
                    oos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return history
    }

    fun clearHistory(): List<SimpleEntryModel>? {
        history = LinkedList()
        history?.let { newList ->
            try {
                ctx.openFileOutput(historyDataFileName, Context.MODE_PRIVATE).use { fos ->
                    val oos = ObjectOutputStream(fos)
                    oos.writeObject(newList)
                    oos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return history
    }

    fun getHistory(position: Int): String? {
        return history?.get(position)?.entry
    }
}