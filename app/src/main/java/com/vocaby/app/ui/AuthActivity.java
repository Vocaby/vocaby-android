package com.vocaby.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.vocaby.app.R;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_fragment_container, new LoginFragment())
                .commit();
    }
}