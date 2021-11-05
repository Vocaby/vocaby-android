package com.vocaby.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.vocaby.app.R;

public class DataManagementFragment extends Fragment {
    public static final int EXPORT_SAVE = 0;

    public DataManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_data_management, container, false);

        Intent startDataTransferActivity = new Intent(requireActivity(), DataTransferActivity.class);

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());


        Button exportSave = view.findViewById(R.id.export_save_button);
        exportSave.setOnClickListener(v -> {
            startDataTransferActivity.putExtra("TYPE", EXPORT_SAVE);
            requireActivity().startActivity(startDataTransferActivity);
        });
        return view;
    }
}