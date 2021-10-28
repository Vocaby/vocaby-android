package com.vocaby.app.ui.customentry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomDefAdapter;
import com.vocaby.app.adapters.DragStartListener;
import com.vocaby.app.adapters.ItemTouchCallback;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.EntryGroupViewModel;

public class EntryGroupBuilderActivity extends AppCompatActivity
        implements DragStartListener, CustomDefAdapter.ItemInteractionListener {
    private EntryGroupViewModel entryGroupViewModel;
    private ItemTouchHelper itemTouchHelper;

    private BottomSheetDialog definitionBuilder;
    private RecyclerView recyclerView;
    private CustomDefAdapter customDefAdapter;

    private TextView saveAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_entry_group_builder_activity);
        saveAlert = findViewById(R.id.definition_add_alert);

        entryGroupViewModel = new ViewModelProvider(this).get(EntryGroupViewModel.class);
        entryGroupViewModel.handleIntent(getIntent());
        entryGroupViewModel.getDefinitions().observe(this,
                list -> customDefAdapter.setList(list)
        );

        setupDefinitionBuilder();
        setupButtons();
        setupRecyclerView();
        setupType();
    }

    private void setupType() {
        entryGroupViewModel.getType().observe(this, type -> {
            String header = StringFormatter.firstLetterUpperOnly(type) + " Definitions";
            TextView activityHeader = findViewById(R.id.custom_group_activity_header);
            activityHeader.setText(header);
            TextView typeHeader = findViewById(R.id.type_header);
            typeHeader.setText(type);
        });
    }

    private void setupDefinitionBuilder() {
        definitionBuilder = new BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog);
        definitionBuilder.setContentView(R.layout.custom_entry_definition_builder_dialog);
        EditText definitionView = definitionBuilder.findViewById(R.id.definition_edit);
        EditText exampleView = definitionBuilder.findViewById(R.id.example_edit);
        Button closeButton = definitionBuilder.findViewById(R.id.close_button);
        TextView alert = definitionBuilder.findViewById(R.id.definition_header_alert);

        // Close Definition Builder Button
        if (closeButton != null) closeButton.setOnClickListener(v -> definitionBuilder.dismiss());

        definitionBuilder.setOnShowListener(dialogInterface -> {
            if (definitionView != null && exampleView != null) {
                definitionView.getText().clear();
                definitionView.clearFocus();
                exampleView.getText().clear();
                exampleView.clearFocus();
            }
        });

        // Add New Definition
        Button addDefinitionButton = definitionBuilder.findViewById(R.id.create_definition_button);
        if (addDefinitionButton != null) {
            addDefinitionButton.setOnClickListener(v -> {
                // null checking
                String definition = definitionView != null ? definitionView.getText().toString() : "";
                String example = exampleView != null ? exampleView.getText().toString() : "";

                if(definition.isEmpty()) {
                    if (alert != null) alert.setVisibility(View.VISIBLE);
                } else {
                    if (alert != null) alert.setVisibility(View.INVISIBLE);

                    entryGroupViewModel.addDefinition(definition, example);
                    customDefAdapter.addItem();
                    definitionBuilder.dismiss();
                }
            });
        }
    }

    private void setupButtons() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Add Definition Button
        Button addDefinitionButton = findViewById(R.id.add_definition_button);
        addDefinitionButton.setOnClickListener(v -> definitionBuilder.show());

        // Save Button
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveAlert.setVisibility(View.INVISIBLE);
            Intent saveIntent = new Intent();
            saveIntent = entryGroupViewModel.addSaveDataToIntent(saveIntent);
            setResult(Activity.RESULT_OK, saveIntent);
            finish();
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.custom_entry_definition_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customDefAdapter = new CustomDefAdapter(this,this, this);
        recyclerView.setAdapter(customDefAdapter);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(customDefAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemRemoved(int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

        entryGroupViewModel.removeDefinition(position);
        customDefAdapter.notifyItemRemoved(position);

        // Google's Implementation of ItemTouchHelper assumes that
        // the swiped items are cleaned up. Because the view is recycled
        // when swiped and not cleaned up with RecyclerView,
        // the view is positioned outside the recyclerview when a new item is added.
        // So we need to revert back the position by doing the following:
        if(holder != null) {
            holder.itemView.setVisibility(View.INVISIBLE);
            customDefAdapter.notifyItemChanged(position);
            itemTouchHelper.startSwipe(holder);
        }

        // Alternatively, I could remove the view from the layout manager
        // by simply doing recyclerView.removeViewAt(position)
        // but this would not make use of recycling.
    }
}