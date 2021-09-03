package com.vocaby.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vocaby.app.R;
import com.vocaby.app.viewmodels.LoginViewModel;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_fragment_container, new LoginFragment())
                .commit();

        LoginViewModel loginViewModel =
                new ViewModelProvider(this).get(LoginViewModel.class);

        loginViewModel.getLoginStatus().observe(this, loginSuccessful -> {
            if(loginSuccessful) {
                loginUser();;
            }
        });
    }

    public void loginUser() {
        Intent loginIntent = new Intent();
        loginIntent.putExtra("loginStatus", true);
        setResult(Activity.RESULT_OK, loginIntent);
        finish();
    }
}