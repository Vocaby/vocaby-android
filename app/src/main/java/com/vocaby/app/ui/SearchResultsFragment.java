package com.vocaby.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.adapters.DefinitionsAdapter;
import com.vocaby.app.api.RequestManager;
import com.vocaby.app.models.Word;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.viewmodels.DictionaryViewModel;


import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class SearchResultsFragment extends Fragment {

    private static final String WORD = "word";
    private static final String FROM_SAVES = "fromSaves";
    private String searchedWord;
    private boolean fromSaves;
    private Context ctx;
    private Button saveButton;
    private DataManager dataManager;

    private TextView word;
    private TextView pronunciation;
    private RecyclerView recyclerView;
    private DefinitionsAdapter adapter;
    private ProgressBar progressBar;
    private ProgressBar saveProgress;

    DictionaryViewModel dictionaryViewModel;
    Observer<Word> observer;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    public static SearchResultsFragment newInstance(String word, boolean fromSaves) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(WORD, word);
        args.putBoolean(FROM_SAVES, fromSaves);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchedWord = getArguments().getString(WORD);
            fromSaves = getArguments().getBoolean(FROM_SAVES);
        }

        ctx = requireActivity().getApplicationContext();
        dataManager = DataManager.getInstance(ctx);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DictionaryFragment fragment = (DictionaryFragment) getParentFragment();
        assert fragment != null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        progressBar = view.findViewById(R.id.search_progress);
        word = view.findViewById(R.id.word_header);
        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(backListener);
        saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(saveListener);
        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);
        pronunciation = view.findViewById(R.id.pronunciation);
        saveProgress = view.findViewById(R.id.save_progress);
        saveProgress.setVisibility(View.INVISIBLE);
        Drawable icon;

        if(dataManager.hasSave(searchedWord)) {
            icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
            saveButton.setText(ctx.getString(R.string.save_button_saved));
        } else {
            icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
            saveButton.setText(getResources().getString(R.string.save_button_unsaved));
        }

        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        recyclerView = view.findViewById(R.id.definitions_recycler_container);
        recyclerView.setEnabled(false);
        adapter = new DefinitionsAdapter(ctx);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        dictionaryViewModel.retrieveWordDataFromRepo(searchedWord, NetworkManager.isConnectedToInternet(ctx));

        observer = wordData -> {
            Log.d("SearchResults", "Observing");
            if (wordData != null && wordData.toString().equals(searchedWord)) {
                if(!fromSaves) {
                    // Only write to history when user searches for the definition
                    // Not when the user looks up definition through saved words
                    dataManager.writeHistory(searchedWord);
                    Intent intent = new Intent(DictionaryHomeFragment.RADIO_DATASET_CHANGED);
                    ctx.sendBroadcast(intent);
                }

                if(wordData.isEmpty()) {
                    populateNoDefinition();
                } else {
                    populateView(wordData);
                    adapter.setWordData(wordData);
                }
            }
        };

        dictionaryViewModel.getWordData().observe(getViewLifecycleOwner(), observer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dictionaryViewModel.setSearch("");
    }

    private void populateView(Word wordData) {
        progressBar.setVisibility(View.INVISIBLE);
        word.setText(searchedWord);
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);

        String pronunciationText = wordData.getPronunciation().replaceAll("\n","");
        if(pronunciationText.isEmpty()) {
            pronunciation.setVisibility(View.GONE);
        } else {
            pronunciation.setVisibility(View.VISIBLE);
            pronunciation.setText(pronunciationText);
        }
    }

    private void populateNoDefinition() {
        progressBar.setVisibility(View.INVISIBLE);
        word.setText(getResources().getString(R.string.no_definition_found));
        pronunciation.setVisibility(View.GONE);
    }

    private final View.OnClickListener backListener = v -> requireActivity().onBackPressed();

    private final View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveButton.setEnabled(false);
            SharedPreferences sharedPref = ctx.getSharedPreferences(getString(R.string.token_key), Context.MODE_PRIVATE);
            String token = sharedPref.getString(getString(R.string.token_key), "");
            try {
                if(saveButton.getText().equals("SAVE")) {
                    // Save the word
                    if(token.isEmpty()) {
                        Drawable icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
                        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                        dataManager.writeSave(searchedWord);
                        saveButton.setEnabled(true);
                        saveProgress.setVisibility(View.INVISIBLE);
                    } else {
                        updateRemoteSaves(false);
                    }
                } else {
                    // Remove the word from saves
                    if(token.isEmpty()) {
                        dataManager.deleteSave(searchedWord);
                        Drawable icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
                        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                        saveButton.setEnabled(true);
                        saveProgress.setVisibility(View.INVISIBLE);
                    } else {
                        updateRemoteSaves(true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void updateRemoteSaves(boolean delete) {
        saveProgress.setVisibility(View.VISIBLE);
        RequestManager requestManager = RequestManager.getInstance(ctx);

        if(delete) {
            requestManager.makeDeleteRequest(searchedWord, deleteListenerResponse, deleteListenerError);
        } else {
            requestManager.makeSaveRequest(searchedWord, saveListenerResponse, saveListenerError);
        }
    }

    private final Response.Listener<JSONObject> saveListenerResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                saveButton.setText(ctx.getString(R.string.save_button_saved));
                Drawable icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
                saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                dataManager.writeSave(searchedWord);
                saveButton.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            saveProgress.setVisibility(View.INVISIBLE);
        }
    };

    private final Response.ErrorListener saveListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(error.networkResponse != null && error.networkResponse.data!=null) {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Toast.makeText(ctx, body, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, "Something went wrong while saving...", Toast.LENGTH_SHORT).show();
            }

            saveButton.setEnabled(true);
            saveProgress.setVisibility(View.INVISIBLE);
        }
    };

    private final Response.Listener<JSONObject> deleteListenerResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            saveButton.setText(ctx.getString(R.string.save_button_unsaved));
            dataManager.deleteSave(searchedWord);
            Drawable icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
            saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            saveButton.setEnabled(true);
            saveProgress.setVisibility(View.INVISIBLE);
        }
    };

    private final Response.ErrorListener deleteListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Toast.makeText(ctx, body, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, "Something went wrong while removing...", Toast.LENGTH_SHORT).show();
            }

            saveButton.setEnabled(true);
            saveProgress.setVisibility(View.INVISIBLE);
        }
    };
}