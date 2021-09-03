package com.vocaby.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.viewmodels.LoginViewModel;

public class LoginFragment extends Fragment {
    private Context ctx;
    private LoginViewModel loginViewModel;
    private TextView emailView;
    private EditText passwordView;
    private TextView emailAlertView;
    private TextView passwordAlertView;
    private TextView loginAlertView;
    private Button loginButton;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_login, container, false);
        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(backListener);

        emailView = view.findViewById(R.id.email_input);
        passwordView = view.findViewById(R.id.password_input);
        emailAlertView = view.findViewById(R.id.email_header_alert);
        passwordAlertView = view.findViewById(R.id.password_header_alert);
        loginAlertView = view.findViewById(R.id.login_alert);
        TextView signUpText = view.findViewById(R.id.signup);
        loginButton = view.findViewById(R.id.login_button);

        loginButton.setOnClickListener(loginListener);

        signUpText.setOnClickListener(v -> getParentFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_left_to_right,
                R.anim.enter_right_to_left,
                R.anim.exit_left_to_right
            )
            .addToBackStack(null)
            .add(R.id.login_fragment_container, new RegisterFragment())
            .commit());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getAuthModel().observe(getViewLifecycleOwner(), authModel ->  {
                if(authModel.emailIsEmpty()) {
                    emailAlertView.setText(getString(R.string.enter_email));
                } else if(!authModel.isEmail()) {
                    emailAlertView.setText(getString(R.string.enter_valid_email));
                } else {
                    emailAlertView.setText("");
                }

                if(authModel.passwordIsEmpty()) {
                    passwordAlertView.setText(getString(R.string.enter_password));
                } else {
                    passwordAlertView.setText("");
                }

                if(authModel.loginIsValid()) {
                    loginViewModel.login();
                    loginButton.setEnabled(false);
                }
        });

        loginViewModel.getLoginStatus().observe(getViewLifecycleOwner(), loginMessage -> {
            loginAlertView.setText(loginMessage);
            loginButton.setEnabled(true);
        });
    }

    private final View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            loginViewModel.setLoginData(email, password);
        }
    };

    private final View.OnClickListener backListener = v -> requireActivity().finish();
}