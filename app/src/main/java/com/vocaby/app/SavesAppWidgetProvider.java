package com.vocaby.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.vocaby.app.database.DatabaseManager;

import java.util.List;

public class SavesAppWidgetProvider extends AppWidgetProvider {
    private DataManager dataManager;
    public static String WIDGET_CLICK = "widgetClick";
    public SavesAppWidgetProvider() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if(action.equals(WIDGET_CLICK)) {
            updateWidgetTexts(context, intent.getIntExtra("WIDGET_ID", -1), intent.getParcelableExtra("REMOTE_VIEW"));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidget(context);
    }

    private void updateWidgetTexts(Context context, int id, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        dataManager = DataManager.getInstance(context);
        List<String> saves = dataManager.getSaves();
        remoteViews.setViewVisibility(R.id.refresh_progress, View.VISIBLE);
        remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", false);
        if(saves.size() > 0) {
            WordPickerService wordPickerService = new WordPickerService(saves, context);
            int index = wordPickerService.getRandomWordFromSaves();
            DatabaseManager databaseManager = DatabaseManager.getInstance(context);
            if(!databaseManager.isOpen()) {
                databaseManager.openDatabase();
            }
            Word wordData = databaseManager.getWordData(saves.get(index));
            String pos = wordData.getAllowedPos()[0];
            String definition = wordData.getDefinitions(pos)[0];
            String[] examples = wordData.getSentences(pos);

            remoteViews.setTextViewText(R.id.widget_word, wordData.getWord());
            remoteViews.setTextViewText(R.id.widget_definition, definition);

            if(examples.length > 0) {
                remoteViews.setTextViewText(R.id.widget_sentence, examples[0]);
            }

            Intent openIntent = new Intent(context, MainActivity.class);
            openIntent.setAction(WIDGET_CLICK);
            openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            openIntent.putExtra("com.vocaby.app.openAndSearch", wordData.getWord());
            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_container, openPendingIntent);
        } else {
            remoteViews.setTextViewText(R.id.widget_word, "NO SAVED WORDS");
            remoteViews.setTextViewText(R.id.widget_definition, "Save words in the app to review them here.");
            remoteViews.setTextViewText(R.id.widget_sentence, "");
        }

        remoteViews.setViewVisibility(R.id.refresh_progress, View.INVISIBLE);
        remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", true);
        appWidgetManager.updateAppWidget(id, remoteViews);
    }

    private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        dataManager = DataManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, SavesAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        for(int id : appWidgetIds) {
            Intent refreshIntent = new Intent(context, SavesAppWidgetProvider.class);
            refreshIntent.setAction(WIDGET_CLICK);
            refreshIntent.putExtra("WIDGET_ID", id);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.saves_widget);
            refreshIntent.putExtra("REMOTE_VIEW", remoteViews);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, id, refreshIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent);

            updateWidgetTexts(context, id, remoteViews);
        }

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        DatabaseManager databaseManager = DatabaseManager.getInstance(context);
        databaseManager.closeDatabase();
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
