package com.vocaby.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.vocaby.app.R;

public class MyEntryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MyEntryFragment() {
        // Required empty public constructor
    }

    public static MyEntryFragment newInstance(String param1, String param2) {
        MyEntryFragment fragment = new MyEntryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_entry, container, false);

        // Navigation
        ImageButton navButton = view.findViewById(R.id.nav_button);
        navButton.setOnClickListener(v -> {
            DrawerLayout drawer = requireActivity().findViewById(R.id.main_drawer);
            drawer.openDrawer(GravityCompat.END);
        });

        // Add Entry
        Button addButton = view.findViewById(R.id.add_entry_button);
        addButton.setOnClickListener(v -> {
            Intent startEntryBuilderIntent = new Intent(requireActivity(), EntryBuilderActivity.class);
            requireActivity().startActivity(startEntryBuilderIntent);
        });

        return view;
    }
}