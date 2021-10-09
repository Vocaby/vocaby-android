package com.vocaby.app.ui;

import static androidx.appcompat.content.res.AppCompatResources.*;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.adapters.DefinitionsAdapter;
import com.vocaby.app.models.EntryDataPackage;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.SearchResultsViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SearchResultsFragment extends Fragment {
    private static final String WORD = "PASSED_WORD_KEY";
    private String searchedWord;

    private Context ctx;
    private Button saveButton;
    private TextView word;
    private TextView pronunciation;
    private DefinitionsAdapter adapter;
    private ProgressBar progressBar;
    private ProgressBar saveProgress;

    SearchResultsViewModel searchResultsViewModel;
    DictionaryViewModel dictionaryViewModel;
    UserViewModel userViewModel;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    public static SearchResultsFragment newInstance(String passedWord) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(WORD, passedWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchedWord = getArguments().getString(WORD);
        }

        ctx = requireActivity().getApplicationContext();
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
        saveProgress = view.findViewById(R.id.save_progress);
        saveProgress.setVisibility(View.VISIBLE);

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
        dictionaryViewModel =
                new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        searchResultsViewModel = new ViewModelProvider(this).get(SearchResultsViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        searchResultsViewModel.retrieveWordDataFromRepo(searchedWord);
        observeWordPackageData();
    }

    public void observeWordPackageData() {
        searchResultsViewModel.getWordData().observe(getViewLifecycleOwner(), wordPackage -> {
            if (wordPackage != null) {
                EntryModel wordData = wordPackage.getWordModel();
                word.setVisibility(View.VISIBLE);
                if (wordData.isEmpty()) {
                    populateNoDefinition();
                } else {
                    populateView(wordData);
                    adapter.setWordData(wordData);
                }

                setupSaveButton(wordPackage.saved(), false);
            }
        });

        searchResultsViewModel.getSavedStatus().observe(getViewLifecycleOwner(), saved -> {
            setupSaveButton(saved, true);
        });
    }

    private void setupSaveButton(boolean saved, boolean updateSave) {
        AtomicReference<Drawable> icon = new AtomicReference<>();
        icon.set(getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        saveButton.setEnabled(false);
        saveButton.setTextColor(ctx.getColor(R.color.dark_gray));
        saveProgress.setVisibility(View.INVISIBLE);

        if(saved) {
            icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved));
            saveButton.setText(ctx.getString(R.string.save_button_saved));
            saveButton.setOnClickListener(v -> {
                searchResultsViewModel.removeSave(searchedWord);
                saveProgress.setVisibility(View.VISIBLE);
                disableSaveButton(icon);
            });

            if (updateSave) userViewModel.addSaveItem(searchedWord);
        } else {
            icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved));
            saveButton.setText(getResources().getString(R.string.save_button_unsaved));
            saveButton.setOnClickListener(v -> {
                searchResultsViewModel.saveWord(searchedWord);
                saveProgress.setVisibility(View.VISIBLE);
                disableSaveButton(icon);
            });

            if (updateSave) userViewModel.removeSaveItem(searchedWord);
        }

        saveProgress.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(true);
        saveButton.setTextColor(ctx.getColor(R.color.colorPrimary));
        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
    }

    private void disableSaveButton(AtomicReference<Drawable> icon) {
        saveButton.setEnabled(false);
        saveButton.setTextColor(ctx.getColor(R.color.dark_gray));
        icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dictionaryViewModel.popSearchStack();
    }

    private void populateView(EntryModel entryData) {
        progressBar.setVisibility(View.GONE);
        word.setText(searchedWord);
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);

        String pronunciationText = entryData.getPronunciation().replaceAll("\n","");
        if(!pronunciationText.isEmpty()) {
            pronunciation.setVisibility(View.VISIBLE);
            pronunciation.setText(pronunciationText);
        }
    }

    private void populateNoDefinition() {
        progressBar.setVisibility(View.GONE);
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