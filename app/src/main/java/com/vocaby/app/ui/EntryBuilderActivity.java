package com.vocaby.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView recyclerView;
    private TextView entryView;
    private ProgressBar saveProgresBar;
    private TextView headerAlert;
    private TextView groupAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_entry_builder);

        entryView = findViewById(R.id.entry_header);
        entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);
        String entry = entryViewModel.parseRetrieved(getIntent());
        entryView.setText(entry.toUpperCase());
        headerAlert = findViewById(R.id.entry_header_alert);
        groupAlert = findViewById(R.id.group_header_alert);

        setUpGroupBuilder();
        setupButtons();
        setupRecyclerView();

        entryViewModel.getResult().observe(this, result -> {
            if (result == EntryViewModel.ADD_GROUP) {
                groupAlert.setVisibility(View.INVISIBLE);
                customGroupAdapter.addItem();
            } else if (result == EntryViewModel.EDIT_GROUP) {
                customGroupAdapter.editItem(entryViewModel.getSelectedItemPosition());
            } else if (result == EntryViewModel.REMOVE_GROUP){
                customGroupAdapter.onItemDismiss(entryViewModel.getSelectedItemPosition());
            } else if (result == EntryViewModel.SAVE_ENTRY) {
                Intent resultIntent = new Intent();
                resultIntent = entryViewModel.addResultDataToIntent(resultIntent);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else if (result == EntryViewModel.EMPTY_ENTRY) {
                groupAlert.setVisibility(View.VISIBLE);
                saveProgresBar.setVisibility(View.INVISIBLE);
            } else if (result == EntryViewModel.CANCEL) {
                finish();
            }
        });

        entryViewModel.getGroups().observe(this, list -> customGroupAdapter.setList(list));
    }

    private void setUpGroupBuilder() {
        groupBuilder = new BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog);
        groupBuilder.setContentView(R.layout.custom_entry_group_builder_dialog);
        radioGroup = groupBuilder.findViewById(R.id.type_radio_container);

        Button button = groupBuilder.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> groupBuilder.dismiss());
        }

        // Create group button
        TextView typeCreatorAlert = groupBuilder.findViewById(R.id.type_creator_alert);
        Button createGroupButton = groupBuilder.findViewById(R.id.create_group_button);

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
                        String type = radioButton.getText().toString().toLowerCase();
                        radioGroup.clearCheck();
                        Intent groupBuilderActivityData = new Intent(this, EntryGroupBuilderActivity.class);
                        groupBuilderActivityData = entryViewModel.addGroupDataToIntent(groupBuilderActivityData, type);
                        groupBuilder.dismiss();
                        groupBuilderActivity.launch(groupBuilderActivityData);
                    }
                }
            });
        }
    }

    private void setupButtons() {
        Button closeButton = findViewById(R.id.back_button);
        closeButton.setOnClickListener(v -> finish());

        // Save Button
        Button saveButton = findViewById(R.id.save_button);
        saveProgresBar = findViewById(R.id.save_progress_bar);
        saveButton.setOnClickListener(v -> {
            saveProgresBar.setVisibility(View.VISIBLE);
            entryViewModel.saveUserEntry();
        });

        // Add new group button
        Button addGroupButton = findViewById(R.id.add_def_group_button);
        addGroupButton.setOnClickListener(view -> {
            String[] types = getResources().getStringArray(R.array.type);
            radioGroup.removeAllViews();

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(12, 8, 12, 8);

            for(String type : types) {
                if(!entryViewModel.entryHasType(type)) {
                    RadioButton radioButton = new RadioButton(new ContextThemeWrapper(this,
                            R.style.Theme_VocabyAndroid_RadioButton), null, 0);
                    radioButton.setText(type.toUpperCase());
                    radioButton.setId(View.generateViewId());
                    if (radioGroup != null) {
                        radioGroup.addView(radioButton, layoutParams);
                    }
                }
            }
            groupAlert.setVisibility(View.INVISIBLE);
            groupBuilder.show();
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.custom_entry_group_container);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customGroupAdapter = new CustomGroupAdapter(this, this, this);
        recyclerView.setAdapter(customGroupAdapter);
        ItemTouchHelper.Callback callback = new ItemTouchCallback(customGroupAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private final ActivityResultLauncher<Intent> groupBuilderActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> entryViewModel.handleResult(result)
    );

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemClicked(int position) {
        Intent groupBuilderActivityData = new Intent(this, EntryGroupBuilderActivity.class);
        groupBuilderActivityData = entryViewModel.addGroupDataToIntent(groupBuilderActivityData, position);
        groupBuilderActivity.launch(groupBuilderActivityData);
        entryViewModel.setSelectedItemPosition(position);
    }

    @Override
    public void onItemRemoved(int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        entryViewModel.removeGroup(position);
        customGroupAdapter.notifyItemRemoved(position);

        // Google's Implementation of ItemTouchHelper assumes that
        // the swiped items are cleaned up. Because the view is recycled
        // when swiped and not cleaned up with RecyclerView,
        // the view is positioned outside the recyclerview when a new item is added.
        // So we need to revert back the position by doing the following:
        if(holder != null) {
            holder.itemView.setVisibility(View.INVISIBLE);
            customGroupAdapter.notifyItemChanged(position);
            itemTouchHelper.startSwipe(holder);
        }
    }
}