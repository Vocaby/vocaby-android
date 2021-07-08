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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                Fragment selected = null;

                if(id == R.id.search) {
                    selected = new HomeFragment();
                } else if(id == R.id.saves) {
                    selected = new SavesFragment();
                } else if(id == R.id.profile) {
                    selected = new ProfileFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
                return true;
            };
}