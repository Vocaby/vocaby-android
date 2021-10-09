package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.viewmodels.DictionaryViewModel;

public class DictionaryHomeFragment extends Fragment {
    private DictionaryViewModel dictionaryViewModel;

    private TextView wordView;
    private TextView posView;
    private TextView definition;
    private TextView sentence;

    private View wordBox;
    private ProgressBar progressBar;

    public DictionaryHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        definition.setVisibility(View.GONE);
        sentence.setVisibility(View.GONE);
        posView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

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

    @Override
    public void onResume() {
        super.onResume();
        dictionaryViewModel.updateRandomWord();
    }
}