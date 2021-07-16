package com.example.vocaby;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private HomeFragment home;
    private SavesFragment saves;
    private SettingsFragment settings;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = findViewById(R.id.navigation_view);
        drawer = findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(navListener);
        navigationView.bringToFront();
        DataManager dataManager = DataManager.getInstance(this);
        home = HomeFragment.newInstance(dataManager, "");
        saves = SavesFragment.newInstance(dataManager);
        settings = SettingsFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home, "search").commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(home.isResumed()) {
            home.resetSearch();
            navigationView.getMenu().findItem(R.id.search).setChecked(true);
        }
    }

    private NavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                drawer.closeDrawers();
                Fragment selected = null;
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if(id == R.id.search) {
                    selected = home;
                    home.resetSearch();
                } else if(id == R.id.saves) {
                    selected = saves;
                } else if(id == R.id.settings) {
                    selected = settings;
                }

                transaction.replace(R.id.fragment_container, selected).commit();
                return true;
            };
}