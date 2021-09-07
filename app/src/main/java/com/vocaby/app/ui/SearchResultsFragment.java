package com.vocaby.app.ui;

import static androidx.appcompat.content.res.AppCompatResources.*;

import android.content.Context;
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
import com.vocaby.app.models.UserStateModel;
import com.vocaby.app.models.WordDataPackage;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.SearchResultsViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SearchResultsFragment extends Fragment {
    private static final String WORD = "PASSED_WORD_KEY";
    private static final String FROM_SAVES = "fromSaves";
    private String searchedWord;

    private Context ctx;
    private Button saveButton;
    private TextView word;
    private TextView pronunciation;
    private DefinitionsAdapter adapter;
    private ProgressBar progressBar;
    private ProgressBar saveProgress;
    private View userStateIndicator;
    private TextView userStateText;

    SearchResultsViewModel searchResultsViewModel;
    DictionaryViewModel dictionaryViewModel;

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
        userStateIndicator = view.findViewById(R.id.network_indicator);
        userStateText = view.findViewById(R.id.network_status_text);

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

        observeWordPackageData();

        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getUserState().observe(getViewLifecycleOwner(), userStateModel -> {
            userViewModel.getUserState().observe(getViewLifecycleOwner(), userState -> {
                if(userState != null) {
                    if(userState.isLocal()) {
                        userStateText.setText(getString(R.string.local));
                        userStateIndicator.setBackgroundTintList(ctx.getColorStateList(R.color.colorPrimary));
                    } else {
                        if(userState.isSynced()) {
                            userStateText.setText(getString(R.string.synced));
                            userStateIndicator.setBackgroundTintList(ctx.getColorStateList(R.color.turquoise));
                        } else {
                            userStateText.setText(getString(R.string.unsynced));
                            userStateIndicator.setBackgroundTintList(ctx.getColorStateList(R.color.color_tertiary));
                        }
                    }
                }
            });
        });

        searchResultsViewModel.getRemoteSaveStatus().observe(getViewLifecycleOwner(),
                remoteSaveSuccessful -> {
                    if(!remoteSaveSuccessful) {
                        userViewModel.setUserState(new UserStateModel(false, false));
                    }
        });
    }

    public void observeWordPackageData() {
        searchResultsViewModel.retrieveWordDataFromRepo(searchedWord, NetworkManager.isConnectedToInternet(ctx));
        AtomicReference<Drawable> icon = new AtomicReference<>();
        icon.set(getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        saveButton.setEnabled(false);
        saveButton.setTextColor(ctx.getColor(R.color.dark_gray));
        saveProgress.setVisibility(View.INVISIBLE);
        AtomicBoolean populated = new AtomicBoolean(false);

        Observer<WordDataPackage> observer = wordPackage -> {
            if (wordPackage != null) {
                if(!populated.get()) {
                    WordModel wordData = wordPackage.getWordModel();
                    if(wordData.isEmpty()) {
                        populateNoDefinition();
                    } else {
                        populateView(wordData);
                        adapter.setWordData(wordData);
                    }

                    populated.set(true);
                }


                if(wordPackage.saved()) {
                    icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved));
                    saveButton.setText(ctx.getString(R.string.save_button_saved));
                    saveButton.setOnClickListener(v -> {
                        searchResultsViewModel.removeSave(searchedWord, NetworkManager.isConnectedToInternet(ctx));
                        saveProgress.setVisibility(View.VISIBLE);
                        disableSaveButton(icon);
                    });
                } else {
                    icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved));
                    saveButton.setText(getResources().getString(R.string.save_button_unsaved));
                    saveButton.setOnClickListener(v -> {
                        searchResultsViewModel.saveWord(searchedWord, NetworkManager.isConnectedToInternet(ctx));
                        saveProgress.setVisibility(View.VISIBLE);
                        disableSaveButton(icon);
                    });
                }

                saveProgress.setVisibility(View.INVISIBLE);
                saveButton.setEnabled(true);
                saveButton.setTextColor(ctx.getColor(R.color.colorPrimary_header));
                saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
            }
        };

        searchResultsViewModel.getWordData().observe(getViewLifecycleOwner(), observer);
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