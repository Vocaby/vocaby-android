package com.example.vocaby;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReciever extends BroadcastReceiver {
    private static final String CHANNEL_ID = "VOCABY_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        DataManager dataManager = DataManager.getInstance(context);
        WordPickerService wordPickerService = new WordPickerService(dataManager, context);
        Word wordData = wordPickerService.getRandomWordFromSaves();
        String word;
        String pos;
        String message;

        if(wordData == null) {
            word = "No Saved Words";
            message = "Save words in the app to dispay in the notification";
        } else {
            word = wordData.getWord();
            pos = wordData.getAllowedPos()[0];
            message = wordData.getDefinitions(pos)[0];
        }

        createNotificationChannel(notificationManager);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_white)
                .setColor(context.getColor(R.color.colorPrimary))
                .setContentTitle(word.toUpperCase())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message);
        notificationManager.notify(777, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Vocaby Notification";
            String description = "Vocaby Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
