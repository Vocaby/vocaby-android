package com.vocaby.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.database.DatabaseManager;
import com.vocaby.app.models.WordModel;

import java.util.List;

public class DictionaryHomeFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener {
    private DataManager dataManager;
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    public static final String RADIO_DATASET_CHANGED = "com.vocaby.app.RADIO_DATASET_CHANGED";
    private Radio radio;

    @Override
    public void onItemTouch(int position) {
        String word = dataManager.getHistory().get(position);
        DictionaryFragment fragment = (DictionaryFragment) this.getParentFragment();
        fragment.addResultsFragment(word,false);
    }

    private class Radio extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RADIO_DATASET_CHANGED)){
                searchHistoryAdapter.notifyDataSetChanged();
            }
        }
    }

    public DictionaryHomeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_dictionary_main, container, false);

        // Random Word of the Day
        TextView wordView = view.findViewById(R.id.word_header);
        TextView posView = view.findViewById(R.id.pos);
        TextView definition = view.findViewById(R.id.definition);
        TextView sentence = view.findViewById(R.id.sentence);
        View wordBox = view.findViewById(R.id.word_box);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        int index = settings.getInt("randomWordIndex", 1);
        DatabaseManager databaseManager = DatabaseManager.getInstance(ctx);
        WordModel wordModelData = databaseManager.getRandomWordData(index);

        String pos = wordModelData.getAllowedPos()[0];
        wordView.setText(wordModelData.getWord());
        posView.setText(pos);
        definition.setText(wordModelData.getDefinitions(pos)[0]);
        sentence.setText(wordModelData.getSentences(pos)[0]);

        wordBox.setOnClickListener(v -> {
            DictionaryFragment fragment = (DictionaryFragment) this.getParentFragment();
            fragment.addResultsFragment(wordModelData.getWord(),true);
        });


        // History
        List<String> history = dataManager.getHistory();
        TextView historyAlert = view.findViewById(R.id.history_alert);
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        searchHistoryAdapter = new SearchHistoryAdapter(ctx, dataManager.getHistory(), this);
        historyContainer.setAdapter(searchHistoryAdapter);
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