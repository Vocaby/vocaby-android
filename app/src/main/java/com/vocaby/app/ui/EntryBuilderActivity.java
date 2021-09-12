package com.vocaby.app.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocaby.app.R;
import com.vocaby.app.adapters.CustomGroupAdapter;
import com.vocaby.app.adapters.DragStartListener;
import com.vocaby.app.adapters.ItemTouchCallback;
import com.vocaby.app.viewmodels.EntryViewModel;

public class EntryBuilderActivity extends AppCompatActivity
        implements DragStartListener, CustomGroupAdapter.ItemInteractionListener {
    private BottomSheetDialog groupBuilder;
    private RadioGroup radioGroup;
    private EntryViewModel entryViewModel;
    private CustomGroupAdapter customGroupAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_entry_builder);

        setUpGroupBuilder();
        setupButtons();
        setupViewModel();
        setupRecyclerView();

        entryViewModel.getResult().observe(this, result -> {
            if (result == EntryViewModel.ADD_GROUP) {
                customGroupAdapter.addItem();
            } else if (result == EntryViewModel.EDIT_GROUP) {
                customGroupAdapter.editItem(entryViewModel.getSelectedItemPosition());
            } else if (result == EntryViewModel.REMOVE_GROUP){
                customGroupAdapter.removeItem(entryViewModel.getSelectedItemPosition());
            }
        });
    }

    private void setupViewModel() {
        entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);
    }

    private void setUpGroupBuilder() {
        groupBuilder = new BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog);
        groupBuilder.setContentView(R.layout.custom_entry_group_builder_dialog);
        radioGroup = groupBuilder.findViewById(R.id.type_radio_container);
    }

    private void setupButtons() {
        Button closeButton = findViewById(R.id.back_button);
        closeButton.setOnClickListener(v -> finish());

        Button button = groupBuilder.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> groupBuilder.dismiss());
        }

        // Edit word / phrase
        Button editWordButton = findViewById(R.id.edit_custom_word_button);

        // Add new group button
        Button addGroupButton = findViewById(R.id.add_def_group_button);
        addGroupButton.setOnClickListener(v -> {
            radioGroup.removeAllViews();

            // Temporary Types
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(12, 8, 12, 8);
            String[] types = getResources().getStringArray(R.array.type);

            for(String type : types) {
                if(!entryViewModel.getGroupTypes().contains(type)) {
                    RadioButton radioButton = new RadioButton(new ContextThemeWrapper(this,
                            R.style.Theme_VocabyAndroid_RadioButton), null, 0);
                    radioButton.setText(type);
                    radioButton.setId(View.generateViewId());
                    if (radioGroup != null) {
                        radioGroup.addView(radioButton, layoutParams);
                    }
                }
            }

            groupBuilder.show();
        });

        // Create group button
        TextView typeCreatorAlert = groupBuilder.findViewById(R.id.type_creator_alert);
        Button createGroupButton = groupBuilder.findViewById(R.id.create_group_button);
        Intent groupBuilderActivityData = new Intent(this, EntryGroupBuilderActivity.class);
        if (createGroupButton != null) {
            createGroupButton.setOnClickListener(v -> {
                int id = radioGroup.getCheckedRadioButtonId();
                if (id == -1) {
                    if (typeCreatorAlert != null) typeCreatorAlert.setVisibility(View.VISIBLE);
                } else {
                    RadioButton radioButton = radioGroup.findViewById(id);
                    if (typeCreatorAlert != null) {
                        typeCreatorAlert.setVisibility(View.INVISIBLE);
                    }
                    if (radioButton == null) {
                        if (typeCreatorAlert != null) typeCreatorAlert.setVisibility(View.VISIBLE);
                    } else {
                        String type = radioButton.getText().toString();
                        radioGroup.clearCheck();
                        groupBuilderActivityData.putExtra("type", type);
                        groupBuilderActivityData.putExtra("edit", false);
                        groupBuilder.dismiss();
                        groupBuilderActivity.launch(groupBuilderActivityData);
                    }
                }
            });
        }
    }

    private void setupRecyclerView() {
        customGroupAdapter = new CustomGroupAdapter(this, entryViewModel.getGroups(),
                this, this, this);
        RecyclerView recyclerView = findViewById(R.id.custom_entry_group_container);
        recyclerView.setAdapter(customGroupAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper.Callback callback = new ItemTouchCallback(customGroupAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private final ActivityResultLauncher<Intent> groupBuilderActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> entryViewModel.handleResult(result)
    );

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemClicked(int position) {
        Intent groupBuilderActivityData = new Intent(this, EntryGroupBuilderActivity.class);
        groupBuilderActivityData.putExtra("definitionData", entryViewModel.getCurrentData().get(position));
        groupBuilderActivityData.putExtra("edit", true);
        groupBuilderActivity.launch(groupBuilderActivityData);
        entryViewModel.setSelectedItemPosition(position);
    }
}