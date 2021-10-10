package com.vocaby.app.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.vocaby.app.Constants;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.viewmodels.DictionaryViewModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static com.vocaby.app.utils.StringFormatter.cleanText;


public class DictionaryFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener {
    private DictionaryViewModel dictionaryViewModel;
    private FloatingSearchView searchView;
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    OnBackPressedCallback backPressedCallback;
    private RecyclerView historyContainer;
    private TextView dictionaryHeaderSmall;
    private TextView entryCounter;
    private ImageView searchBackground;

    public DictionaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchView = view.findViewById(R.id.vocaby_search_bar);
        searchView.setOnSearchListener(searchListener);
        searchView.setOnQueryChangeListener(queryChangeListener);
        dictionaryHeaderSmall = view.findViewById(R.id.header_dictionary);
        entryCounter = view.findViewById(R.id.header_dictionary_counter);
        searchBackground = view.findViewById(R.id.search_background);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.dictionary_fragment_container,
                    new DictionaryHomeFragment()).commit();
        }

        // History
        historyContainer = view.findViewById(R.id.search_history_container);
        searchHistoryAdapter = new SearchHistoryAdapter(ctx, this);
        historyContainer.setAdapter(searchHistoryAdapter);
        historyContainer.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() == 1) slideDownHeader();
                if (getChildFragmentManager().getBackStackEntryCount() > 0)
                    getChildFragmentManager().popBackStack();
                if (getChildFragmentManager().getBackStackEntryCount() == 0) this.setEnabled(false);
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        Observer<String> searchObserver = s -> {
            if (!s.isEmpty()) {
                addResultsFragment(s, false);
            }
        };

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), searchObserver);

        dictionaryViewModel.getSearchHistory().observe(getViewLifecycleOwner(), searchHistory -> {
            searchHistoryAdapter.updateSearchHistory(searchHistory);
        });

        dictionaryViewModel.getDictionaryEntries().observe(getViewLifecycleOwner(), dictionaryEntries -> {
            String count = NumberFormat.getNumberInstance(Locale.US).format(dictionaryEntries.size());
            entryCounter.setText(count);
        });
    }

    public void slideUpHeader() {
        final LinearLayout.LayoutParams headerParams = (LinearLayout.LayoutParams) dictionaryHeaderSmall.getLayoutParams();
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
        final LinearLayout.LayoutParams headerParams = (LinearLayout.LayoutParams) dictionaryHeaderSmall.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) getResources().getDimension(R.dimen.dictionary_header_margin_before_slide));
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderSmall.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    // TODO: Need to account for duplicate search result in the backstack
    public void addResultsFragment(String search, boolean ignoreHistory) {
        if (getChildFragmentManager().getBackStackEntryCount() == 0) {
            backPressedCallback.setEnabled(true);
            slideUpHeader();
        }

        search = cleanText(search);
        if (!dictionaryViewModel.isOpen(search)) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            dictionaryViewModel.addToStack(search);

            if (!ignoreHistory) {
                // Only write to history when user searches for the definition
                // Not when the user looks up a definition through saved words
                dictionaryViewModel.writeHistory(search);
            }

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

                List<SearchSuggestionItem> searchSuggestions =
                        dictionaryViewModel.getSearchSuggestion(newQuery, 4);

                searchView.swapSuggestions(searchSuggestions);
            };

    @Override
    public void onItemTouch(int position) {
        String word = dictionaryViewModel.getHistoryWord(position);
        dictionaryViewModel.setSearch(word);
    }
}