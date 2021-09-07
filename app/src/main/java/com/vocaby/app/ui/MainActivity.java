package com.vocaby.app.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bugsnag.android.Bugsnag;
import com.google.android.material.navigation.NavigationView;
import com.vocaby.app.receivers.NotificationReceiver;
import com.vocaby.app.adapters.FragmentAdapter;
import com.vocaby.app.R;
import com.vocaby.app.viewmodels.UserViewModel;

public class MainActivity extends AppCompatActivity {
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private ViewPager2 viewPager;
    private SharedPreferences sharedPreferences;
    private NavigationView navigationView;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bugsnag.start(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setupApplication();

        setupNotification();
        setupNavigation();

        View headerView = navigationView.getHeaderView(0);
        TextView firstName = headerView.findViewById(R.id.first_name);
        TextView currentUser = headerView.findViewById(R.id.current_user);
        Button loginoutButton = findViewById(R.id.loginout_button);


        userViewModel.getUser().observe(this, user -> {
            firstName.setText(user.getFirstName());
            currentUser.setText(user.getEmail());

            if(user.isLoggedIn()) {
                loginoutButton.setText(getString(R.string.log_out));
                loginoutButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.color_tertiary)));
                loginoutButton.setOnClickListener(v -> userViewModel.logout());
            } else {
                loginoutButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                loginoutButton.setText(getString(R.string.log_in));
                loginoutButton.setOnClickListener(v -> login());
            }
        });
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
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 777, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setupNavigation() {
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setAdapter(new FragmentAdapter(this));
        viewPager.setUserInputEnabled(false);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.bringToFront();
        DrawerLayout drawer = findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawer(GravityCompat.END);
            int current = item.getItemId();
            if(current == R.id.profileFragment) {
                viewPager.setCurrentItem(2, false);
            } else if(current == R.id.savesFragment) {
                viewPager.setCurrentItem(1, false);
            } else {
                viewPager.setCurrentItem(0, false);
            }

            return true;
        }) ;

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                navigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private void login() {
        mGetLogin.launch(new Intent(this, AuthActivity.class));
    }

    private final ActivityResultLauncher<Intent> mGetLogin = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> userViewModel.handleActivityResult(result)
    );

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

    public void showDefinition(String word) {
        viewPager.setCurrentItem(0);
        DictionaryFragment dictionaryFragment = (DictionaryFragment) getSupportFragmentManager()
                .findFragmentByTag("f0");

        if(dictionaryFragment != null) {
            dictionaryFragment.addResultsFragment(word, true);
        }
    }
}