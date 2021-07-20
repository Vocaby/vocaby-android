package com.example.vocaby;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private BottomNavigationView navigationView;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnItemSelectedListener(navListener);
        navigationView.bringToFront();

        String notifiedWord = getIntent().getStringExtra("com.example.vocaby.notification");
        if(notifiedWord != null) {
            if(!notifiedWord.equals("No Saved Words")) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment.newInstance(notifiedWord), "HOME")
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment.newInstance(""), "HOME")
                        .commit();
            }
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment.newInstance(""), "HOME")
                    .commit();
        }


        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReciever.class);
        pendingIntent = PendingIntent.getBroadcast(this, 777, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateNotificationSettings(sharedPreferences, getString(R.string.pref_notification_key));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // navigationView.getMenu().findItem(R.id.search).setChecked(true);
    }

    public void showSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                Fragment selected = null;
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if(id == R.id.search) {
                    selected = HomeFragment.newInstance("");
                    transaction.replace(R.id.fragment_container, selected, "HOME").commit();
                    return true;
                } else if(id == R.id.saves) {
                    selected = new SavesFragment();
                } else if(id == R.id.settings) {
                    selected = new ProfileFragment();
                }

                assert selected != null;
                transaction.replace(R.id.fragment_container, selected).commit();
                return true;
            };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_notification_key))) {
            updateNotificationSettings(sharedPreferences, key);
        } else if(key.equals(getString(R.string.pref_notification_frequency_key))) {
            updateNotificationSettings(sharedPreferences, getString(R.string.pref_notification_key));
        }
    }

    private void updateNotificationSettings(SharedPreferences sharedPreferences, String key) {
        alarmManager.cancel(pendingIntent);
        if(sharedPreferences.getBoolean(key, false)) {
            int minutes = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5"));
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 60 * minutes, pendingIntent);
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}