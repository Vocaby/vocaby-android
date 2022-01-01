package com.vocaby.app.widget

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.room.rxjava3.EmptyResultSetException
import com.vocaby.app.R
import com.vocaby.app.data.VocabyDatabase
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.exceptions.SaveRepetitionException
import com.vocaby.app.ui.MainActivity
import com.vocaby.app.utils.Generators.generateRandomInt
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavesAppWidgetProvider : AppWidgetProvider() {
    companion object {
        const val WIDGET_CLICK = "widgetClick"
        const val WIDGET_PREV_KEY = "WIDGET_PREV_SELECT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == WIDGET_CLICK) {
            updateWidgetTexts(
                context,
                intent.getIntExtra("WIDGET_ID", -1),
                intent.getParcelableExtra("REMOTE_VIEW")
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidget(context)
    }

    private fun updateWidgetTexts(context: Context, id: Int, remoteViews: RemoteViews?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val vocabyDao =
            VocabyDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.Main.immediate))
                .vocabyDao()
        val vocabyRepository = VocabyRepository(vocabyDao, context.applicationContext as Application)
        val sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE)

        remoteViews?.let { view ->
            view.setViewVisibility(R.id.refresh_progress, View.VISIBLE)
            view.setBoolean(R.id.widget_refresh_button, "setEnabled", false)

            CoroutineScope(Dispatchers.Main.immediate).launch(CoroutineExceptionHandler { _, throwable ->
                when (throwable) {
                    is EmptyResultSetException -> {
                        remoteViews.setTextViewText(R.id.widget_word, "NO SAVED WORDS")
                        remoteViews.setTextViewText(
                            R.id.widget_definition,
                            "Save words in the app to review them here."
                        )
                        remoteViews.setTextViewText(R.id.widget_sentence, "")
                        remoteViews.setViewVisibility(R.id.refresh_progress, View.GONE)
                        remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", true)
                        appWidgetManager.updateAppWidget(id, remoteViews)
                    }

                    is SaveRepetitionException -> {
                        /* DO NOTHING */
                    }

                    else -> {}
                }
            }) {
                val saves = vocabyRepository.getSavedWords()
                val prevWord = sp.getString(WIDGET_PREV_KEY + id, "")
                if (saves.isEmpty()) {
                    val editor = sp.edit()
                    editor.putString(WIDGET_PREV_KEY + id, "")
                    editor.apply()
                    throw EmptyResultSetException("User has no saves!")
                } else if (saves.size == 1) {
                    if (prevWord == saves[0]) {
                        throw SaveRepetitionException()
                    }
                }

                var index = generateRandomInt(0, saves.size - 1)
                while (saves[index] == prevWord) {
                    index = generateRandomInt(0, saves.size - 1)
                }

                val entryModel = vocabyRepository.getAllEntryData(saves[index])
                var definition = "No definition found"
                var example = ""

                entryModel?.let { wordData ->
                    val editor = sp.edit()
                    editor.putString(WIDGET_PREV_KEY + id, wordData.entry)
                    editor.apply()

                    val group = wordData.firstGroup
                    definition = group.definitionData[0].toString()
                    example = group.definitionData[0].example ?: ""
                }

                remoteViews.setTextViewText(R.id.widget_word, saves[index])
                remoteViews.setTextViewText(R.id.widget_definition, definition)
                remoteViews.setTextViewText(R.id.widget_sentence, example)

                val openIntent = Intent(context, MainActivity::class.java)
                openIntent.action = WIDGET_CLICK
                openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val openPendingIntent = PendingIntent
                    .getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

                remoteViews.setOnClickPendingIntent(R.id.widget_container, openPendingIntent)
                remoteViews.setViewVisibility(R.id.refresh_progress, View.GONE)
                remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", true)
                appWidgetManager.updateAppWidget(id, remoteViews)
            }
        }
    }

    private fun updateWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, SavesAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        for (id in appWidgetIds) {
            val refreshIntent = Intent(context, SavesAppWidgetProvider::class.java)
            refreshIntent.action = WIDGET_CLICK
            refreshIntent.putExtra("WIDGET_ID", id)
            val remoteViews = RemoteViews(context.packageName, R.layout.saves_widget)
            refreshIntent.putExtra("REMOTE_VIEW", remoteViews)
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, id, refreshIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            remoteViews.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)
            updateWidgetTexts(context, id, remoteViews)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE)
        val editor = sp.edit()
        for (id in appWidgetIds) {
            editor.remove(WIDGET_PREV_KEY + id)
            editor.apply()
        }
    }
}