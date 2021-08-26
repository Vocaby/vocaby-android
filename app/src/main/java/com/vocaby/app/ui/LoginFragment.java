package com.vocaby.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.api.RequestManager;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.viewmodels.LoginViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                Log.d("AuthModel", "Changed");

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

        loginViewModel.getLoginStatus().observe(getViewLifecycleOwner(), loginSuccessful -> {
            if(loginSuccessful) {
                MainActivity.loginUser();
                requireActivity().finish();
            } else {
                loginAlertView.setText(getString(R.string.wrong_credentials_desc));
                loginButton.setEnabled(true);
            }
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