package com.vocaby.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomDefAdapter;
import com.vocaby.app.models.DefinitionModel;

import java.util.ArrayList;
import java.util.List;

public class EntryGroupBuilderActivity extends AppCompatActivity {
    private CustomDefAdapter customDefAdapter;
    private List<DefinitionModel> definitions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_group_builder_activity);
        definitions = new ArrayList<>();
        setupRecyclerView();
        setupButtons();
    }

    private void setupButtons() {
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Add Definition Group Setup
        final BottomSheetDialog definitionBuilder = new BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog);
        definitionBuilder.setContentView(R.layout.definition_builder);

        Button button = definitionBuilder.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> definitionBuilder.dismiss());
        }

        Button addGroupButton = findViewById(R.id.add_def_button);
        addGroupButton.setOnClickListener(v -> {
            definitionBuilder.show();
        });

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
                        exampleView.getText().clear();
                    }

                    DefinitionModel definitionData = new DefinitionModel(definition, example);
                    definitions.add(definitionData);
                    customDefAdapter.notifyItemInserted(definitions.size() - 1);
                    definitionBuilder.dismiss();
                }
            });
        }
    }

    private void setupRecyclerView() {
        customDefAdapter = new CustomDefAdapter(definitions);
        RecyclerView recyclerView = findViewById(R.id.custom_entry_definition_container);
        recyclerView.setAdapter(customDefAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}