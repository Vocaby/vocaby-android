package com.vocaby.app.ui;

import static com.vocaby.app.utils.StringFormatter.cleanText;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.vocaby.app.R;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.viewmodels.DictionaryViewModel;

import java.util.List;


public class DictionaryFragment extends Fragment {
    private DictionaryViewModel dictionaryViewModel;
    private FloatingSearchView searchView;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchView = view.findViewById(R.id.vocaby_search_bar);
        searchView.setOnSearchListener(searchListener);
        searchView.setOnQueryChangeListener(queryChangeListener);

        // Navigation
        ImageButton navButton = view.findViewById(R.id.nav_button);
        navButton.setOnClickListener(v -> {
            DrawerLayout drawer = requireActivity().findViewById(R.id.main_drawer);
            drawer.openDrawer(GravityCompat.END);
        });

        if(savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.dictionary_fragment_container,
                    new DictionaryHomeFragment()).commit();
        }

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
        search = cleanText(search);
        if(!dictionaryViewModel.isOpen(search)) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            dictionaryViewModel.addToStack(search);

            if(!ignoreHistory) {
                // Only write to history when user searches for the definition
                // Not when the user looks up a definition through saved words
                dictionaryViewModel.writeHistory(search);
            }

            transaction
                    .addToBackStack(null)
                    .setCustomAnimations(
                            R.anim.enter_bottom_to_top,
                            R.anim.exit_top_to_bottom,
                            R.anim.enter_bottom_to_top,
                            R.anim.exit_top_to_bottom
                    )
                    .add(R.id.dictionary_fragment_container,
                            SearchResultsFragment.newInstance(search)
                    ).commit();
        }
    }

    private final FloatingSearchView.OnSearchListener searchListener = new FloatingSearchView.OnSearchListener() {
        @Override
        public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
            String searchedWord = cleanText(searchSuggestion.getBody());
            searchView.setSearchText(searchedWord);
            searchView.clearSearchFocus();
            dictionaryViewModel.setSearch(searchedWord);
        }

        @Override
        public void onSearchAction(String currentQuery) {
            String searchedWord = cleanText(currentQuery);
            dictionaryViewModel.setSearch(searchedWord);
        }
    };

    private final FloatingSearchView.OnQueryChangeListener queryChangeListener =
            (oldQuery, newQuery) -> {
        if(!oldQuery.equals(newQuery)) {
            List<SearchSuggestionItem> searchSuggestions =
                    dictionaryViewModel.getSearchSuggestion(newQuery,4);

            searchView.swapSuggestions(searchSuggestions);
        }

    };
}