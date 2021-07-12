package com.example.vocaby;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private HomeFragment home;
    private SavesFragment saves;
    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        DataManager dataManager = DataManager.getInstance(this);
        searchAdapter = new SearchAdapter(MainActivity.this, dataManager.getHistory());
        home = HomeFragment.newInstance(dataManager, "");
        saves = SavesFragment.newInstance(dataManager);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        home.resetSearch();
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                Fragment selected = null;
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction transaction = fm.beginTransaction();

                if(id == R.id.search) {
                    selected = home;
                    home.resetSearch();
                } else if(id == R.id.saves) {
                    selected = saves;
                } else if(id == R.id.profile) {
                    selected = new ProfileFragment();
                }


                transaction.replace(R.id.fragment_container, selected).commit();
                return true;
            };
}