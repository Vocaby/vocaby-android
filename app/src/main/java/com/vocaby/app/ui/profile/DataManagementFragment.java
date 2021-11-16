package com.vocaby.app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vocaby.app.R;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.viewmodels.UserViewModel;

import static com.vocaby.app.viewmodels.DataTransferViewModel.EXPORT_SAVE;
import static com.vocaby.app.viewmodels.DataTransferViewModel.EXPORT_SAVE_BACKUP;
import static com.vocaby.app.viewmodels.DataTransferViewModel.IMPORT_ENTRY;
import static com.vocaby.app.viewmodels.DataTransferViewModel.IMPORT_SAVE;

public class DataManagementFragment extends Fragment {
    private UserViewModel userViewModel;

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

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        Intent startDataTransferActivity = new Intent(requireActivity(), DataTransferActivity.class);

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        Button exportSave = view.findViewById(R.id.export_save_button);
        exportSave.setOnClickListener(v -> {
            startDataTransferActivity.putExtra("TYPE", EXPORT_SAVE);
            dataTransferActivity.launch(startDataTransferActivity);
        });

        Button exportSaveForBackup = view.findViewById(R.id.export_save_backup_button);
        exportSaveForBackup.setOnClickListener(v -> {
            startDataTransferActivity.putExtra("TYPE", EXPORT_SAVE_BACKUP);
            dataTransferActivity.launch(startDataTransferActivity);
        });

        Button importSave = view.findViewById(R.id.import_saves_button);
        importSave.setOnClickListener(v -> {
            startDataTransferActivity.putExtra("TYPE", IMPORT_SAVE);
            dataTransferActivity.launch(startDataTransferActivity);
        });

        return view;
    }

    private final ActivityResultLauncher<Intent> dataTransferActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    switch (result.getData().getIntExtra("TYPE", -1)) {
                        case IMPORT_SAVE:
                            userViewModel.resetSaves();
                            break;
                        case IMPORT_ENTRY:
                            break;
                    }
                } else {
                    Logger.reportToDebug("CANCELED");
                }
            }
    );
}