package com.vocaby.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vocaby.app.database.DatabaseManager;

import java.util.List;

public class SearchFragment extends Fragment implements SearchAdapter.OnItemTouchListener {
    private DataManager dataManager;
    private Context ctx;
    private SearchAdapter searchAdapter;
    public static final String RADIO_DATASET_CHANGED = "com.vocaby.app.RADIO_DATASET_CHANGED";
    private Radio radio;
    private TextView wordView;
    private TextView posView;
    private TextView definition;
    private TextView sentence;

    @Override
    public void onItemTouch(int position) {
        String word = dataManager.getHistory().get(position);
        HomeFragment fragment = (HomeFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("HOME");
        fragment.addResultsFragment(word, true);
    }

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

        wordView = view.findViewById(R.id.word_header);
        posView = view.findViewById(R.id.pos);
        definition = view.findViewById(R.id.definition);
        sentence = view.findViewById(R.id.sentence);
        updateRandomWords();

        // History
        TextView historyAlert = view.findViewById(R.id.history_alert);
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        searchAdapter = new SearchAdapter(ctx, dataManager.getHistory(), this);
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

    // Only updates when the home fragment is replaced
    public void updateRandomWords() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        int index = settings.getInt("randomWordIndex", 1);
        DatabaseManager databaseManager = DatabaseManager.getInstance(ctx);
        Word wordData = databaseManager.getRandomWordData(index);

        String pos = wordData.getAllowedPos()[0];
        wordView.setText(wordData.getWord());
        posView.setText(pos);
        definition.setText(wordData.getDefinitions(pos)[0]);
        sentence.setText(wordData.getSentences(pos)[0]);
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