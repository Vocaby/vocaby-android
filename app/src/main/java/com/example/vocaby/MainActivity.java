package com.example.vocaby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private EditText search;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        search = findViewById(R.id.search_bar);
        search.setOnFocusChangeListener(searchFocusListener);
        search.setOnEditorActionListener(searchEditorListener);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.search);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
    }

    private TextView.OnEditorActionListener searchEditorListener = (v, actionId, event) -> {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();

        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            if(search != null) {
                String word = search.getText().toString().trim();
                if(!word.isEmpty()) {
                    SearchResultsFragment fragment = SearchResultsFragment.newInstance(word);
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.addToBackStack(null);
                    transaction.add(R.id.fragment_container, fragment, "SEARCH_RESULTS_FRAGMENT").commit();
                    return true;
                }
            }
        };

        return false;
    };

    private View.OnFocusChangeListener searchFocusListener = (v, hasFocus) -> {
        if(!hasFocus) {
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    };

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                switch(item.getItemId()) {
                    case R.id.search:
                        return true;
                    case R.id.saves:
                        startActivity(new Intent(getApplicationContext(), SavesActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                }

                return true;
            };
}