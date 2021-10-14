package com.vocaby.app.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.DictionaryViewModel;


public class DictionaryFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener {
    private DictionaryViewModel dictionaryViewModel;
    private FloatingSearchView searchView;
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    OnBackPressedCallback backPressedCallback;
    private TextView dictionaryHeaderSmall;
    private TextView entryCounter;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.dictionary_fragment_container,
                    new DictionaryHomeFragment()).commit();
        }

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchView = view.findViewById(R.id.vocaby_search_bar);
        searchView.setOnSearchListener(searchListener);
        searchView.setOnQueryChangeListener(queryChangeListener);
        dictionaryHeaderSmall = view.findViewById(R.id.header_dictionary);
        entryCounter = view.findViewById(R.id.header_dictionary_counter);

        setUpHistoryRecyclerView(view);

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() == 1) slideDownHeader();
                if (getChildFragmentManager().getBackStackEntryCount() > 0)
                    getChildFragmentManager().popBackStack();
                if (getChildFragmentManager().getBackStackEntryCount() == 0) this.setEnabled(false);
            }
        };

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backPressedCallback);

        return view;
    }

    private void setUpHistoryRecyclerView(View view) {
        RecyclerView historyContainer = view.findViewById(R.id.search_history_container);
        searchHistoryAdapter = new SearchHistoryAdapter(ctx, this);
        historyContainer.setAdapter(searchHistoryAdapter);
        historyContainer.setLayoutManager(
                new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), this::addResultsFragment);

        dictionaryViewModel.getSearchHistory().observe(getViewLifecycleOwner(), searchHistory ->
                searchHistoryAdapter.updateSearchHistory(searchHistory));

        dictionaryViewModel.getEntryCount().observe(getViewLifecycleOwner(), count ->
                entryCounter.setText(StringFormatter.cleanNumber(count))
        );
    }

    public void slideUpHeader() {
        final LinearLayout.LayoutParams headerParams =
                (LinearLayout.LayoutParams) dictionaryHeaderSmall.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(headerParams.topMargin, 0);
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderSmall.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(600);
        animator.start();
    }

    public void slideDownHeader() {
        final LinearLayout.LayoutParams headerParams =
                (LinearLayout.LayoutParams) dictionaryHeaderSmall.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) getResources().getDimension(R.dimen.dictionary_header_margin_before_slide));
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderSmall.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    public void setSearch(String entry) {
        dictionaryViewModel.setSearch(entry);
    }

    // TODO: Need to account for duplicate search result in the backstack
    private void addResultsFragment(String search) {
        if (getChildFragmentManager().getBackStackEntryCount() == 0) {
            backPressedCallback.setEnabled(true);
            slideUpHeader();
        }

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction
        .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
        ).add(
            R.id.dictionary_fragment_container,
            SearchResultsFragment.newInstance(search),
            search
        ).addToBackStack(null).commit();
    }

    private final FloatingSearchView.OnSearchListener searchListener =
            new FloatingSearchView.OnSearchListener() {
        @Override
        public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
            searchView.setSearchText(searchSuggestion.getBody());
            searchView.clearSearchFocus();
            setSearch(searchSuggestion.getBody());
        }

        @Override
        public void onSearchAction(String currentQuery) {
            setSearch(currentQuery);
        }
    };

    private final FloatingSearchView.OnQueryChangeListener queryChangeListener =
            (oldQuery, newQuery) -> searchView.swapSuggestions(
                    dictionaryViewModel.getSearchSuggestion(oldQuery, newQuery, 4)
            );

    @Override
    public void onItemTouch(int position) {
        dictionaryViewModel.getHistoryDefinition(position);
    }
}