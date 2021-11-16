package com.vocaby.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.viewpager2.widget.ViewPager2
import android.content.SharedPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vocaby.app.viewmodels.DictionaryViewModel
import android.os.Bundle
import com.vocaby.app.R
import com.bugsnag.android.Bugsnag
import com.vocaby.app.viewmodels.UserViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.app.NotificationManager
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.vocaby.app.adapters.FragmentAdapter
import com.vocaby.app.receivers.NotificationReceiver

open class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationIntent: Intent
    private lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationView: BottomNavigationView
    private lateinit var dictionaryViewModel: DictionaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Bugsnag.start(this)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.setupApplication()
        dictionaryViewModel = ViewModelProvider(this)[DictionaryViewModel::class.java]
        dictionaryViewModel.setupDictionaryEntries()

        setupNotification()
        setupNavigation()
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(mPrefsListener)
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefsListener)
    }

    private fun setupNotification() {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        notificationIntent = Intent(this, NotificationReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
                this,
                777,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun setupNavigation() {
        viewPager = findViewById(R.id.fragment_container)
        viewPager.adapter = FragmentAdapter(this)
        viewPager.isUserInputEnabled = false
        viewPager.offscreenPageLimit = 1
        navigationView = findViewById(R.id.navigation_view)
        navigationView.bringToFront()
        navigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.profileFragment -> viewPager.setCurrentItem(3, false)
                R.id.customEntryFragment -> viewPager.setCurrentItem(2, false)
                R.id.savesFragment -> viewPager.setCurrentItem(1, false)
                else -> viewPager.setCurrentItem(0, false)
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                navigationView.menu.getItem(position).isChecked = true
            }
        })
    }

    private val mPrefsListener = OnSharedPreferenceChangeListener {
        sharedPreferences: SharedPreferences, key: String ->
        when (key) {
            getString(R.string.pref_notification_key) -> updateNotificationStatus(sharedPreferences, key)
            getString(R.string.pref_notification_frequency_key) ->
                updateNotificationFrequency(sharedPreferences, getString(R.string.pref_notification_key))
        }
    }

    private fun updateNotificationStatus(sharedPreferences: SharedPreferences, key: String) {
        if (sharedPreferences.getBoolean(key, false)) {
            sendBroadcast(notificationIntent)
            val minutes = sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5")!!.toInt()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000L * 60 * minutes, pendingIntent)
        } else {
            alarmManager.cancel(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(313)
        }
    }

    private fun updateNotificationFrequency(sharedPreferences: SharedPreferences, key: String) {
        if (sharedPreferences.getBoolean(key, false)) {
            alarmManager.cancel(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(313)
            val minutes = sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5")!!.toInt()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000L * 60 * minutes, pendingIntent)
        }
    }

    fun showDefinition(entry: String?) {
        viewPager.currentItem = 0
        dictionaryViewModel.setSearch(entry)
    }
}