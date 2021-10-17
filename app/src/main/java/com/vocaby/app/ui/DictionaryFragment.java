package com.vocaby.app.ui;

import android.animation.ValueAnimator;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vocaby.app.R;
import com.vocaby.app.utils.PaintUtil;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.DictionaryViewModel;


public class DictionaryFragment extends Fragment {
    private DictionaryViewModel dictionaryViewModel;

    private TextView dictionaryHeaderVocaby;
    private TextView dictionaryHeaderDictionary;
    private TextView entryCounter;

    private OnBackPressedCallback backPressedCallback;


    public DictionaryFragment() {
        // Required empty public constructor
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

        dictionaryHeaderVocaby = view.findViewById(R.id.header_text);
        dictionaryHeaderDictionary = view.findViewById(R.id.header_dictionary);
        entryCounter = view.findViewById(R.id.header_dictionary_counter);

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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dictionaryViewModel = new ViewModelProvider(requireActivity()).get(DictionaryViewModel.class);

        dictionaryViewModel.getSearch().observe(getViewLifecycleOwner(), string -> {
            if (getParentFragmentManager().getBackStackEntryCount() == 0) {
                backPressedCallback.setEnabled(true);
                slideUpHeader();
            }
        });

        dictionaryViewModel.getEntryCount().observe(getViewLifecycleOwner(), count ->
                entryCounter.setText(StringFormatter.cleanNumber(count))
        );
    }

    public void slideUpHeader() {
        final LinearLayout.LayoutParams headerParams =
                (LinearLayout.LayoutParams) dictionaryHeaderDictionary.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(headerParams.topMargin, 0);
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderDictionary.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    public void slideDownHeader() {
        final LinearLayout.LayoutParams headerParams =
                (LinearLayout.LayoutParams) dictionaryHeaderDictionary.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) getResources().getDimension(R.dimen.dictionary_header_margin_before_slide));
        animator.addUpdateListener(valueAnimator -> {
            headerParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
            dictionaryHeaderDictionary.setLayoutParams(headerParams);
        });

        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }
}