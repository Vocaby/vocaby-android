package com.vocaby.app.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.vocaby.app.R;
import com.vocaby.app.viewmodels.DataTransferViewModel;

public class DataTransferActivity extends AppCompatActivity {
    private DataTransferViewModel dataTransferViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_OK, dataTransferViewModel.addResult());
            finish();
        });

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView progressText = findViewById(R.id.progress_text);

        dataTransferViewModel = new ViewModelProvider(this).get(DataTransferViewModel.class);
        Intent directoryPickerIntent = dataTransferViewModel.handleReceived(getIntent());
        directorySelector.launch(directoryPickerIntent);

        dataTransferViewModel.getProgressText().observe(this, progressText::setText);
        dataTransferViewModel.getTransferStatus().observe(this, (successful) -> {
            progressBar.setIndeterminate(false);
            progressBar.setMax(1);
            progressBar.incrementProgressBy(1);
            if (!successful) progressBar.setProgressTintList(ColorStateList.valueOf(getColor(R.color.colorHeadline)));
        });
    }

    private final ActivityResultLauncher<Intent> directorySelector = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) dataTransferViewModel.handleResult(result.getData());
                else finish();
            }
    );
}