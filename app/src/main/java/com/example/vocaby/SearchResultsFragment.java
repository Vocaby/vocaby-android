package com.example.vocaby;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultsFragment extends Fragment {

    private static final String WORD = "param1";
    private static final String WORD_DATA = "param2";

    private String mWord;
    private Word mWordData;
    private Context ctx;
    private Button saveButton;
    private DataManager dataManager;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    public static SearchResultsFragment newInstance(String param1, Word wordData) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(WORD, param1);
        args.putSerializable(WORD_DATA, wordData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWord = getArguments().getString(WORD);
            mWordData = (Word) getArguments().getSerializable(WORD_DATA);
        }

        ctx = requireActivity().getApplicationContext();
        dataManager = DataManager.getInstance(ctx);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        HomeFragment fragment = (HomeFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("HOME");
        assert fragment != null;
        fragment.resetSearch();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        // Inflate the layout for this fragment
        if (mWordData == null) {
            view = inflater.inflate(R.layout.fragment_search_results_no_def, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_search_results, container, false);
            TextView header = view.findViewById(R.id.word_header);
            header.setText(mWord);
            saveButton = view.findViewById(R.id.save_button);
            saveButton.setOnClickListener(saveListener);
            TextView pronunciation = view.findViewById(R.id.pronunciation);
            pronunciation.setText(mWordData.getPronunciation());

            Drawable icon;
            if(dataManager.hasSave(mWord)) {
                icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
                saveButton.setText(ctx.getString(R.string.save_button_saved));
            } else {
                icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
                saveButton.setText(getResources().getString(R.string.save_button_unsaved));
            }
            saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);

            RecyclerView recyclerView = view.findViewById(R.id.definitions_recycler_container);
            DefinitionsAdapter adapter = new DefinitionsAdapter(ctx, mWordData);
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        }

        return view;
    }

    private final View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Drawable icon;
            try {
                if(saveButton.getText().equals("SAVE")) {
                    icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
                    saveButton.setText(ctx.getString(R.string.save_button_saved));
                    dataManager.writeSave(mWord);
                } else {
                    // Unsave the word
                    icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
                    saveButton.setText(ctx.getString(R.string.save_button_unsaved));
                    dataManager.deleteSave(mWord);
                }
                saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}