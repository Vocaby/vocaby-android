package com.vocaby.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SuccessfulCreationFragment extends Fragment {

    public SuccessfulCreationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_successful_creation, container, false);
        Button continueButton = view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(v -> {
            FragmentManager fm = getParentFragmentManager();
            fm.popBackStack();
            fm.beginTransaction()
            .setCustomAnimations(
                    0,
                    R.anim.exit_left_to_right,
                    0,
                    R.anim.exit_left_to_right
            )
            .replace(R.id.fragment_container, new LoginFragment())
            .commit();
        });

        return view;
    }
}