package com.example.vocaby;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
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

        if(intent.getAction().equals(WIDGET_CLICK)) {
            Log.d("Click", "Click!!");
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
        if(saves.size() > 0) {
            WordPickerService wordPickerService = new WordPickerService(dataManager, context);
            Word wordData = wordPickerService.getRandomWordFromSaves();
            String pos = wordData.getAllowedPos()[0];
            String definition = wordData.getDefinitions(pos)[0];
            String[] examples = wordData.getSentences(pos);

            remoteViews.setTextViewText(R.id.widget_word, wordData.getWord());
            remoteViews.setTextViewText(R.id.widget_definition, definition);

            if(examples.length > 0) {
                remoteViews.setTextViewText(R.id.widget_sentence, examples[0]);
            }
        } else {
            remoteViews.setTextViewText(R.id.widget_word, "NO SAVED WORDS");
            remoteViews.setTextViewText(R.id.widget_definition, "Touch to Refresh");
            remoteViews.setTextViewText(R.id.widget_sentence, "");
        }

        appWidgetManager.updateAppWidget(id, remoteViews);
    }

    private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        dataManager = DataManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, SavesAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        for(int id : appWidgetIds) {
            Intent intent = new Intent(context, SavesAppWidgetProvider.class);
            intent.setAction(WIDGET_CLICK);
            intent.putExtra("WIDGET_ID", id);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.saves_widget);
            intent.putExtra("REMOTE_VIEW", remoteViews);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
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
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
