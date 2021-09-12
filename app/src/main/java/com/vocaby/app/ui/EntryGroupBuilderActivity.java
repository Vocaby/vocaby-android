package com.vocaby.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomDefAdapter;
import com.vocaby.app.adapters.DragStartListener;
import com.vocaby.app.adapters.ItemTouchCallback;
import com.vocaby.app.models.CustomEntryGroupModel;
import com.vocaby.app.viewmodels.EntryGroupViewModel;

public class EntryGroupBuilderActivity extends AppCompatActivity
        implements DragStartListener, CustomDefAdapter.ItemInteractionListener {
    private CustomDefAdapter customDefAdapter;
    private BottomSheetDialog definitionBuilder;
    private ItemTouchHelper itemTouchHelper;
    private EntryGroupViewModel entryGroupViewModel;
    private TextView typeHeader;
    private TextView saveAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_entry_group_builder_activity);
        saveAlert = findViewById(R.id.definition_add_alert);

        setupBottomDialog();
        setupButtons();
        setupViewModel();
        setupRecyclerView();
        entryGroupViewModel.handleIntent(getIntent());
        setupType();
    }

    private void setupType() {
        entryGroupViewModel.getType().observe(this, type -> {
            String header = type + " Definitions";
            TextView activityHeader = findViewById(R.id.custom_group_activity_header);
            activityHeader.setText(header);

            typeHeader = findViewById(R.id.type_header);
            typeHeader.setText(type);
        });
    }

    private void setupViewModel() {
        entryGroupViewModel = new ViewModelProvider(this).get(EntryGroupViewModel.class);
    }

    private void setupBottomDialog() {
        definitionBuilder = new BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog);
        definitionBuilder.setContentView(R.layout.custom_entry_definition_builder_dialog);
        LinearLayout container = definitionBuilder.findViewById(R.id.custom_entry_definition_dialog);
        if (container != null) {
            container.setOnClickListener(this::hideKeyboard);
        }
    }

    private void setupButtons() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Save Button
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            if (entryGroupViewModel.getCurrentData().size() == 0 && !entryGroupViewModel.isEdit()) {
                saveAlert.setVisibility(View.VISIBLE);
            } else {
                saveAlert.setVisibility(View.INVISIBLE);
                Intent saveIntent = new Intent();
                String type = typeHeader.getText().toString();
                CustomEntryGroupModel customEntryGroupModel = new CustomEntryGroupModel(
                        type,
                        entryGroupViewModel.getCurrentData()
                );

                saveIntent.putExtra("groupData", customEntryGroupModel);
                setResult(Activity.RESULT_OK, saveIntent);
                finish();
            }
        });

        // Add Definition Setup
        Button button = definitionBuilder.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> definitionBuilder.dismiss());
        }

        Button addGroupButton = findViewById(R.id.add_definition_button);
        addGroupButton.setOnClickListener(v -> definitionBuilder.show());

        // Add New Definition
        Button addDefinitionButton = definitionBuilder.findViewById(R.id.create_definition_button);
        if (addDefinitionButton != null) {
            addDefinitionButton.setOnClickListener(v -> {
                EditText definitionView = definitionBuilder.findViewById(R.id.definition_edit);
                EditText exampleView = definitionBuilder.findViewById(R.id.example_edit);
                TextView alert = definitionBuilder.findViewById(R.id.definition_header_alert);
                String definition = definitionView != null ? definitionView.getText().toString() : "null";
                String example = exampleView != null ? exampleView.getText().toString() : "";
                if(definition.isEmpty()) {
                    if (alert != null) {
                        alert.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (alert != null) {
                        alert.setVisibility(View.INVISIBLE);
                    }

                    if (exampleView != null && definitionView != null) {
                        definitionView.getText().clear();
                        definitionView.clearFocus();
                        exampleView.getText().clear();
                        exampleView.clearFocus();
                    }

                    saveAlert.setVisibility(View.INVISIBLE);
                    entryGroupViewModel.addDefinition(definition, example);
                    customDefAdapter.addItem();
                    definitionBuilder.dismiss();
                }
            });
        }
    }

    private void setupRecyclerView() {
        customDefAdapter = new CustomDefAdapter(this, entryGroupViewModel.getDefinitions(),
                this, this, this);
        RecyclerView recyclerView = findViewById(R.id.custom_entry_definition_container);
        recyclerView.setAdapter(customDefAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper.Callback callback = new ItemTouchCallback(customDefAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onItemRemoved(int position) {
        entryGroupViewModel.removeDefinition(position);
    }
}