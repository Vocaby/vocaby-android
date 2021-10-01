package com.vocaby.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomEntryAdapter;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.MyEntryViewModel;

public class MyEntryFragment extends Fragment implements CustomEntryAdapter.ItemTouchListener {
    private BottomSheetDialog entryEditDialog;
    private CustomEntryAdapter customEntryAdapter;
    private TextView entryCountView;
    private EditText entryEdit;
    private TextView entryAlert;
    private MyEntryViewModel entryViewModel;
    private RecyclerView recyclerView;
    private DictionaryViewModel dictionaryViewModel;

    public MyEntryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_entry, container, false);

        setupEntryBuilder();

        // Navigation
        ImageButton navButton = view.findViewById(R.id.nav_button);
        navButton.setOnClickListener(v -> {
            DrawerLayout drawer = requireActivity().findViewById(R.id.main_drawer);
            drawer.openDrawer(GravityCompat.END);
        });

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

        setupRecyclerView(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        entryViewModel = new ViewModelProvider(requireActivity()).get(MyEntryViewModel.class);

        entryViewModel.getEntries().observe(getViewLifecycleOwner(), customEntries -> {
            customEntryAdapter.setList(customEntries);
            updateCount();
        });
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.custom_entry_container);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        customEntryAdapter = new CustomEntryAdapter(this);
        recyclerView.setAdapter(customEntryAdapter);
    }

    public void updateCount() {
        String count = "" + customEntryAdapter.getItemCount();
        entryCountView.setText(count);
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
                    String entry = result.getData().getStringExtra(MyEntryViewModel.ENTRY_TEXT_KEY);
                    boolean isEdit = result.getData().getBooleanExtra(MyEntryViewModel.ENTRY_EDIT, false);
                    boolean delete = result.getData().getBooleanExtra(MyEntryViewModel.ENTRY_DELETE, false);

                    if(delete) {
                        customEntryAdapter.deleteEntry(entry);
                    } else {
                        if (!isEdit) {
                            customEntryAdapter.addEntry(entry);
                        }
                    }
                }

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