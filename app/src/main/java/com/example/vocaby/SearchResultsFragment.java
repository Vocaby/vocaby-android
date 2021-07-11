package com.example.vocaby;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultsFragment extends Fragment {

    private static final String WORD = "param1";
    private static final String WORD_DATA = "param2";

    private String mWord;
    private TextView header;
    private Word mWordData;
    private Context ctx;
    private RecyclerView recyclerView;
    private TextView pronunciation;

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

        ctx = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        header = view.findViewById(R.id.word_header);
        header.setText(mWord);
        pronunciation = view.findViewById(R.id.pronunciation);
        pronunciation.setText(mWordData.getPronunciation());
        recyclerView = view.findViewById(R.id.definitions_recycler_container);
        DefinitionsAdapter adapter = new DefinitionsAdapter(ctx, mWordData);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }
}