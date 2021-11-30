package com.vocaby.app.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class DataManager private constructor(context: Context) {
    private val historyDataFileName = "hVocaby"
    private val ctx: Context = context.applicationContext
    var history: LinkedList<String>? = null
    var numHistoryItems = 6

    companion object {
        private var instance: DataManager? = null

        fun getInstance(context: Context): DataManager =
            instance ?: synchronized(this) {
                instance ?: DataManager(context.applicationContext).also { instance = it }
            }
    }

    init {
        val historyFile = File(ctx.filesDir, historyDataFileName)
        if (historyFile.exists()) {
            try {
                ctx.openFileInput(historyDataFileName).use { fis ->
                    val ois = ObjectInputStream(fis)
                    history = ois.readObject() as LinkedList<String>
                    ois.close()
                }
            } catch (e: IOException) {
                Log.d("DataManager", "Something went wrong in getInstance")
            } catch (e: ClassNotFoundException) {
                Log.d("DataManager", "Something went wrong in getInstance")
            }
        } else {
            history = LinkedList()
            history?.let { newList ->
                newList.add("HISTORY")
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

    fun writeHistory(word: String): List<String>? {
        history?.let { list ->
            list.add(1, word)

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

    fun clearHistory(): List<String>? {
        history = LinkedList()
        history?.let { newList ->
            newList.add("HISTORY")

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
}