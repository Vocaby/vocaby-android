package com.vocaby.app.ui.customentry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomEntryAdapter;
import com.vocaby.app.models.payload.PayloadState;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.MyEntryViewModel;

public class MyEntryFragment extends Fragment implements CustomEntryAdapter.ItemTouchListener {
    private Context ctx;

    private TextView entryCountView;
    private ProgressBar deleteProgress;

    private BottomSheetDialog entryEditDialog;
    private CustomEntryAdapter customEntryAdapter;

    private MyEntryViewModel entryViewModel;
    private DictionaryViewModel dictionaryViewModel;

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
        entryCountView = view.findViewById(R.id.entry_count);
        deleteProgress = view.findViewById(R.id.progress_bar);

        setupButtons(view);
        setupEntryBuilder();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);

        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        entryViewModel = new ViewModelProvider(requireActivity()).get(MyEntryViewModel.class);

        entryViewModel.getEntryResultPayload().observe(getViewLifecycleOwner(), payload -> {
            if (payload.getState() == PayloadState.ADD) {
                customEntryAdapter.addEntry();
            } else if (payload.getState() == PayloadState.DELETE) {
                customEntryAdapter.deleteEntry(payload.getPayload());
            }
        });

        entryViewModel.getEntries().observe(getViewLifecycleOwner(),
                customEntries -> customEntryAdapter.setList(customEntries)
        );

        entryViewModel.getCustomEntryCount().observe(getViewLifecycleOwner(),
                count -> entryCountView.setText(StringFormatter.cleanNumber(count))
        );

        entryViewModel.getDeleteStatus().observe(getViewLifecycleOwner(), deletePayload -> {
            if (deletePayload.getState() == PayloadState.DELETE) {
                deleteProgress.setVisibility(View.GONE);
                customEntryAdapter.deleteEntry(deletePayload.getPayload());
                dictionaryViewModel.resetDictionaryEntries();
            }
        });
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.custom_entry_container);
        customEntryAdapter = new CustomEntryAdapter(getActivity(), this);
        recyclerView.setAdapter(customEntryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
    }

    private void setupButtons(View view) {
        Button addButton = view.findViewById(R.id.add_entry_button);
        addButton.setOnClickListener(v -> entryEditDialog.show());
    }

    private void setupEntryBuilder() {
        entryEditDialog =
                new BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog);
        entryEditDialog.setContentView(R.layout.custom_entry_header_dialog);

        EditText entryEdit = entryEditDialog.findViewById(R.id.entry_edit);
        TextView entryAlert = entryEditDialog.findViewById(R.id.entry_header_alert);

        Button button = entryEditDialog.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> entryEditDialog.dismiss());
        }

        Button saveButton = entryEditDialog.findViewById(R.id.dialog_save_button);
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

                        Intent startEntryBuilderIntent =
                                new Intent(requireActivity(), EntryBuilderActivity.class);
                        startEntryBuilderIntent =
                                entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, -1);
                        entryBuilderActivity.launch(startEntryBuilderIntent);
                    }
                }
            });
        }

        // Clear content on show
        entryEditDialog.setOnShowListener(dialogInterface -> {
            if (entryEdit != null && entryAlert != null) {
                entryEdit.getText().clear();
                entryAlert.setVisibility(View.INVISIBLE);
            }
        });
    }

    private final ActivityResultLauncher<Intent> entryBuilderActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                dictionaryViewModel.resetDictionaryEntries();
                entryViewModel.handleResult(result);
            }
    );

    @Override
    public void onItemDelete(String entry, int position) {
        deleteProgress.setVisibility(View.VISIBLE);
        entryViewModel.removeCustomEntry(entry, position);
    }

    @Override
    public void onItemTouch(String entry, int position) {
        entryViewModel.setSelectedPosition(position);
        Intent startEntryBuilderIntent = new Intent(requireActivity(), EntryBuilderActivity.class);
        startEntryBuilderIntent = entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, position);
        entryBuilderActivity.launch(startEntryBuilderIntent);
    }
}