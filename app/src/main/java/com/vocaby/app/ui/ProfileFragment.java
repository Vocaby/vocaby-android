package com.vocaby.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.vocaby.app.R;

import com.vocaby.app.models.UserModel;
import com.vocaby.app.viewmodels.UserViewModel;

public class ProfileFragment extends Fragment {
    private Button loginoutButton;
    private UserViewModel userViewModel;
    private TextView currentUserName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        currentUserName = view.findViewById(R.id.current_user);

        ImageButton navButton = view.findViewById(R.id.settings_button);
        navButton.setOnClickListener(v -> {});

        loginoutButton = view.findViewById(R.id.loginout_button);

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        Observer<Boolean> loginObserver = loggedIn -> {
            if(loggedIn) {
                loginoutButton.setText(getString(R.string.log_out));
                loginoutButton.setOnClickListener(v -> userViewModel.logout());
            } else {
                loginoutButton.setText(getString(R.string.log_in));
                loginoutButton.setOnClickListener(v -> login());
            }
        };

        userViewModel.getLoginStatus().observe(getViewLifecycleOwner(), loginObserver);

        Observer<UserModel> userObserver = user -> currentUserName.setText(user.getEmail());

        userViewModel.getUser().observe(getViewLifecycleOwner(), userObserver);
    }

    private void login() {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        startActivity(intent);
    }
}