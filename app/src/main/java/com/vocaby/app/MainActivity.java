package com.vocaby.app;

import androidx.appcompat.app.AppCompatActivity;
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
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bugsnag.android.Bugsnag;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vocaby.app.database.DatabaseManager;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private DatabaseManager databaseManager;
    private int prevPage;

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
    protected void onResume() {
        super.onResume();
        updateRandomWordIndex();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bugsnag.start(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 777, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        databaseManager = DatabaseManager.getInstance(getApplicationContext());
        databaseManager.openDatabase();

        prevPage = R.id.search;
        setContentView(R.layout.activity_main);
        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnItemSelectedListener(navListener);
        navigationView.bringToFront();

        updateRandomWordIndex();

        // Notification
        String notifiedWord = getIntent().getStringExtra("com.vocaby.app.openAndSearch");
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

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            updateNotificationSettings(sharedPreferences, getString(R.string.pref_notification_key));
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment frag : fm.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    public void changePrevPage(int id) {
        prevPage = id;
    }

    public void showSettings() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.enter_right_to_left,
                        R.anim.exit_right_to_left,
                        R.anim.enter_right_to_left,
                        R.anim.exit_left_to_right
                )
                .addToBackStack(null)
                .add(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                Fragment selected = null;
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                if(prevPage != id) {
                    if(id == R.id.search) {
                        selected = HomeFragment.newInstance("");
                        transaction.setCustomAnimations(
                                R.anim.enter_left_to_right,
                                R.anim.exit_left_to_right
                        );
                        transaction.replace(R.id.fragment_container, selected, "HOME").commit();
                        prevPage = id;
                        return true;
                    } else if(id == R.id.saves) {
                        if(prevPage == R.id.profile) {
                            transaction.setCustomAnimations(
                                    R.anim.enter_left_to_right,
                                    R.anim.exit_left_to_right
                            );
                        } else {
                            transaction.setCustomAnimations(
                                    R.anim.enter_right_to_left,
                                    R.anim.exit_right_to_left
                            );
                        }

                        selected = new SavesFragment();
                    } else if(id == R.id.profile) {
                        transaction.setCustomAnimations(
                                R.anim.enter_right_to_left,
                                R.anim.exit_right_to_left
                        );
                        selected = new ProfileFragment();
                    }

                    prevPage = id;
                    assert selected != null;
                    transaction.replace(R.id.fragment_container, selected).commit();
                    return true;
                }

                return true;
            };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.closeDatabase();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_notification_key))) {
            updateNotificationSettings(sharedPreferences, key);
        } else if(key.equals(getString(R.string.pref_notification_frequency_key))) {
            updateNotificationSettings(sharedPreferences, getString(R.string.pref_notification_key));
        }
    }

    private void updateRandomWordIndex() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int lastTimeStarted = settings.getInt("appStarted", -1);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);

        if (today != lastTimeStarted) {
            SharedPreferences randomWord = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = randomWord.edit();
            int index = databaseManager.getRandomWordIndex();
            editor.putInt("randomWordIndex", index);
            editor.apply();

            editor = settings.edit();
            editor.putInt("appStarted", today);
            editor.commit();
        }
    }

    private void updateNotificationSettings(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences.getBoolean(key, false)) {
            int minutes = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5"));
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 60 * minutes, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}