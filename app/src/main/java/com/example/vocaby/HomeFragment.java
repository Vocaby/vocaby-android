package com.example.vocaby;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    private EditText search;
    private InputMethodManager imm;
    private Context ctx;
    private ProgressBar progressBar;
    private FrameLayout fragmentContainer;
    private RecyclerView recyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressBar = view.findViewById(R.id.search_progress);
        fragmentContainer = view.findViewById(R.id.search_fragment_container);
        progressBar.setVisibility(View.INVISIBLE);
        search = view.findViewById(R.id.search_bar);
        search.setOnFocusChangeListener(searchFocusListener);
        search.setOnEditorActionListener(searchEditorListener);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment_container, new SearchFragment()).commit();
        return view;
    }

    private final TextView.OnEditorActionListener searchEditorListener = (v, actionId, event) -> {
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();

        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            if(search != null) {
                String word = search.getText().toString().trim();
                if(!word.isEmpty()) {
                    fragmentContainer.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    String url = "https://od-api.oxforddictionaries.com/api/v2/entries/en/" + word;
                    RequestQueue q = Volley.newRequestQueue(ctx);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    // Parse the data
                                    try {
                                        WordService service = new WordService(word, response);
                                        service.parse();
                                        if(service.wasSuccessful()) {
                                            Word wordData = service.getWordData();
                                            Fragment fragment = SearchResultsFragment.newInstance(word, wordData);
                                            FragmentManager fm = getActivity().getSupportFragmentManager();
                                            FragmentTransaction transaction = fm.beginTransaction();
                                            transaction.addToBackStack(null);
                                            transaction.add(R.id.search_fragment_container, fragment, "SEARCH_RESULTS_FRAGMENT").commit();
                                        } else {
                                            throw new IllegalStateException();
                                        }
                                    } catch (IllegalStateException e) {
                                        Toast.makeText(ctx, "Something went wrong when parsing...", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(ctx, "Word not found!!!", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(ctx, error.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    // Change to "Something went wrong" fragment
                                    Toast.makeText(ctx, error.toString(), Toast.LENGTH_SHORT).show();
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

                    q.add(jsonObjectRequest);
                    return true;
                }
            }
        }

        return false;
    };

    private final View.OnFocusChangeListener searchFocusListener = (v, hasFocus) -> {
        if(!hasFocus) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    };
}