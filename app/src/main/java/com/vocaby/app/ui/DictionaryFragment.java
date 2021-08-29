package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.viewmodels.DictionaryViewModel;


public class DictionaryFragment extends Fragment {
    private DictionaryViewModel dictionaryViewModel;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    public static DictionaryFragment newInstance() {
        DictionaryFragment fragment = new DictionaryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
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


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        Observer<String> searchObserver = s -> {
            if(!s.isEmpty()) {
                addResultsFragment(s, false);
            }
        };

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), searchObserver);
    }

    public void addResultsFragment(String search, boolean ignoreHistory) {
        dictionaryViewModel.addToStack(search);
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            )
            .add(R.id.search_fragment_container, SearchResultsFragment.newInstance(search, ignoreHistory))
            .commit();
    }

    private final TextView.OnEditorActionListener searchEditorListener = (v, actionId, event) -> {
        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();
        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            String searchedWord = v.getText().toString().toLowerCase().replaceAll("[^a-z]", "");
            dictionaryViewModel.setSearch(searchedWord);
            return true;
        }

        return false;
    };
}