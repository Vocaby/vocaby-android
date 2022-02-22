package com.vocaby.application.feature_profile.presentation.setting.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.util.Generators.generateRandomInt
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var dictionaryRepository: DictionaryRepository
    @Inject
    lateinit var customDictionaryRepository: CustomDictionaryRepository
    @Inject
    lateinit var saveRepository: SaveRepository
    @Inject
    lateinit var userRepository: UserRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val scope = CoroutineScope(Dispatchers.Main.immediate)
        val sp = context.getSharedPreferences("SAVES", Context.MODE_PRIVATE)

        scope.launch(Dispatchers.Default) {
            val userId = userRepository.getUser()
            val settings = userRepository.settingsFlow.first()
            val selectedCollectionId = settings.notificationCollectionId
            val collection = saveRepository.getSaveCollectionWithId(selectedCollectionId)
            var isFromCollection = true
            var saves = saveRepository.getCollectionItems(selectedCollectionId).map { it.entry }
            if (collection == null || selectedCollectionId < 1) {
                isFromCollection = false
                saves = saveRepository.getAllSavedEntriesFlow(userId).first()
            }

            if (saves.isEmpty()) {
                val editor = sp.edit()
                editor.putString("NOTIF_PREV_SELECT", "")
                editor.apply()

                val title = "No Saved Words"
                val message = "Save words in the app to display in the notification"
                createNotification(
                    context,
                    notificationManager,
                    title,
                    isFromCollection,
                    collection,
                    message
                )
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
                    model.firstGroup?.let {
                        message = it.definitionData[0].definition
                    }
                }

                sp.edit().putString("NOTIF_PREV_SELECT", entry).apply()
                createNotification(
                    context,
                    notificationManager,
                    entry,
                    isFromCollection,
                    collection,
                    message
                )
            }
        }
    }

    private fun createNotification(
        context: Context,
        notificationManager: NotificationManager,
        title: String,
        isFromCollection: Boolean,
        collection: SaveCollectionModel?,
        message: String
    ) {
        createNotificationChannel(notificationManager)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val resultPendingIntent = PendingIntent.getActivity(
            context, 0,
            resultIntent, PendingIntent.FLAG_IMMUTABLE
        )

        if (isFromCollection && collection != null) {
            builder.setSubText(collection.collectionName)
        }

        builder.setSmallIcon(R.drawable.ic_notification_white)
            .setColor(context.getColor(R.color.colorPrimary))
            .setContentTitle(title)
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