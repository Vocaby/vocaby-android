package com.vocaby.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.vocaby.app.R;

public class EntryBuilderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_builder);
        setupButtons();
        setupHeaderEditor();
    }

    private void setupButtons() {
        Button closeButton = findViewById(R.id.back_button);
        closeButton.setOnClickListener(v -> finish());

        Button addGroupButton = findViewById(R.id.add_def_group_button);
        Intent startGroupBuilderIntent = new Intent(this, EntryGroupBuilderActivity.class);
        addGroupButton.setOnClickListener(v -> {
            startActivity(startGroupBuilderIntent);
        });
    }

    private void setupHeaderEditor() {
        // Entry Header Editor
        EditText entryHeaderEditor = findViewById(R.id.entry_header_edit);
        entryHeaderEditor.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                hideKeyboard(v);
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}