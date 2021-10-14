package com.vocaby.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vocaby.app.R;

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