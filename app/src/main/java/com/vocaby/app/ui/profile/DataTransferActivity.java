package com.vocaby.app.ui.profile;

import static com.vocaby.app.ui.profile.DataManagementFragment.EXPORT_SAVE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.utils.LiveDataUtil;
import com.vocaby.app.viewmodels.DataTransferViewModel;

import java.util.Locale;

public class DataTransferActivity extends AppCompatActivity {
    private DataTransferViewModel dataTransferViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> finish());

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView progressMaxText = findViewById(R.id.progress_counter_max);
        TextView progressText = findViewById(R.id.progress_text);

        Intent directoryPickerIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        directoryPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        directoryPickerIntent.setType("text/plain");
        directoryPickerIntent.putExtra(Intent.EXTRA_TITLE, "vocaby_saves.txt");
        directorySelector.launch(directoryPickerIntent);

        dataTransferViewModel = new ViewModelProvider(this).get(DataTransferViewModel.class);
        dataTransferViewModel.setActionType(getIntent().getIntExtra("TYPE", -1));


        LiveDataUtil.observeOnce(dataTransferViewModel.getProgressMax(), max -> {
            progressBar.setMax(2);
            progressMaxText.setText(String.format(Locale.US, "%d", max));
        });

        dataTransferViewModel.getProgressText().observe(this, progressText::setText);
        dataTransferViewModel.getProgressIncrement().observe(this, (successful) -> {
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