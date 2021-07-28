package com.vocaby.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoginFragment extends Fragment {
    private Context ctx;

    public LoginFragment() {
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        TextView signUpText = view.findViewById(R.id.signup);
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.enter_right_to_left,
                            R.anim.exit_left_to_right,
                            R.anim.enter_right_to_left,
                            R.anim.exit_left_to_right
                    )
                    .addToBackStack("login")
                    .add(R.id.fragment_container, new RegisterFragment(), "registration")
                    .commit();
            }
        });

        return view;
    }
}