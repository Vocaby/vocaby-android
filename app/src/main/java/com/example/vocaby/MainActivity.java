package com.example.vocaby;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private HomeFragment home;
    private SavesFragment saves;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        drawer = findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(navListener);
        navigationView.bringToFront();
        DataManager dataManager = DataManager.getInstance(this);
        home = HomeFragment.newInstance(dataManager, "");
        saves = SavesFragment.newInstance(dataManager);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        home.resetSearch();
    }

    private NavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                int id = item.getItemId();
                drawer.closeDrawers();
                Fragment selected = null;
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction transaction = fm.beginTransaction();

                if(id == R.id.search) {
                    selected = home;
                    home.resetSearch();
                } else if(id == R.id.saves) {
                    selected = saves;
                } else if(id == R.id.settings) {
                    selected = new SettingsFragment();
                }


                transaction.replace(R.id.fragment_container, selected).commit();
                return true;
            };
}