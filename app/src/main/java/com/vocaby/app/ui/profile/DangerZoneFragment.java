package com.vocaby.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vocaby.app.R;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.MyEntryViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

public class DangerZoneFragment extends Fragment {
    private MaterialAlertDialogBuilder builder;
    private UserViewModel userViewModel;
    private DictionaryViewModel dictionaryViewModel;
    private MyEntryViewModel myEntryViewModel;

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
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        myEntryViewModel = new ViewModelProvider(requireActivity()).get(MyEntryViewModel.class);

        View view = inflater.inflate(R.layout.fragment_profile_danger_zone, container, false);

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        Button clearHistoryButton = view.findViewById(R.id.clear_history_button);
        clearHistoryButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you wish to clear your search history?")
                    .setMessage("This action is irreversible.")
                    .setPositiveButton("CLEAR", (dialog, which) -> dictionaryViewModel.clearHistory())
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
                    .setPositiveButton("CLEAR", (dialog, which) -> myEntryViewModel.clearEntries())
                    .setNegativeButton("CANCEL", null);

            AlertDialog alert = builder.create();
            alert.show();
        });

        Button eraseDataButton = view.findViewById(R.id.erase_data_button);
        eraseDataButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you wish to erase your data?")
                    .setMessage("All of your data will be deleted. This action is irreversible.")
                    .setPositiveButton("ERASE", (dialog, which) -> {
                        dictionaryViewModel.clearHistory();
                        userViewModel.clearSaves();
                        myEntryViewModel.clearEntries();
                    }).setNegativeButton("CANCEL", null);

            AlertDialog alert = builder.create();
            alert.show();
        });

        return view;
    }
}