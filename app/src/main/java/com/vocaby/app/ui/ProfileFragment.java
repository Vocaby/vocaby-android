package com.vocaby.app.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vocaby.app.R;


public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.profile_fragment_container,
                    new ProfileHomeFragment()).commit();
        }

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}