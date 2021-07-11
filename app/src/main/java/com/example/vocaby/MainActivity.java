package com.example.vocaby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private HomeFragment home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        DataManager dataManager = DataManager.getInstance(this);
        home = HomeFragment.newInstance(dataManager);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home).commit();
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                Fragment selected = null;

                if(id == R.id.search) {
                    selected = home;
                } else if(id == R.id.saves) {
                    selected = new SavesFragment();
                } else if(id == R.id.profile) {
                    selected = new ProfileFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
                return true;
            };
}