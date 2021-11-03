package com.vocaby.app.ui;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bugsnag.android.Bugsnag;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vocaby.app.R;
import com.vocaby.app.adapters.FragmentAdapter;
import com.vocaby.app.receivers.NotificationReceiver;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

public class MainActivity extends AppCompatActivity {
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent notificationIntent;
    private ViewPager2 viewPager;
    private SharedPreferences sharedPreferences;
    private BottomNavigationView navigationView;
    private DictionaryViewModel dictionaryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bugsnag.start(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setupApplication();

        dictionaryViewModel = new ViewModelProvider(this).get(DictionaryViewModel.class);
        dictionaryViewModel.setupDictionaryEntries();

        setupNotification();
        setupNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences.registerOnSharedPreferenceChangeListener(mPrefsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }

    private void setupNotification() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationIntent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 777, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setupNavigation() {
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setAdapter(new FragmentAdapter(this));
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(1);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.bringToFront();
        navigationView.setOnItemSelectedListener(item -> {
            int current = item.getItemId();
            if(current == R.id.profileFragment) {
                viewPager.setCurrentItem(3, false);
            } else if(current == R.id.customEntryFragment) {
                viewPager.setCurrentItem(2, false);
            } else if(current == R.id.savesFragment) {
                viewPager.setCurrentItem(1, false);
            } else {
                viewPager.setCurrentItem(0, false);
            }

            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                navigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener =
        (sharedPreferences, key) -> {
            if(key.equals(getString(R.string.pref_notification_key))) {
                updateNotificationStatus(sharedPreferences, key);
            } else if(key.equals(getString(R.string.pref_notification_frequency_key))) {
                updateNotificationFrequency(sharedPreferences, getString(R.string.pref_notification_key));
            }
    };

    private void updateNotificationStatus(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences.getBoolean(key, false)) {
            sendBroadcast(notificationIntent);
            int minutes = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5"));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000L * 60 * minutes, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(313);
        }
    }

    private void updateNotificationFrequency(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences.getBoolean(key, false)) {
            alarmManager.cancel(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(313);

            int minutes = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5"));
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000L * 60 * minutes, pendingIntent);
        }
    }

    public void showDefinition(String entry) {
        viewPager.setCurrentItem(0);
        dictionaryViewModel.setSearch(entry);
    }
}