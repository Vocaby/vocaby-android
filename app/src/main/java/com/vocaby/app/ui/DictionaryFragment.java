package com.vocaby.app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.vocaby.app.R;
import com.vocaby.app.adapters.SearchHistoryAdapter;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.viewmodels.DictionaryViewModel;

import java.util.List;

import static com.vocaby.app.utils.StringFormatter.cleanText;


public class DictionaryFragment extends Fragment implements SearchHistoryAdapter.OnItemTouchListener{
    private DictionaryViewModel dictionaryViewModel;
    private FloatingSearchView searchView;
    private Context ctx;
    private SearchHistoryAdapter searchHistoryAdapter;
    private TextView dictionaryHeaderBig;
    OnBackPressedCallback backPressedCallback;
    private RecyclerView historyContainer;
    private TextView dictionaryHeaderSmall;

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
        dictionaryHeaderBig = view.findViewById(R.id.dictionary_header);
        dictionaryHeaderSmall = view.findViewById(R.id.header_dictionary);

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
            searchHistoryAdapter.updateSearchHistory(searchHistory);
        });
    }

    public void slideUpHeader() {
        dictionaryHeaderBig.animate()
                .alpha(0.0f)
                .translationY(-dictionaryHeaderBig.getHeight())
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dictionaryHeaderBig.setVisibility(View.INVISIBLE);
                    }
                });

        CardView inputView = searchView.findViewById(R.id.search_query_section);
        final FrameLayout.LayoutParams searchParams = (FrameLayout.LayoutParams) inputView.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(searchParams.topMargin, (int) getResources().getDimension(R.dimen.search_margin_after_slide));
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

        final LinearLayout.LayoutParams headerParams = (LinearLayout.LayoutParams) dictionaryHeaderSmall.getLayoutParams();
        animator = ValueAnimator.ofInt(params.topMargin, 0);
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderSmall.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setStartDelay(500);
        animator.setDuration(600);
        animator.start();
    }

    public void slideDownHeader() {
        dictionaryHeaderBig.setVisibility(View.VISIBLE);
        dictionaryHeaderBig.animate()
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

        CardView inputView = searchView.findViewById(R.id.search_query_section);
        final FrameLayout.LayoutParams searchParams = (FrameLayout.LayoutParams) inputView.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(searchParams.topMargin, (int) getResources().getDimension(R.dimen.search_margin_before_slide));
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

        final LinearLayout.LayoutParams headerParams = (LinearLayout.LayoutParams) dictionaryHeaderSmall.getLayoutParams();
        animator = ValueAnimator.ofInt(0, (int) getResources().getDimension(R.dimen.dictionary_header_margin_before_slide));
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderSmall.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new AccelerateInterpolator());
        animator.setStartDelay(300);
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