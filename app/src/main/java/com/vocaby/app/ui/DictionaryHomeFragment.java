package com.vocaby.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.database.DatabaseManager;
import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.viewmodels.DictionaryViewModel;

import java.util.List;

public class DictionaryHomeFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener {
    private DataManager dataManager;
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    public static final String RADIO_DATASET_CHANGED = "com.vocaby.app.RADIO_DATASET_CHANGED";
    private Radio radio;
    private DictionaryViewModel dictionaryViewModel;

    private TextView wordView;
    private TextView posView;
    private TextView definition;
    private TextView sentence;
    private View wordBox;
    private ProgressBar progressBar;

    @Override
    public void onItemTouch(int position) {
        String word = dataManager.getHistory().get(position);
        dictionaryViewModel.setSearch(word);
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
        wordView = view.findViewById(R.id.word_header);
        posView = view.findViewById(R.id.pos);
        definition = view.findViewById(R.id.definition);
        sentence = view.findViewById(R.id.sentence);
        wordBox = view.findViewById(R.id.word_box);
        progressBar = view.findViewById(R.id.randomword_progress);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        int index = settings.getInt("randomWordIndex", 1);


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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        dictionaryViewModel.getRandomWord().observe(getViewLifecycleOwner(), new Observer<WordModel>() {
            @Override
            public void onChanged(WordModel wordModel) {
                progressBar.setVisibility(View.INVISIBLE);
                String pos = wordModel.getAllowedPos()[0];
                wordView.setText(wordModel.getWord());
                posView.setText(pos);
                definition.setText(wordModel.getDefinitions(pos)[0]);
                sentence.setText(wordModel.getSentences(pos)[0]);
                dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
                wordBox.setOnClickListener(v -> {
                    dictionaryViewModel.setSearch(wordModel.getWord());
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RADIO_DATASET_CHANGED);
        ctx.registerReceiver(radio, filter);
        dictionaryViewModel.updateRandomWord();
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