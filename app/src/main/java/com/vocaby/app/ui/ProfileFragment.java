package com.vocaby.app.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.vocaby.app.R;
import com.vocaby.app.api.RequestManager;
import com.vocaby.app.models.User;
import com.vocaby.app.viewmodels.UserViewModel;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ProfileFragment extends Fragment {
    private Context ctx;
    private Button loginoutButton;
    private UserViewModel userViewModel;
    private TextView currentUserName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ctx = requireActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        currentUserName = view.findViewById(R.id.current_user);

        ImageButton navButton = view.findViewById(R.id.settings_button);
        navButton.setOnClickListener(v -> ((MainActivity)requireActivity()).showSettings());

        loginoutButton = view.findViewById(R.id.loginout_button);

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        Observer<User> userObserver = user -> {
            currentUserName.setText(user.getUsername());

            if(user.isLoggedIn()) {
                loginoutButton.setText(getString(R.string.log_out));
                loginoutButton.setOnClickListener(v -> logout());
            } else {
                loginoutButton.setText(getString(R.string.log_in));
                loginoutButton.setOnClickListener(v -> login());
            }
        };

        userViewModel.getUser().observe(getViewLifecycleOwner(), userObserver);
    }

    private void logout() {
        RequestManager requestManager = RequestManager.getInstance(ctx);
        requestManager.makeLogoutRequest(logoutListenerResponse, logoutListenerError);
    }

    private void login() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private final Response.Listener<JSONObject> logoutListenerResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.token_key), null);
            editor.apply();
            FragmentManager fm = getParentFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        }
    };

    private final Response.ErrorListener logoutListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Toast.makeText(ctx, body, Toast.LENGTH_SHORT).show();
            }

            loginoutButton.setEnabled(true);
        }
    };
}