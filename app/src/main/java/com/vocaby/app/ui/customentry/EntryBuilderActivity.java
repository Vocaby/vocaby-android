package com.vocaby.app.ui.customentry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.vocaby.app.adapters.TypeAdapter;
import com.vocaby.app.models.payload.PayloadState;
import com.vocaby.app.viewmodels.EntryViewModel;

public class EntryBuilderActivity extends AppCompatActivity
        implements DragStartListener, CustomGroupAdapter.ItemInteractionListener, TypeAdapter.ItemInteractionListener {
    private EntryViewModel entryViewModel;
    private ItemTouchHelper itemTouchHelper;

    private BottomSheetDialog groupBuilder;
    private RecyclerView recyclerView;
    private CustomGroupAdapter customGroupAdapter;
    private TypeAdapter typeAdapter;

    private ProgressBar saveProgressBar;
    private TextView groupAlert;
    private Button createGroupButton;
    private TextView typeCreatorAlert;
    private EditText pronunciationInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_entry_builder);
        groupAlert = findViewById(R.id.group_header_alert);
        TextView entryView = findViewById(R.id.entry_header);
        LinearLayout instruction = findViewById(R.id.card_instruction);
        pronunciationInput = findViewById(R.id.pronunciation_input);

        setUpGroupBuilder();
        setupRecyclerView();
        setupButtons();

        entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);
        entryViewModel.parseRetrieved(getIntent());
        entryViewModel.getTypes().observe(this, typeAdapter::setList);

//        LiveDataUtil.observeOnce(entryViewModel.getGroups(), list -> {
//            if (list.isEmpty()) instruction.setVisibility(View.VISIBLE);
//
//            customGroupAdapter.setList(list);
//        });
//
//        LiveDataUtil.observeOnce(entryViewModel.getPronunciation(),
//                pronunciation -> pronunciationInput.setText(pronunciation, TextView.BufferType.EDITABLE));


        entryViewModel.getGroupChange().observe(this, groupPayload -> {
            if (groupPayload.getState() == PayloadState.ADD) {
                groupAlert.setVisibility(View.INVISIBLE);
                customGroupAdapter.addItem();
            } else if (groupPayload.getState() == PayloadState.DELETE) {
                customGroupAdapter.removeItem(groupPayload.getPayload());
            } else if (groupPayload.getState() == PayloadState.UPDATE) {
                customGroupAdapter.editItem(groupPayload.getPayload());
            }

            if (instruction.getVisibility() == View.VISIBLE) {
                instruction.setVisibility(View.GONE);
            }
        });

        entryViewModel.getTypeChange().observe(this, typePayload -> {
            if (typePayload.getState() == PayloadState.ADD) {
                typeAdapter.addItem(typePayload.getPayload());
            } else if (typePayload.getState() == PayloadState.DELETE) {
                typeAdapter.removeItem(typePayload.getPayload());
            }
        });

        entryViewModel.getSaveResult().observe(this, saveSuccessful -> {
            if (saveSuccessful) {
                setResult(Activity.RESULT_OK, entryViewModel.addEntryResultDataToIntent());
            } else {
                setResult(Activity.RESULT_CANCELED);
            }

            finish();
        });

        entryViewModel.getSelectedType().observe(this, type -> {
            if (createGroupButton != null) {
                createGroupButton.setOnClickListener(v -> {
                    if (type.isEmpty()) {
                        if (typeCreatorAlert != null) typeCreatorAlert.setVisibility(View.VISIBLE);
                    } else {
                        if (typeCreatorAlert != null) typeCreatorAlert.setVisibility(View.INVISIBLE);

                        Intent groupBuilderActivityData = new Intent(this, EntryGroupBuilderActivity.class);
                        groupBuilderActivityData = entryViewModel.addNewGroupDataToIntent(groupBuilderActivityData, type);
                        groupBuilderActivity.launch(groupBuilderActivityData);
                        groupBuilder.dismiss();
                    }
                });
            }
        });

        entryViewModel.getEntry().observe(this, entryView::setText);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.custom_entry_group_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customGroupAdapter = new CustomGroupAdapter(this, this, this);
        recyclerView.setAdapter(customGroupAdapter);
        ItemTouchHelper.Callback callback = new ItemTouchCallback(customGroupAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setupButtons() {
        // Close Entry Builder Button
        Button closeButton = findViewById(R.id.back_button);
        closeButton.setOnClickListener(v -> finish());

        // Save Custom Entry Button
        Button saveButton = findViewById(R.id.save_button);
        saveProgressBar = findViewById(R.id.save_progress_bar);
        saveButton.setOnClickListener(v -> {
            saveProgressBar.setVisibility(View.VISIBLE);
            entryViewModel.saveUserEntry(pronunciationInput.getText().toString());
        });

        // Add new group button
        Button addGroupButton = findViewById(R.id.add_def_group_button);
        addGroupButton.setOnClickListener(view -> groupBuilder.show());

        createGroupButton = groupBuilder.findViewById(R.id.create_group_button);
        typeCreatorAlert = groupBuilder.findViewById(R.id.type_creator_alert);
    }

    private void setUpGroupBuilder() {
        groupBuilder = new BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog);
        groupBuilder.setContentView(R.layout.custom_entry_group_builder_dialog);

        groupBuilder.setOnShowListener(dialogInterface -> groupAlert.setVisibility(View.INVISIBLE));

        RecyclerView builderRecyclerView = groupBuilder.findViewById(R.id.type_container);
        if (builderRecyclerView != null) {
            builderRecyclerView.setHasFixedSize(true);
            builderRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            typeAdapter = new TypeAdapter(this);
            builderRecyclerView.setAdapter(typeAdapter);
        }

        Button button = groupBuilder.findViewById(R.id.close_button);
        if (button != null) {
            button.setOnClickListener(v -> groupBuilder.dismiss());
        }
    }

    private final ActivityResultLauncher<Intent> groupBuilderActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> entryViewModel.handleGroupCreationResult(result)
    );

    @Override
    public void onDragStart(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onGroupCardClicked(int position) {
        entryViewModel.setSelectedGroup(position);
        Intent groupBuilderActivityData = new Intent(this, EntryGroupBuilderActivity.class);
        groupBuilderActivityData = entryViewModel.addExistingGroupDataToIntent(groupBuilderActivityData, position);
        groupBuilderActivity.launch(groupBuilderActivityData);
    }

    @Override
    public void onItemRemoved(int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        entryViewModel.removeGroup(PayloadState.UPDATE, position);

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

    @Override
    public void onTypeClicked(String type) {
        entryViewModel.setSelectedType(type);
    }
}