package com.vocaby.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    private static final String WORD = "word";

    private EditText search;
    private InputMethodManager imm;
    private String sentText;
    private String prevWord;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        search = view.findViewById(R.id.search_bar);
        search.setOnEditorActionListener(searchEditorListener);

        getChildFragmentManager().beginTransaction().replace(R.id.search_fragment_container, new SearchFragment()).commit();
        prevWord = "";

        // Coming from the saves fragment
        if(sentText.length() > 0) {
            search.setText(sentText);
            // When the user clicks on a saved item, the nav should check dictionary
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_navigation);
            navView.getMenu().findItem(R.id.search).setChecked(true);
            addResultsFragment(sentText, true);
        }

        return view;
    }

    private void addResultsFragment(String word, boolean fromSaves) {
        prevWord = word;
        Fragment fragment = SearchResultsFragment.newInstance(word, fromSaves);
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.addToBackStack(null);
        transaction.setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
        );

        transaction.add(R.id.search_fragment_container, fragment, "SEARCH_RESULTS_FRAGMENT").commit();
    }

    private final TextView.OnEditorActionListener searchEditorListener = (v, actionId, event) -> {
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();

        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            // Clean up text a little bit
            String searchedText = v.getText().toString().toLowerCase().replaceAll("[^a-z]","");
            // Prevent double searching
            if(!searchedText.isEmpty() && !prevWord.equals(searchedText)) {
                addResultsFragment(searchedText, false);
            }

            return true;
        }

        return false;
    };

    public void resetSearch() {
        prevWord = "";
    }
}