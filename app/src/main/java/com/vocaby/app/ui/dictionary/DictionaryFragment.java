package com.vocaby.app.ui.dictionary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vocaby.app.R;
import com.vocaby.app.viewmodels.DictionaryViewModel;


public class DictionaryFragment extends Fragment {
    private OnBackPressedCallback backPressedCallback;


    public DictionaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getChildFragmentManager().getBackStackEntryCount() > 0) backPressedCallback.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        backPressedCallback.setEnabled(false);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().replace(R.id.dictionary_fragment_container,
                    new DictionaryHomeFragment()).commit();
        }

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 0)
                    getChildFragmentManager().popBackStack();
                if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backPressedCallback);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DictionaryViewModel dictionaryViewModel =
                new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), string -> {
            if (getParentFragmentManager().getBackStackEntryCount() == 0) {
                backPressedCallback.setEnabled(true);
            }
        });
    }
}