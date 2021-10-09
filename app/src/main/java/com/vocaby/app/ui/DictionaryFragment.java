package com.vocaby.app.ui;

import static com.vocaby.app.utils.StringFormatter.cleanText;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.view.SearchInputView;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.viewmodels.DictionaryViewModel;

import java.util.List;


public class DictionaryFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener{
    private DictionaryViewModel dictionaryViewModel;
    private FloatingSearchView searchView;
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    private TextView dictionaryHeader;
    private LinearLayout header;
    OnBackPressedCallback backPressedCallback;
    private RecyclerView historyContainer;

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
        dictionaryHeader = view.findViewById(R.id.dictionary_header);
        header = view.findViewById(R.id.vocaby_header);

        if(savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.dictionary_fragment_container,
                    new DictionaryHomeFragment()).commit();
        }

        // History
        historyContainer = view.findViewById(R.id.search_history_container);
        searchHistoryAdapter = new SearchHistoryAdapter(ctx,this);
        historyContainer.setAdapter(searchHistoryAdapter);
        historyContainer.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() == 1) slideDownHeader();
                if (getChildFragmentManager().getBackStackEntryCount() > 0) getChildFragmentManager().popBackStack();
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
            if(!s.isEmpty()) {
                addResultsFragment(s, false);
            }
        };

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), searchObserver);

        dictionaryViewModel.getSearchHistory().observe(getViewLifecycleOwner(), searchHistory -> {
            if(searchHistory.size() > 0) {

            } else {

            }

            searchHistoryAdapter.updateSearchHistory(searchHistory);
        });
    }

    public void slideUpHeader() {
        dictionaryHeader.animate()
                .alpha(0.0f)
                .translationY(-dictionaryHeader.getHeight())
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dictionaryHeader.setVisibility(View.INVISIBLE);
                    }
                });

        final ConstraintLayout.LayoutParams headerParams = (ConstraintLayout.LayoutParams) header.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(headerParams.topMargin, (int) -header.getHeight());
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            header.setLayoutParams(headerParams);
        });
        animator.setStartDelay(200);
        animator.setDuration(300);
        animator.start();

        CardView inputView = searchView.findViewById(R.id.search_query_section);
        final FrameLayout.LayoutParams searchParams = (FrameLayout.LayoutParams) inputView.getLayoutParams();
        animator = ValueAnimator.ofInt(searchParams.topMargin, (int) getResources().getDimension(R.dimen.search_margin_after_slide));
        animator.addUpdateListener(valueAnimator -> {
            searchParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            inputView.setLayoutParams(searchParams);
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setStartDelay(200);
        animator.setDuration(300);
        animator.start();

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) historyContainer.getLayoutParams();
        animator = ValueAnimator.ofInt(params.topMargin, (int) getResources().getDimension(R.dimen.header_margin_after_slide));
        animator.addUpdateListener(valueAnimator -> {
            params.topMargin = (Integer) valueAnimator.getAnimatedValue();
            historyContainer.setLayoutParams(params);
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setStartDelay(200);
        animator.setDuration(300);
        animator.start();
    }

    public void slideDownHeader() {
        dictionaryHeader.setVisibility(View.VISIBLE);
        dictionaryHeader.animate()
                .alpha(1.0f)
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                    }
                });

        final ConstraintLayout.LayoutParams headerParams = (ConstraintLayout.LayoutParams) header.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(headerParams.topMargin, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            header.setLayoutParams(headerParams);
        });

        animator.setDuration(300);
        animator.start();

        CardView inputView = searchView.findViewById(R.id.search_query_section);
        final FrameLayout.LayoutParams searchParams = (FrameLayout.LayoutParams) inputView.getLayoutParams();
        animator = ValueAnimator.ofInt(searchParams.topMargin, (int) getResources().getDimension(R.dimen.search_margin_before_slide));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            searchParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            inputView.setLayoutParams(searchParams);
        });

        animator.setDuration(300);
        animator.start();

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) historyContainer.getLayoutParams();
        animator = ValueAnimator.ofInt(params.topMargin, (int) getResources().getDimension(R.dimen.header_margin_before_slide));
        animator.addUpdateListener(valueAnimator -> {
            params.topMargin = (Integer) valueAnimator.getAnimatedValue();
            historyContainer.setLayoutParams(params);
        });

        animator.setDuration(300);
        animator.start();
    }

    public void addResultsFragment(String search, boolean ignoreHistory) {
        if (getChildFragmentManager().getBackStackEntryCount() == 0) {
            backPressedCallback.setEnabled(true);
            slideUpHeader();
        }

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

    @Override
    public void onItemTouch(int position) {
        String word = dictionaryViewModel.getHistoryWord(position);
        dictionaryViewModel.setSearch(word);
    }
}