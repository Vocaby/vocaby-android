package com.vocaby.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.vocaby.app.database.DatabaseManager;

import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "VOCABY_CHANNEL";
    private static final String NOTIFICATION_PICKS = "notificationPicks";
    private static final String PREV_PICK = "notificationPrevPick";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOTIFICATION_PICKS, Context.MODE_PRIVATE);
        String prevWord = sharedPreferences.getString(PREV_PICK, null);
        DataManager dataManager = DataManager.getInstance(context);
        List<String> saves = dataManager.getSaves();
        WordPickerService wordPickerService = new WordPickerService(saves, context);
        int index = wordPickerService.getRandomWordFromSaves(prevWord);
        DatabaseManager databaseManager = DatabaseManager.getInstance(context);
        databaseManager.openDatabase();

        Word wordData = databaseManager.getWordData(saves.get(index));
        databaseManager.closeDatabase();
        String word;
        String pos;
        String message;

        if(saves.isEmpty()) {
            word = "No Saved Words";
            message = "Save words in the app to display in the notification";
        } else {
            word = wordData.getWord();
            pos = wordData.getAllowedPos()[0];
            message = wordData.getDefinitions(pos)[0];

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREV_PICK, word);
            editor.apply();
        }

        createNotificationChannel(notificationManager);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("com.vocaby.app.openAndSearch", word);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_white)
                .setColor(context.getColor(R.color.colorPrimary))
                .setContentTitle(word.toUpperCase())
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message);
        notificationManager.notify(777, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Vocaby Notification";
            String description = "Vocaby Notification";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
