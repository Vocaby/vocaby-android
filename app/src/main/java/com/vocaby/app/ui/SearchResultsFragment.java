package com.vocaby.app.ui;

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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.vocaby.app.R;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.viewmodels.DictionaryViewModel;
import com.vocaby.app.viewmodels.SearchResultsViewModel;
import com.vocaby.app.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SearchResultsFragment extends Fragment {
    private static final String WORD = "PASSED_WORD_KEY";
    private String searchedWord;

    private Context ctx;
    private ViewPager2 viewPager;
    private Button saveButton;
    private ProgressBar progressBar;
    private ProgressBar saveProgress;
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
        saveButton.setOnClickListener(saveListener);
        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);
        saveProgress = view.findViewById(R.id.save_progress);
        saveProgress.setVisibility(View.VISIBLE);
        dictionarySelector = view.findViewById(R.id.dictionary_selector);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel =
                new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);
        searchResultsViewModel = new ViewModelProvider(this).get(SearchResultsViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        searchResultsViewModel.retrieveWordDataFromRepo(searchedWord);
        observeWordPackageData();
    }

    public void observeWordPackageData() {
        searchResultsViewModel.getWordData().observe(getViewLifecycleOwner(), wordPackage -> {
            saveButton.setVisibility(View.VISIBLE);
            saveButton.setEnabled(true);
            List<EntryModel> entryData = new ArrayList<>();
            if (wordPackage.bothDataAvailable()) {
                entryData.add(wordPackage.getCustomData());
                entryData.add(wordPackage.getOriginalData());

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
            } else if (wordPackage.onlyCustomAvailable()) {
                entryData.add(wordPackage.getCustomData());
                RadioButton button = dictionarySelector.findViewById(R.id.selection_original);
                dictionarySelector.removeView(button);
            } else {
                dictionarySelector.check(R.id.selection_original);
                entryData.add(wordPackage.getOriginalData());
                RadioButton button = dictionarySelector.findViewById(R.id.selection_custom);
                dictionarySelector.removeView(button);
            }

            viewPager.setAdapter(new FragmentAdapter(this, entryData));
            if (wordPackage.customEntryAvailable()) dictionarySelector.check(R.id.selection_custom);
            progressBar.setVisibility(View.GONE);

            setupSaveButton(wordPackage.saved(), false);
        });

        searchResultsViewModel.getSavedStatus().observe(getViewLifecycleOwner(), saved -> {
            setupSaveButton(saved, true);
        });
    }

    private void setupSaveButton(boolean saved, boolean updateSave) {
        AtomicReference<Drawable> icon = new AtomicReference<>();
        icon.set(getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        saveButton.setEnabled(false);
        saveButton.setTextColor(ctx.getColor(R.color.dark_gray));
        saveProgress.setVisibility(View.INVISIBLE);

        if(saved) {
            icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved));
            saveButton.setText(ctx.getString(R.string.save_button_saved));
            saveButton.setOnClickListener(v -> {
                searchResultsViewModel.removeSave(searchedWord);
                saveProgress.setVisibility(View.VISIBLE);
                disableSaveButton(icon);
            });

            if (updateSave) userViewModel.addSaveItem(searchedWord);
        } else {
            icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved));
            saveButton.setText(getResources().getString(R.string.save_button_unsaved));
            saveButton.setOnClickListener(v -> {
                searchResultsViewModel.saveWord(searchedWord);
                saveProgress.setVisibility(View.VISIBLE);
                disableSaveButton(icon);
            });

            if (updateSave) userViewModel.removeSaveItem(searchedWord);
        }

        saveProgress.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(true);
        saveButton.setTextColor(ctx.getColor(R.color.colorPrimary));
        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
    }

    private void disableSaveButton(AtomicReference<Drawable> icon) {
        saveButton.setEnabled(false);
        saveButton.setTextColor(ctx.getColor(R.color.dark_gray));
        icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        icon.set(AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_disabled));
        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
        saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon.get(), null, null, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dictionaryViewModel.popSearchStack();
    }

    private final View.OnClickListener backListener = v -> requireActivity().onBackPressed();

    private final View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveButton.setEnabled(false);
        }
    };

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

                return SearchResultsBodyFragment.newInstance(entryData.get(0));
            } else {
                return SearchResultsBodyFragment.newInstance(entryData.get(0));
            }
        }

        @Override
        public int getItemCount() {
            return entryData.size();
        }
    }
}