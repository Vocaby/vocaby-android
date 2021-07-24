package com.vocaby.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private DataManager dataManager;
    private Context ctx;
    private TextView historyAlert;
    private SearchAdapter searchAdapter;
    public static final String RADIO_DATASET_CHANGED = "com.vocaby.app.RADIO_DATASET_CHANGED";
    private Radio radio;

    private class Radio extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RADIO_DATASET_CHANGED)){
                searchAdapter.notifyDataSetChanged();
            }
        }
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = requireActivity().getApplicationContext();
        dataManager = DataManager.getInstance(ctx);
        radio = new Radio();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        List<String> history = dataManager.getHistory();
        historyAlert = view.findViewById(R.id.history_alert);
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        searchAdapter = new SearchAdapter(ctx, dataManager.getHistory());
        historyContainer.setAdapter(searchAdapter);
        historyContainer.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        historyContainer.setLayoutManager(new LinearLayoutManager(ctx) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        if(history.size() > 0) {
            historyAlert.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RADIO_DATASET_CHANGED);
        ctx.registerReceiver(radio, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            ctx.unregisterReceiver(radio);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}