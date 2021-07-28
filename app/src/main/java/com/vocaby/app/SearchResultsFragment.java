package com.vocaby.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
    private ProgressBar progressBar;

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
        HomeFragment fragment = (HomeFragment) getParentFragment();
        assert fragment != null;
        fragment.resetSearch();
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

        search(searchedWord);
        return view;
    }

    private void populateView(Word wordData) {
        progressBar.setVisibility(View.INVISIBLE);
        word.setText(searchedWord);
        pronunciation.setText(wordData.getPronunciation());
        saveButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);
        DefinitionsAdapter adapter = new DefinitionsAdapter(ctx, wordData);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
    }

    private void populateNoDefinition() {
        progressBar.setVisibility(View.INVISIBLE);
        word.setText(getResources().getString(R.string.no_definition_found));
    }


    private void search(String searchedWord) {
        if(!fromSaves) {
            // Only write to history when user searches for the definition
            // Not when the user looks up definition through saved words
            dataManager.writeHistory(searchedWord);
            Intent intent = new Intent(SearchFragment.RADIO_DATASET_CHANGED);
            ctx.sendBroadcast(intent);
        }

        // check if data exists already
        if(dataManager.hasWord(searchedWord)) {
            populateView(dataManager.getData(searchedWord));
        } else {
            String url = "https://od-api.oxforddictionaries.com/api/v2/entries/en/" + word;
            RequestQueue q = Volley.newRequestQueue(ctx);
            JsonObjectRequest jsonObjectRequest = makeRequest(url, searchedWord);
            // Toast.makeText(ctx, "Getting data from API", Toast.LENGTH_SHORT).show();
            q.add(jsonObjectRequest);
        }
    }

    private JsonObjectRequest makeRequest(String url, String word) {
        return new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            WordService service = new WordService(word, response);
                            service.parse();
                            if(service.wasSuccessful()) {
                                Word data = service.getWordData();
                                populateView(data);
                                try {
                                    dataManager.writeData(data);
                                } catch(IOException e) {
                                    Toast.makeText(ctx, "Something went wrong while storing data", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                throw new IllegalStateException();
                            }
                        } catch (IllegalStateException e) {
                            Toast.makeText(ctx, "Something went wrong while parsing...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String responseBody;
                        if(error.networkResponse.data != null) {
                            try {
                                responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                JSONObject res = new JSONObject(responseBody);
                                String message = res.getString("error").toLowerCase();
                                if(message.contains("no entry found")) {
                                    populateNoDefinition();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(ctx, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> m = new HashMap<>();
                m.put("app_id", getResources().getString(R.string.app_id));
                m.put("app_key", getResources().getString(R.string.app_key));

                return m;
            }
        };
    }

    private final View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MainActivity) requireActivity()).onBackPressed();
        }
    };

    private final View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Drawable icon;
            try {
                if(saveButton.getText().equals("SAVE")) {
                    icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved);
                    saveButton.setText(ctx.getString(R.string.save_button_saved));
                    dataManager.writeSave(searchedWord);
                } else {
                    // Unsave the word
                    icon =  AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved);
                    saveButton.setText(ctx.getString(R.string.save_button_unsaved));
                    dataManager.deleteSave(searchedWord);
                }
                saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}