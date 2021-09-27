package com.vocaby.app.widget;

import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.R;
import com.vocaby.app.exceptions.SaveRepetitionException;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.ui.MainActivity;

import java.util.concurrent.ThreadLocalRandom;

import io.reactivex.rxjava3.disposables.Disposable;

public class SavesAppWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_CLICK = "widgetClick";
    public static final String WIDGET_PREV_KEY = "WIDGET_PREV_SELECT_";
    public static Disposable disposable;
    public SavesAppWidgetProvider() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if(action.equals(WIDGET_CLICK)) {
            if(disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
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
        VocabyRepository vocabyRepository = new VocabyRepository((Application) context.getApplicationContext());

        SharedPreferences sharedPreferences = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        SharedPreferences sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE);
        int currentId = sharedPreferences.getInt("CURRENT_USER_ID", 1);

        remoteViews.setViewVisibility(R.id.refresh_progress, View.VISIBLE);
        remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", false);

        disposable = vocabyRepository.getUserSaves(currentId)
                .flatMap(saves -> {
                    String prev_word = sp.getString(WIDGET_PREV_KEY+id, "");
                    if(saves.isEmpty()) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(WIDGET_PREV_KEY+id, "");
                        editor.apply();
                        throw new EmptyResultSetException("User has no saves!");
                    } else if(saves.size() == 1) {
                        if(prev_word.equals(saves.get(0))) {
                            throw new SaveRepetitionException();
                        }
                    }

                    int index = ThreadLocalRandom.current().nextInt(0, saves.size());

                    while(saves.get(index).equals(prev_word)) {
                        index = ThreadLocalRandom.current().nextInt(0, saves.size());
                    }


                    return vocabyRepository.getWordDataFromDatabase(saves.get(index));
                }).subscribe(wordData -> {
                    String pos = wordData.getFirstGroup().getType();
                    String definition = wordData.getFirstGroup().getDefinitionData().get(0).toString();
                    String example = wordData.getFirstGroup().getDefinitionData().get(0).getExample();

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(WIDGET_PREV_KEY+id, wordData.getEntry());
                    editor.apply();

                    remoteViews.setTextViewText(R.id.widget_word, wordData.getEntry());
                    remoteViews.setTextViewText(R.id.widget_definition, definition);

                    if(!example.isEmpty()) {
                        remoteViews.setTextViewText(R.id.widget_sentence, example);
                    }

                    Intent openIntent = new Intent(context, MainActivity.class);
                    openIntent.setAction(WIDGET_CLICK);
                    openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent openPendingIntent = PendingIntent
                            .getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);
                    remoteViews.setOnClickPendingIntent(R.id.widget_container, openPendingIntent);

                    remoteViews.setViewVisibility(R.id.refresh_progress, View.GONE);
                    remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", true);
                    appWidgetManager.updateAppWidget(id, remoteViews);
                }, error -> {
                    if(error instanceof EmptyResultSetException) {
                        remoteViews.setTextViewText(R.id.widget_word, "NO SAVED WORDS");
                        remoteViews.setTextViewText(R.id.widget_definition, "Save words in the app to review them here.");
                        remoteViews.setTextViewText(R.id.widget_sentence, "");

                        remoteViews.setViewVisibility(R.id.refresh_progress, View.GONE);
                        remoteViews.setBoolean(R.id.widget_refresh_button, "setEnabled", true);
                        appWidgetManager.updateAppWidget(id, remoteViews);
                        Log.d("updateWidgetTexts: ", error.getMessage());
                    } else if(error instanceof SaveRepetitionException) {
                        Log.d("updateWidgetTexts: ", "do nothing");
                    } else {
                        error.printStackTrace();
                    }
                });
    }

    private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, SavesAppWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        for(int id : appWidgetIds) {
            Intent refreshIntent = new Intent(context, SavesAppWidgetProvider.class);
            refreshIntent.setAction(WIDGET_CLICK);
            refreshIntent.putExtra("WIDGET_ID", id);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.saves_widget);
            refreshIntent.putExtra("REMOTE_VIEW", remoteViews);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, id, refreshIntent,
                    PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent);

            updateWidgetTexts(context, id, remoteViews);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        for(int id : appWidgetIds) {
            editor.remove(WIDGET_PREV_KEY+id);
            editor.apply();
        }
    }
}
