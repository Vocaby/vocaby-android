package com.vocaby.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vocaby.app.R;


public class ProfileFragment extends Fragment {
    private OnBackPressedCallback onBackPressedCallback;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getChildFragmentManager().getBackStackEntryCount() > 0) onBackPressedCallback.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        onBackPressedCallback.setEnabled(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if(savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.profile_fragment_container,
                    new ProfileHomeFragment()).commit();
        }

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 0) getChildFragmentManager().popBackStack();
                if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), onBackPressedCallback);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}