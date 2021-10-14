package com.vocaby.app.receivers;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.Constants;
import com.vocaby.app.R;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.ui.MainActivity;

import java.util.concurrent.ThreadLocalRandom;

import io.reactivex.rxjava3.disposables.Disposable;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "VOCABY_CHANNEL";
    private Disposable disposable;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        VocabyRepository vocabyRepository = new VocabyRepository((Application) context.getApplicationContext());
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE);
        int currentId = sharedPreferences.getInt(Constants.CURRENT_USER_ID_KEY, 1);
        SharedPreferences sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE);

        disposable = vocabyRepository.getUserSaves(currentId)
                .flatMap(saves -> {
                    String prev_word = sp.getString("NOTIF_PREV_SELECT", "");
                    if(saves.isEmpty()) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("NOTIF_PREV_SELECT", "");
                        editor.apply();
                        throw new EmptyResultSetException("User has no saves!");
                    }

                    int index = ThreadLocalRandom.current().nextInt(0, saves.size());
                    if(saves.size() > 1) {
                        while(saves.get(index).equals(prev_word)) {
                            index = ThreadLocalRandom.current().nextInt(0, saves.size());
                        }
                    }

                    return vocabyRepository.getWordDataFromDatabase(saves.get(index));
                }).subscribe(wordData -> {
                    String word = wordData.getEntry();
                    String pos = wordData.getFirstGroup().getType();
                    String message = wordData.getFirstGroup().getDefinitionData().get(0).toString();

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("NOTIF_PREV_SELECT", word);
                    editor.apply();

                    createNotification(context, notificationManager, word, message);
                }, error -> {
                    if(error instanceof EmptyResultSetException) {
                        String title = "No Saved Words";
                        String message = "Save words in the app to display in the notification";
                        createNotification(context, notificationManager, title, message);
                    } else {
                        error.printStackTrace();
                    }
                });
    }

    private void createNotification(Context context, NotificationManager notificationManager, String title, String message) {
        disposable.dispose();
        createNotificationChannel(notificationManager);
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                resultIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_white)
                .setColor(context.getColor(R.color.colorPrimaryAccent))
                .setContentTitle(title.toUpperCase())
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message);
        notificationManager.notify(313, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        CharSequence name = "Vocaby Notification";
        String description = "Vocaby Notification";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);
    }
}
