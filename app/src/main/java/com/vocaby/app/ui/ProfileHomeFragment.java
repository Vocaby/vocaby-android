package com.vocaby.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.vocaby.app.R;
import com.vocaby.app.viewmodels.UserViewModel;

public class ProfileHomeFragment extends Fragment {
    public ProfileHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_main, container, false);

        // Navigation
        ImageButton navButton = view.findViewById(R.id.nav_button);
        navButton.setOnClickListener(v -> {
            DrawerLayout drawer = requireActivity().findViewById(R.id.drawer);
            drawer.openDrawer(GravityCompat.END);
        });

        Button notificationButton = view.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(v -> getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.enter_right_to_left,
                        R.anim.exit_right_to_left,
                        R.anim.enter_right_to_left,
                        R.anim.exit_left_to_right
                )
                .addToBackStack(null)
                .add(R.id.profile_fragment_container, new NotificationFragment())
                .commit());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}