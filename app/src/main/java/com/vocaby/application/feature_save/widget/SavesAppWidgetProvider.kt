package com.vocaby.application.feature_save.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.util.Generators.generateRandomInt
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SavesAppWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var dictionaryRepository: DictionaryRepository
    @Inject
    lateinit var customDictionaryRepository: CustomDictionaryRepository
    @Inject
    lateinit var saveRepository: SaveRepository
    @Inject
    lateinit var userRepository: UserRepository

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
        val scope = CoroutineScope(Dispatchers.Main.immediate)
        val sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE)

        remoteViews?.let { view ->
            view.setViewVisibility(R.id.refresh_progress, View.VISIBLE)
            view.setBoolean(R.id.widget_refresh_button, "setEnabled", false)

            scope.launch {
                val userId = userRepository.getUser()
                val saves = saveRepository.getAllSavedEntriesFlow(userId).first()

                if (saves.isEmpty()) {
                    val editor = sp.edit()
                    editor.putString("${WIDGET_PREV_KEY}_$id", "")
                    editor.apply()

                    val title = "No Saved Words"
                    val message = "Save words in the app to display in the notification"
                    renderWidget(view, context, appWidgetManager, id, title, message, "")
                } else {
                    var index = generateRandomInt(0, saves.size-1)
                    val prevWord = sp.getString("${WIDGET_PREV_KEY}_$id", "")

                    while (saves.size != 1 && saves[index] == prevWord) {
                        index = generateRandomInt(0, saves.size-1)
                    }

                    val entry = saves[index]
                    val entryModel: EntryModel? =
                        customDictionaryRepository.getUserEntryData(userId, entry) ?:
                        dictionaryRepository.getEntryDataFromDatabase(entry)

                    var message = "No definition found"
                    var example = ""

                    entryModel?.let { model ->
                        model.firstGroup?.let {
                            message = it.definitionData[0].definition
                            example = it.definitionData[0].example ?: ""
                        }
                    }

                    sp.edit().putString("${WIDGET_PREV_KEY}_$id", entry).apply()
                    renderWidget(view, context, appWidgetManager, id, entry, message, example)
                }
            }
        }
    }

    private fun renderWidget(
        remoteViews: RemoteViews,
        context: Context,
        appWidgetManager:
        AppWidgetManager,
        id: Int,
        entry: String,
        definition: String,
        example: String
    ) {
        remoteViews.setTextViewText(R.id.widget_word, entry)
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

    private fun updateWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, SavesAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        for (id in appWidgetIds) {
            val refreshIntent = Intent(context, SavesAppWidgetProvider::class.java)
            refreshIntent.action = WIDGET_CLICK
            refreshIntent.putExtra("WIDGET_ID", id)
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_saves)
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
            editor.remove("${WIDGET_PREV_KEY}_$id")
            editor.apply()
        }
    }
}