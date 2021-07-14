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

import java.util.List;

public class SearchFragment extends Fragment {

    private static final String DATA_MANAGER = "dm";

    private DataManager dataManager;
    private Context ctx;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(DataManager dataManager) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA_MANAGER, dataManager);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataManager = (DataManager) getArguments().getSerializable(DATA_MANAGER);
        }

        ctx = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        List<String> history = dataManager.getHistory();
        TextView historyAlert = view.findViewById(R.id.history_alert);
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        SearchAdapter searchAdapter = new SearchAdapter(ctx, dataManager.getHistory());
        historyContainer.setAdapter(searchAdapter);
        if(history.size() > 0) {
            historyAlert.setVisibility(View.INVISIBLE);
            historyContainer.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
            historyContainer.setLayoutManager(new LinearLayoutManager(ctx) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
        }

        return view;
    }
}