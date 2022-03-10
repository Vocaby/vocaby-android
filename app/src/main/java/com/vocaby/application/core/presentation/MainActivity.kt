package com.vocaby.application.core.presentation

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
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_profile.presentation.profile.ProfileViewModel
import com.vocaby.application.feature_profile.presentation.setting.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var navigationView: BottomNavigationView

    private val profileViewModel: ProfileViewModel by viewModels()
    private val dictionaryViewModel: DictionaryViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (profileViewModel.isReady
                        && dictionaryViewModel.dictionaryIsReady
                    ) {
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
        setupNavigation()
        collect()
    }

    private fun collect() {
        launchAndRepeatWithViewLifecycle {
            settingViewModel.dataSettings.collect { enabled ->
                if (enabled) Bugsnag.start(this@MainActivity)
            }
        }
    }

    private fun setupNavigation() {
        viewPager = findViewById(R.id.fragment_container)
        viewPager.adapter = FragmentAdapter(this)
        viewPager.isUserInputEnabled = false

        navigationView = findViewById(R.id.navigation_view)
        navigationView.bringToFront()
        navigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.profileFragment -> viewPager.setCurrentItem(2, false)
                R.id.savesFragment -> viewPager.setCurrentItem(0, false)
                else -> viewPager.setCurrentItem(1, false)
            }

            true
        }

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                navigationView.menu.getItem(position).isChecked = true
            }
        })

        viewPager.currentItem = 1
    }


    fun showDefinition(entry: String) {
        viewPager.currentItem = 1
        dictionaryViewModel.search(entry)
    }
}