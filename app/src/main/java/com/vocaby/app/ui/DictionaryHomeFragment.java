package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.viewmodels.DictionaryViewModel;

public class DictionaryHomeFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener {
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    private DictionaryViewModel dictionaryViewModel;

    private TextView wordView;
    private TextView posView;
    private TextView definition;
    private TextView sentence;
    private TextView historyAlert;

    private View wordBox;
    private ProgressBar progressBar;

    @Override
    public void onItemTouch(int position) {
        String word = dictionaryViewModel.getHistoryWord(position);
        dictionaryViewModel.setSearch(word);
    }

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
        definition = view.findViewById(R.id.definition);
        sentence = view.findViewById(R.id.sentence);
        wordBox = view.findViewById(R.id.word_box);
        progressBar = view.findViewById(R.id.randomword_progress);


        // History
        historyAlert = view.findViewById(R.id.history_alert);
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        searchHistoryAdapter = new SearchHistoryAdapter(ctx,this);
        historyContainer.setAdapter(searchHistoryAdapter);
        historyContainer.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        historyContainer.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(this).get(DictionaryViewModel.class);

        dictionaryViewModel.getRandomWord().observe(getViewLifecycleOwner(), wordModel -> {
            progressBar.setVisibility(View.INVISIBLE);
            String pos = wordModel.getAllowedPos()[0];
            wordView.setText(wordModel.getWord());
            posView.setText(pos);
            definition.setText(wordModel.getDefinitions(pos)[0]);
            sentence.setText(wordModel.getSentences(pos)[0]);
            wordBox.setOnClickListener(v ->
                    dictionaryViewModel.setSearch(wordModel.getWord())
            );
        });

        dictionaryViewModel.getSearchHistory().observe(getViewLifecycleOwner(), searchHistory -> {
            if(searchHistory.size() > 0) {
                historyAlert.setVisibility(View.INVISIBLE);
            }

            searchHistoryAdapter.updateSearchHistory(searchHistory);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        dictionaryViewModel.updateRandomWord();
    }
}