package com.vocaby.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bugsnag.android.Bugsnag;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vocaby.app.FragmentAdapter;
import com.vocaby.app.NotificationReceiver;
import com.vocaby.app.R;
import com.vocaby.app.database.DatabaseManager;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.viewmodels.UserViewModel;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private DatabaseManager databaseManager;
    private static UserViewModel userViewModel;
    private ViewPager2 viewPager;

    @Override
    protected void onResume() {
        super.onResume();
        updateRandomWordIndex();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bugsnag.start(this);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 777, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        databaseManager = DatabaseManager.getInstance(getApplicationContext());
        databaseManager.openDatabase();

        setupNavigation();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateNotificationSettings(sharedPreferences, getString(R.string.pref_notification_key));

        updateRandomWordIndex();
    }

    public static void loginUser() {
        userViewModel.loginUser();
    }

    private void setupNavigation() {
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setAdapter(new FragmentAdapter(this));
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int current = item.getItemId();
            if(current == R.id.profileFragment) {
                viewPager.setCurrentItem(2);
            } else if(current == R.id.savesFragment) {
                viewPager.setCurrentItem(1);
            } else {
                viewPager.setCurrentItem(0);
            }

            return true;
        }) ;

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });
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
            editor.apply();
        }
    }

    private void updateNotificationSettings(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences.getBoolean(key, false)) {
            int minutes = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_notification_frequency_key), "5"));
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000L * 60 * minutes, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }

    public void showDefinition(String word) {
        viewPager.setCurrentItem(0);
        DictionaryFragment dictionaryFragment = (DictionaryFragment) getSupportFragmentManager().findFragmentByTag("f0");
        dictionaryFragment.addResultsFragment(word, true);
    }
}