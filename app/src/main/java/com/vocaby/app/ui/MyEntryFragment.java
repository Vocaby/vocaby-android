package com.vocaby.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.Constants;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomEntryAdapter;
import com.vocaby.app.models.CustomEntryPackage;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.MyEntryViewModel;

public class MyEntryFragment extends Fragment implements CustomEntryAdapter.ItemTouchListener {
    private Context ctx;

    private BottomSheetDialog entryEditDialog;
    private CustomEntryAdapter customEntryAdapter;
    private TextView entryCountView;
    private EditText entryEdit;
    private TextView entryAlert;
    private MyEntryViewModel entryViewModel;
    private DictionaryViewModel dictionaryViewModel;
    private LinearLayout emptyCard;

    public MyEntryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_entry, container, false);
        emptyCard = view.findViewById(R.id.empty_card);

        setupEntryBuilder();

        // Add Entry
        Button addButton = view.findViewById(R.id.add_entry_button);
        addButton.setOnClickListener(v -> {
            if (entryEdit != null && entryAlert != null) {
                    entryEdit.getText().clear();
                    entryAlert.setVisibility(View.INVISIBLE);
                    entryEditDialog.dismiss();
            }

            entryEditDialog.show();
        });

        entryCountView = view.findViewById(R.id.entry_count);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        entryViewModel = new ViewModelProvider(requireActivity()).get(MyEntryViewModel.class);
        setupRecyclerView(view);

        entryViewModel.getEntries().observe(getViewLifecycleOwner(), customEntries -> {
            if (customEntries.size() == 0) emptyCard.setVisibility(View.VISIBLE);
            else emptyCard.setVisibility(View.GONE);

            customEntryAdapter.setList(customEntries);
        });

        entryViewModel.getCustomEntryCount().observe(getViewLifecycleOwner(), count ->
                entryCountView.setText(StringFormatter.cleanNumber(count))
        );
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.custom_entry_container);
        customEntryAdapter = new CustomEntryAdapter(this);
        recyclerView.setAdapter(customEntryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
    }

    private void setupEntryBuilder() {
        entryEditDialog =
                new BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog);
        entryEditDialog.setContentView(R.layout.custom_entry_header_dialog);

        Button button = entryEditDialog.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> entryEditDialog.dismiss());
        }

        Button saveButton = entryEditDialog.findViewById(R.id.dialog_save_button);
        entryEdit = entryEditDialog.findViewById(R.id.entry_edit);
        entryAlert = entryEditDialog.findViewById(R.id.entry_header_alert);
        if (saveButton != null) {
            saveButton.setText(R.string.create);
            saveButton.setOnClickListener(v -> {
                if (entryEdit != null && entryAlert != null) {
                    String entry = StringFormatter.cleanText(entryEdit.getText().toString());
                    if (entry.isEmpty()) {
                        entryAlert.setVisibility(View.VISIBLE);
                    } else {
                        entryEdit.getText().clear();
                        entryAlert.setVisibility(View.INVISIBLE);
                        entryEditDialog.dismiss();


                        Intent startEntryBuilderIntent = new Intent(requireActivity(), EntryBuilderActivity.class);
                        startEntryBuilderIntent = entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, -1);
                        entryBuilderActivity.launch(startEntryBuilderIntent);
                    }
                }
            });
        }
    }

    private final ActivityResultLauncher<Intent> entryBuilderActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                    CustomEntryPackage customEntryPackage =
                            result.getData().getParcelableExtra(MyEntryViewModel.CUSTOM_ENTRY_PACKAGE_KEY);
                    if(customEntryPackage.isDelete()) {
                        customEntryAdapter.deleteEntry(customEntryPackage.getEntry());
                    } else {
                        if (!customEntryPackage.isEdit()) {
                            customEntryAdapter.addEntry(customEntryPackage.getEntry());
                        }
                    }
                }

                if (customEntryAdapter.getItemCount() == 0) emptyCard.setVisibility(View.VISIBLE);
                else emptyCard.setVisibility(View.GONE);

                dictionaryViewModel.resetDictionaryEntries();
                entryViewModel.handleResult(result);
            }
    );

    @Override
    public void onItemTouch(String entry, int position) {
        Intent startEntryBuilderIntent = new Intent(requireActivity(), EntryBuilderActivity.class);
        startEntryBuilderIntent = entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, position);
        entryBuilderActivity.launch(startEntryBuilderIntent);
    }
}