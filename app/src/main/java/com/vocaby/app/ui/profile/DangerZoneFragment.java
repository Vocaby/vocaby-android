package com.vocaby.app.ui.profile;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vocaby.app.R;
import com.vocaby.app.viewmodels.DangerZoneViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

public class DangerZoneFragment extends Fragment {
    private MaterialAlertDialogBuilder builder;
    private DangerZoneViewModel dangerZoneViewModel;
    private UserViewModel userViewModel;

    public DangerZoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        builder = new MaterialAlertDialogBuilder(requireActivity());
        dangerZoneViewModel = new ViewModelProvider(this).get(DangerZoneViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        View view = inflater.inflate(R.layout.fragment_profile_danger_zone, container, false);

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        Button clearHistoryButton = view.findViewById(R.id.clear_history_button);
        clearHistoryButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you wish to clear your search history?")
                    .setMessage("This action is irreversible.")
                    .setPositiveButton("CLEAR", (dialog, which) ->
                            Log.d("Vocabydebug", "onCreateView: clear"))
                    .setNegativeButton("CANCEL", null);

            AlertDialog alert = builder.create();
            alert.show();
        });

        Button clearSavesButton = view.findViewById(R.id.clear_saves_button);
        clearSavesButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you wish to clear your saves?")
                    .setMessage("This action is irreversible.")
                    .setPositiveButton("CLEAR", (dialog, which) -> userViewModel.clearSaves())
                    .setNegativeButton("CANCEL", null);

            AlertDialog alert = builder.create();
            alert.show();
        });

        Button clearEntriesButton = view.findViewById(R.id.clear_custom_entries_button);
        clearEntriesButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you wish to clear your entries?")
                    .setMessage("This action is irreversible.")
                    .setPositiveButton("CLEAR", (dialog, which) ->
                            Log.d("Vocabydebug", "onCreateView: clear"))
                    .setNegativeButton("CANCEL", null);

            AlertDialog alert = builder.create();
            alert.show();
        });

        Button eraseDataButton = view.findViewById(R.id.erase_data_button);
        eraseDataButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you wish to erase your data?")
                    .setMessage("All of your data will be deleted. This action is irreversible.")
                    .setPositiveButton("ERASE", (dialog, which) ->
                            Log.d("Vocabydebug", "onCreateView: clear"))
                    .setNegativeButton("CANCEL", null);

            AlertDialog alert = builder.create();
            alert.show();
        });

        return view;
    }
}