package com.vocaby.application.feature_user.presentation.setting.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.rxjava3.EmptyResultSetException
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.util.Generators.generateRandomInt
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var dictionaryRepository: DictionaryRepository
    @Inject
    lateinit var customDictionaryRepository: CustomDictionaryRepository
    @Inject
    lateinit var userRepository: UserRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scope = CoroutineScope(Dispatchers.Main.immediate)
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
            val userId = userRepository.getUser()
            val saves = userRepository.getSavedWords(userId)

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
                val entryModel: EntryModel? =
                    customDictionaryRepository.getUserEntryData(userId, entry) ?:
                    dictionaryRepository.getEntryDataFromDatabase(entry)

                var message = "No definition found"

                entryModel?.let { model ->
                    val group = model.firstGroup
                    message = group.definitionData[0].toString()
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
            .setColor(context.getColor(R.color.colorPrimary))
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