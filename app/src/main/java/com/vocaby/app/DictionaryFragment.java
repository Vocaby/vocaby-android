package com.vocaby.app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vocaby.app.models.Word;
import com.vocaby.app.viewmodels.DictionaryViewModel;


public class DictionaryFragment extends Fragment {
    private static final String WORD = "word";

    private String sentText;
    private DictionaryViewModel dictionaryViewModel;
    private Observer<String> searchObserver;
    private String searchedWord;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    public static DictionaryFragment newInstance(String word) {
        DictionaryFragment fragment = new DictionaryFragment();
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
        EditText search = view.findViewById(R.id.search_bar);
        search.setOnEditorActionListener(searchEditorListener);

        getChildFragmentManager().beginTransaction().replace(R.id.search_fragment_container,
                new DictionaryHomeFragment()).commit();

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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        searchObserver = s -> {
            if(!s.isEmpty()) {
                addResultsFragment(s, false);
            }
        };

        dictionaryViewModel.getSearch().observe(requireActivity(), searchObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dictionaryViewModel.getSearch().removeObserver(searchObserver);
    }

    public void addResultsFragment(String word, boolean ignoreHistory) {
        Fragment fragment = SearchResultsFragment.newInstance(word, ignoreHistory);
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.addToBackStack(null);
        transaction.setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
        );

        transaction
                .add(R.id.search_fragment_container, fragment, "SEARCH_RESULTS_FRAGMENT")
                .commit();
    }

    private final TextView.OnEditorActionListener searchEditorListener = (v, actionId, event) -> {
        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();
        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchedWord = v.getText().toString().toLowerCase().replaceAll("[^a-z]","");
            dictionaryViewModel.setSearch(searchedWord);
            return true;
        }

        return false;
    };
}