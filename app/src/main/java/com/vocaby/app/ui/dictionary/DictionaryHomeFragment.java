package com.vocaby.app.ui.dictionary;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.viewmodels.DictionaryViewModel;

public class DictionaryHomeFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener {
    private Context ctx;

    private DictionaryViewModel dictionaryViewModel;

    private FloatingSearchView searchView;
    private TextView wordView;
    private TextView posView;
    private TextView definition;
    private TextView sentence;
    private View wordBox;
    private ProgressBar progressBar;

    private SearchHistoryAdapter searchHistoryAdapter;

    public DictionaryHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionary_main, container, false);

        // Random Word of the Day
        wordView = view.findViewById(R.id.word_header);
        posView = view.findViewById(R.id.pos);
        definition = view.findViewById(R.id.card_definition);
        sentence = view.findViewById(R.id.card_sentence);
        wordBox = view.findViewById(R.id.word_box);
        progressBar = view.findViewById(R.id.randomword_progress);
        searchView = view.findViewById(R.id.vocaby_search_bar);
        searchView.setOnSearchListener(searchListener);
        searchView.setOnQueryChangeListener(queryChangeListener);

        definition.setVisibility(View.GONE);
        sentence.setVisibility(View.GONE);
        posView.setVisibility(View.GONE);

        setUpHistoryRecyclerView(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), this::addResultsFragment);

        dictionaryViewModel.getSearchHistory().observe(getViewLifecycleOwner(), searchHistory ->
                searchHistoryAdapter.updateSearchHistory(searchHistory));

        dictionaryViewModel.getRandomWord().observe(getViewLifecycleOwner(), wordModel -> {
            definition.setVisibility(View.VISIBLE);
            sentence.setVisibility(View.VISIBLE);
            posView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            String pos = wordModel.getFirstGroup().getType();
            wordView.setText(wordModel.getEntry());
            posView.setText(pos);
            definition.setText(wordModel.getFirstGroup().getDefinitionData().get(0).toString());
            sentence.setText(wordModel.getFirstGroup().getDefinitionData().get(0).getExample());
            wordBox.setOnClickListener(v -> dictionaryViewModel.setSearch(wordModel.getEntry()));
        });
    }

    private void addResultsFragment(String search) {
        FragmentManager fm = getParentFragmentManager();
        fm.popBackStackImmediate();
        fm.beginTransaction()
                .setCustomAnimations(
                        R.anim.enter_bottom_to_top,
                        R.anim.exit_top_to_bottom,
                        R.anim.enter_bottom_to_top,
                        R.anim.exit_top_to_bottom
                ).add(
                R.id.dictionary_fragment_container,
                SearchResultsFragment.newInstance(search)
        ).addToBackStack(null).commit();
    }

    private void setUpHistoryRecyclerView(View view) {
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        searchHistoryAdapter = new SearchHistoryAdapter(ctx, this);
        historyContainer.setAdapter(searchHistoryAdapter);
        historyContainer.setLayoutManager(
                new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        );
    }

    public void setSearch(String entry) {
        dictionaryViewModel.setSearch(entry);
    }

    private final FloatingSearchView.OnSearchListener searchListener =
            new FloatingSearchView.OnSearchListener() {
                @Override
                public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                    searchView.setSearchText(searchSuggestion.getBody());
                    searchView.clearSearchFocus();
                    setSearch(searchSuggestion.getBody());
                }

                @Override
                public void onSearchAction(String currentQuery) {
                    setSearch(currentQuery);
                }
            };

    private final FloatingSearchView.OnQueryChangeListener queryChangeListener =
            (oldQuery, newQuery) -> searchView.swapSuggestions(
                    dictionaryViewModel.getSearchSuggestion(newQuery, 4)
            );

    @Override
    public void onResume() {
        super.onResume();
        dictionaryViewModel.updateRandomWord();
    }

    @Override
    public void onItemTouch(int position) {
        dictionaryViewModel.getHistoryDefinition(position);
    }
}