package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vocaby.app.Constants;
import com.vocaby.app.R;
import com.vocaby.app.adapters.DefinitionsAdapter;
import com.vocaby.app.models.EntryModel;

public class SearchResultsBodyFragment extends Fragment {
    private Context ctx;
    private TextView word;
    private TextView pronunciation;
    private DefinitionsAdapter adapter;

    private static final String ENTRY_DATA_PARAM = "entryData";

    private EntryModel entryData;

    public SearchResultsBodyFragment() {
        // Required empty public constructor
    }
    public static SearchResultsBodyFragment newInstance(EntryModel entryData) {
        SearchResultsBodyFragment fragment = new SearchResultsBodyFragment();
        Bundle args = new Bundle();
        args.putParcelable(ENTRY_DATA_PARAM, entryData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entryData = getArguments().getParcelable(ENTRY_DATA_PARAM);
        }

        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results_body, container, false);
        word = view.findViewById(R.id.word_header);
        pronunciation = view.findViewById(R.id.pronunciation);

        RecyclerView recyclerView = view.findViewById(R.id.definitions_recycler_container);
        recyclerView.setEnabled(false);
        adapter = new DefinitionsAdapter(ctx);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        if (entryData.isEmpty()) {
            populateNoDefinition();
        } else {
            populateView(entryData);
            adapter.setWordData(entryData);
        }

        return view;
    }

    private void populateView(EntryModel entryData) {
        word.setText(entryData.getEntry());
        String pronunciationText = entryData.getPronunciation().replaceAll("\n","");
        if(!pronunciationText.isEmpty()) {
            pronunciation.setVisibility(View.VISIBLE);
            pronunciation.setText(pronunciationText);
        }
    }

    private void populateNoDefinition() {
        word.setText(getResources().getString(R.string.no_definition_found));
        pronunciation.setVisibility(View.GONE);
    }
}