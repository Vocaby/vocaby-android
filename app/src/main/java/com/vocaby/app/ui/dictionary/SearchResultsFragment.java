package com.vocaby.app.ui.dictionary;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.vocaby.app.R;
import com.vocaby.app.models.dictionary.EntryModel;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.SearchResultsViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

import java.util.List;

public class SearchResultsFragment extends Fragment {
    private Context ctx;
    private static final String WORD = "PASSED_WORD_KEY";
    private String searchedWord;

    private ViewPager2 viewPager;
    private Button saveButton;
    private ProgressBar progressBar;
    private RadioGroup dictionarySelector;

    SearchResultsViewModel searchResultsViewModel;
    DictionaryViewModel dictionaryViewModel;
    UserViewModel userViewModel;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    public static SearchResultsFragment newInstance(String passedWord) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(WORD, passedWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchedWord = getArguments().getString(WORD);
        }

        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        viewPager = view.findViewById(R.id.search_results_body_pager);
        progressBar = view.findViewById(R.id.search_progress);

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(backListener);

        saveButton = view.findViewById(R.id.save_button);
        ProgressBar saveProgress = view.findViewById(R.id.save_progress);
        saveProgress.setVisibility(View.GONE);

        dictionarySelector = view.findViewById(R.id.dictionary_selector);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.setPageTransformer(new MarginPageTransformer(40));

        dictionaryViewModel =
                new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        searchResultsViewModel = new ViewModelProvider(this).get(SearchResultsViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        searchResultsViewModel.retrieveWordDataFromRepo(searchedWord);

        // Observe changes to entry save state
        searchResultsViewModel.getSaveState().observe(getViewLifecycleOwner(), saveState -> {
            saveButton.setVisibility(saveState.getVisibility());
            saveButton.setText(ctx.getString(saveState.getText()));
            saveButton.setTextColor(ctx.getColor(saveState.getColor()));
            saveButton.setEnabled(saveState.getEnabled());
            Drawable icon = getDrawable(ctx, saveState.getIcon());
            saveButton.setTextColor(ctx.getColor(R.color.colorPrimaryAccent));
            saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            saveButton.setOnClickListener(v -> {
                if (saveState.getSaved()) {
                    userViewModel.removeSaveItem(searchedWord);
                } else {
                    userViewModel.addSaveItem(searchedWord);
                }

                searchResultsViewModel.updateEntrySave(searchedWord);
            });
        });

        searchResultsViewModel.getDictionaryMissing().observe(getViewLifecycleOwner(), missingId -> {
            RadioButton button = dictionarySelector.findViewById(missingId);
            dictionarySelector.removeView(button);
            dictionarySelector.check(dictionarySelector.getChildAt(0).getId());
        });

        observeWordData();
    }

    public void observeWordData() {
        searchResultsViewModel.getWordData().observe(getViewLifecycleOwner(), entryData -> {
            setupDictionary(entryData.size());
            viewPager.setAdapter(new FragmentAdapter(this, entryData));
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setupDictionary(int size) {
        if (size > 1) {
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (position == 0) {
                        dictionarySelector.check(R.id.selection_custom);
                    } else {
                        dictionarySelector.check(R.id.selection_original);
                    }
                }
            });

            dictionarySelector.setOnCheckedChangeListener((radioGroup, id) -> {
                if (id == R.id.selection_original) {
                    viewPager.setCurrentItem(1);
                } else {
                    viewPager.setCurrentItem(0);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dictionaryViewModel.popSearchStack();
    }

    private final View.OnClickListener backListener = v -> requireActivity().onBackPressed();

    // ViewPager Adapter for Different Dictionary Definitions
    private static class FragmentAdapter extends FragmentStateAdapter {
        List<EntryModel> entryData;

        public FragmentAdapter(@NonNull Fragment fragment, List<EntryModel> entryData) {
            super(fragment);
            this.entryData = entryData;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (entryData.size() == 2) {
                if (position == 1) {
                    return SearchResultsBodyFragment.newInstance(entryData.get(1));
                }
            }

            return SearchResultsBodyFragment.newInstance(entryData.get(0));
        }

        @Override
        public int getItemCount() {
            return entryData.size();
        }
    }
}