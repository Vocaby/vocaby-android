package com.vocaby.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bugsnag.android.Bugsnag
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.adapters.FragmentAdapter
import com.vocaby.app.receivers.NotificationReceiver
import com.vocaby.app.viewmodels.DictionaryViewModel
import com.vocaby.app.viewmodels.DictionaryViewModelFactory
import com.vocaby.app.viewmodels.UserViewModel
import com.vocaby.app.viewmodels.UserViewModelFactory

open class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationIntent: Intent
    private lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationView: BottomNavigationView

    private val dictionaryViewModel: DictionaryViewModel by viewModels{
        DictionaryViewModelFactory((application as VocabyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val userViewModel: UserViewModel by viewModels {
            UserViewModelFactory((application as VocabyApplication).repository)
        }
        userViewModel.setupUser()

        if (userViewModel.isDataShareEnabled()) Bugsnag.start(this)
        dictionaryViewModel.clearCache()

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
            val minutes = sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "15")!!.toInt()
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                1000L * 60 * minutes,
                pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun updateNotificationFrequency(sharedPreferences: SharedPreferences, key: String) {
        if (sharedPreferences.getBoolean(key, false)) {
            val minutes = sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "15")!!.toInt()
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                1000L * 60 * minutes,
                pendingIntent
            )
        }
    }


    fun showDefinition(entry: String) {
        viewPager.currentItem = 0
        dictionaryViewModel.search(entry)
    }
}