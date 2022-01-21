package com.vocaby.application.feature_profile.presentation.setting.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.vocaby.application.R

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                777,
                Intent(context, NotificationReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            if (sharedPreferences.getBoolean(
                    context.getString(R.string.pref_notification_key),
                    false
                )
            ) {
                val minutes = sharedPreferences.getString(
                    context.getString(R.string.pref_notification_frequency_key),
                    "15"
                )!!
                    .toInt()
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    1000L * 60 * minutes,
                    pendingIntent
                )
            }
        }
    }
}