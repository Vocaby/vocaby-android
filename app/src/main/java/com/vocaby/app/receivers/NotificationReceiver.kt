package com.vocaby.app.receivers

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.rxjava3.EmptyResultSetException
import com.vocaby.app.R
import com.vocaby.app.data.VocabyDatabase
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.ui.MainActivity
import com.vocaby.app.utils.Generators.generateRandomInt
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scope = CoroutineScope(Dispatchers.Main.immediate)
        val vocabyDao =
            VocabyDatabase.getDatabase(context.applicationContext, scope)
                .vocabyDao()
        val vocabyRepository = VocabyRepository(vocabyDao, context.applicationContext as Application)
        val sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE)

        scope.launch(CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is EmptyResultSetException -> {
                    val title = "No Saved Words"
                    val message = "Save words in the app to display in the notification"
                    createNotification(context, notificationManager, title, message)
                }

                else -> {}
            }
        }) {
            val saves = vocabyRepository.getSavedWords()

            if (saves.isEmpty()) {
                val editor = sp.edit()
                editor.putString("NOTIF_PREV_SELECT", "")
                editor.apply()
                throw EmptyResultSetException("User has no saves!")
            } else {
                var index = generateRandomInt(0, saves.size-1)
                val prevWord = sp.getString("NOTIF_PREV_SELECT", "")

                while (saves.size != 1 && saves[index] == prevWord) {
                    index = generateRandomInt(0, saves.size-1)
                }

                val entry = saves[index]
                val entryModel = vocabyRepository.getAllEntryData(entry)
                var message = "No definition found"

                entryModel?.let { model ->
                    val group = model.firstGroup
                    message = group.definitionData.get(0).toString()
                }

                sp.edit().putString("NOTIF_PREV_SELECT", entry).apply()
                createNotification(context, notificationManager, entry, message)
            }
        }
    }

    private fun createNotification(
        context: Context,
        notificationManager: NotificationManager,
        title: String,
        message: String
    ) {
        createNotificationChannel(notificationManager)
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val resultPendingIntent = PendingIntent.getActivity(
            context, 0,
            resultIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_white)
            .setColor(context.getColor(R.color.colorPrimaryAccent))
            .setContentTitle(title.uppercase())
            .setContentIntent(resultPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setContentText(message)
        notificationManager.notify(313, builder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val name: CharSequence = "Vocaby Notification"
        val description = "Vocaby Notification"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "VOCABY_CHANNEL"
    }
}