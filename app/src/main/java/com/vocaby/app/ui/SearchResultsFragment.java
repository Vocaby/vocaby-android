package com.vocaby.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.adapters.DefinitionsAdapter;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.viewmodels.DictionaryViewModel;

public class SearchResultsFragment extends Fragment {
    private static final String WORD = "PASSED_WORD_KEY";
    private static final String FROM_SAVES = "fromSaves";
    private String searchedWord;
    private boolean fromSaves;
    private Context ctx;
    private Button saveButton;
    private DataManager dataManager;

    private TextView word;
    private TextView pronunciation;
    private DefinitionsAdapter adapter;
    private ProgressBar progressBar;

    DictionaryViewModel dictionaryViewModel;
    Observer<WordModel> observer;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    public static SearchResultsFragment newInstance(String passedWord, boolean fromSaves) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(WORD, passedWord);
        args.putBoolean(FROM_SAVES, fromSaves);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchedWord = getArguments().getString(WORD);
            fromSaves = getArguments().getBoolean(FROM_SAVES);
        }

        ctx = requireActivity().getApplicationContext();
        dataManager = DataManager.getInstance(ctx);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DictionaryFragment fragment = (DictionaryFragment) getParentFragment();
        assert fragment != null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        progressBar = view.findViewById(R.id.search_progress);
        word = view.findViewById(R.id.word_header);
        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(backListener);
        saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(saveListener);
        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);
        pronunciation = view.findViewById(R.id.pronunciation);
        ProgressBar saveProgress = view.findViewById(R.id.save_progress);
        saveProgress.setVisibility(View.INVISIBLE);
        Drawable icon;
        icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);

//        if(dataManager.hasSave(searchedWord)) {
//            icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
//            saveButton.setText(ctx.getString(R.string.save_button_saved));
//        } else {
//            icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
//            saveButton.setText(getResources().getString(R.string.save_button_unsaved));
//        }

        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        saveButton.setOnClickListener(v -> {
            dictionaryViewModel.saveWord(searchedWord);
        });

        RecyclerView recyclerView = view.findViewById(R.id.definitions_recycler_container);
        recyclerView.setEnabled(false);
        adapter = new DefinitionsAdapter(ctx);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        dictionaryViewModel.retrieveWordDataFromRepo(searchedWord, NetworkManager.isConnectedToInternet(ctx));
        observer = wordData -> {
            if (wordData != null) {
                if(!fromSaves) {
                    // Only write to history when user searches for the definition
                    // Not when the user looks up definition through saved words
                    dataManager.writeHistory(searchedWord);
                    Intent intent = new Intent(DictionaryHomeFragment.RADIO_DATASET_CHANGED);
                    ctx.sendBroadcast(intent);
                }

                if(wordData.isEmpty()) {
                    populateNoDefinition();
                } else {
                    populateView(wordData);
                    adapter.setWordData(wordData);
                }
            }

            dictionaryViewModel.getWordData().removeObservers(getViewLifecycleOwner());
        };

        dictionaryViewModel.getWordData().observe(getViewLifecycleOwner(), observer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dictionaryViewModel.popSearchHistory();
    }

    private void populateView(WordModel wordModelData) {
        progressBar.setVisibility(View.INVISIBLE);
        word.setText(searchedWord);
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);

        String pronunciationText = wordModelData.getPronunciation().replaceAll("\n","");
        if(pronunciationText.isEmpty()) {
            pronunciation.setVisibility(View.GONE);
        } else {
            pronunciation.setVisibility(View.VISIBLE);
            pronunciation.setText(pronunciationText);
        }
    }

    private void populateNoDefinition() {
        progressBar.setVisibility(View.INVISIBLE);
        word.setText(getResources().getString(R.string.no_definition_found));
        pronunciation.setVisibility(View.GONE);
    }

    private final View.OnClickListener backListener = v -> requireActivity().onBackPressed();

    private final View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveButton.setEnabled(false);
        }
    };
}