package com.vocaby.application.core.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bugsnag.android.Bugsnag
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vocaby.application.R
import com.vocaby.application.core.presentation.adapter.FragmentAdapter
import com.vocaby.application.core.util.Logger
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_profile.presentation.profile.ProfileViewModel
import com.vocaby.application.feature_profile.presentation.setting.SettingViewModel
import com.vocaby.application.feature_profile.presentation.setting.receivers.NotificationReceiver
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {
    @Inject
    @Named("application")
    lateinit var applicationSharedPref: SharedPreferences

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notificationIntent: Intent
    private lateinit var viewPager: ViewPager2
    private lateinit var navigationView: BottomNavigationView

    private val dictionaryViewModel: DictionaryViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (profileViewModel.isReady) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cleanup()
        setupNotification()
        setupNavigation()
        collect()
    }

    private fun cleanup() {
        dictionaryViewModel.clearCache()
    }

    private fun collect() {
        launchAndRepeatWithViewLifecycle {
            val enabled = settingViewModel.dataSettings.first()
            if (enabled) Bugsnag.start(this@MainActivity)
        }

        launchAndRepeatWithViewLifecycle {
            settingViewModel.notificationSettings.collectLatest { model ->
                if (model.enabled) {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis(),
                        1000L * 60 * model.minutes,
                        pendingIntent
                    )
                } else {
                    alarmManager.cancel(pendingIntent)
                }
            }
        }
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
        // viewPager.offscreenPageLimit = 1
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


    fun showDefinition(entry: String) {
        viewPager.currentItem = 0
        dictionaryViewModel.search(entry)
    }
}