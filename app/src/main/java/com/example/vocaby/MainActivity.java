package com.example.vocaby;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnItemSelectedListener(navListener);
        navigationView.bringToFront();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(""), "search").commit();

        Intent notificationIntent = new Intent(this, NotificationReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 777, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 60 * 5, pendingIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // navigationView.getMenu().findItem(R.id.search).setChecked(true);
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

                transaction.replace(R.id.fragment_container, selected).commit();
                return true;
            };
}