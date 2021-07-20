package com.example.vocaby;

import android.content.Context;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    private static final String DATA_MANAGER = "dm";
    private static final String WORD = "word";

    private EditText search;
    private InputMethodManager imm;
    private Context ctx;
    private ProgressBar progressBar;
    private FrameLayout fragmentContainer;
    private DataManager dataManager;
    private String searchedText;
    private String sentText;
    private int stackCount;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String word) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(WORD, word);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sentText = getArguments().getString(WORD);
        }

        ctx = requireActivity().getApplicationContext();
        dataManager = DataManager.getInstance(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchedText = "";
        progressBar = view.findViewById(R.id.search_progress);
        fragmentContainer = view.findViewById(R.id.search_fragment_container);
        progressBar.setVisibility(View.INVISIBLE);
        search = view.findViewById(R.id.search_bar);
        search.setOnFocusChangeListener(searchFocusListener);
        search.setOnEditorActionListener(searchEditorListener);


        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment_container, SearchFragment.newInstance()).commit();

        // Coming from the saves fragment
        if(sentText.length() > 0) {
            search.setText(sentText);
            // When the user clicks on a saved item, the nav should check dictionary
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_navigation);
            navView.getMenu().findItem(R.id.search).setChecked(true);
            search();
            sentText = "";
        }

        return view;
    }

    // Allow search for the same word after the user presses the back button
    public void resetSearch() {
        searchedText = "";
    }



    private void search() {
        String word = search.getText().toString().toLowerCase().trim();
        if(!word.isEmpty() && !searchedText.equals(word)) {
            if(sentText.length() == 0) {
                // Don't write to history if the word was passed from the saves fragment
                dataManager.writeHistory(word);
            }

            searchedText = word; // prevents searching the same word twice on the same page
            fragmentContainer.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            // check if data exists already
            if(dataManager.hasWord(word)) {
                Word wordData = dataManager.getData(word);
                switchToResultsFragment(word, wordData);
                progressBar.setVisibility(View.INVISIBLE);
                fragmentContainer.setVisibility(View.VISIBLE);
            } else {
                String url = "https://od-api.oxforddictionaries.com/api/v2/entries/en/" + word;
                RequestQueue q = Volley.newRequestQueue(ctx);
                JsonObjectRequest jsonObjectRequest = makeRequest(url, word);
                // Toast.makeText(ctx, "Getting data from API", Toast.LENGTH_SHORT).show();
                q.add(jsonObjectRequest);
            }
        }
    }

    private void switchToResultsFragment(String word, Word wordData) {
        Fragment fragment = SearchResultsFragment.newInstance(word, wordData);
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.search_fragment_container, fragment, "SEARCH_RESULTS_FRAGMENT").commit();
    }

    private final TextView.OnEditorActionListener searchEditorListener = (v, actionId, event) -> {
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();

        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            search();
            return true;
        }

        return false;
    };

    private JsonObjectRequest makeRequest(String url, String word) {
        return new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            WordService service = new WordService(word, response);
                            service.parse();
                            if(service.wasSuccessful()) {
                                Word wordData = service.getWordData();
                                try {
                                    dataManager.writeData(wordData);
                                    switchToResultsFragment(word, wordData);
                                } catch(IOException e) {
                                    Toast.makeText(ctx, "Something went wrong while storing data", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                throw new IllegalStateException();
                            }
                        } catch (IllegalStateException e) {
                            Toast.makeText(ctx, "Something went wrong while parsing...", Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                        fragmentContainer.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.INVISIBLE);
                        fragmentContainer.setVisibility(View.VISIBLE);
                        String responseBody;
                        if(error.networkResponse.data != null) {
                            try {
                                responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                JSONObject res = new JSONObject(responseBody);
                                String message = res.getString("error").toLowerCase();
                                if(message.contains("no entry found")) {
                                    switchToResultsFragment(word, null);
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

    private final View.OnFocusChangeListener searchFocusListener = (v, hasFocus) -> {
        if(!hasFocus) {
            imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    };
}