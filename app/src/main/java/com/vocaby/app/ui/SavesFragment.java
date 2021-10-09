package com.vocaby.app.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.adapters.SavesAdapter;
import com.vocaby.app.viewmodels.UserViewModel;


public class SavesFragment extends Fragment implements SavesAdapter.OnSaveItemTouch {
    private Context ctx;
    private TextView savesCount;
    private SavesAdapter savesAdapter;
    private ProgressBar progressBar;

    private UserViewModel userViewModel;

    public SavesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = requireActivity().getApplicationContext();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saves, container, false);
        savesCount = view.findViewById(R.id.saves_count);
        progressBar = view.findViewById(R.id.saves_progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        RecyclerView recyclerView = view.findViewById(R.id.saves_container);
        savesAdapter = new SavesAdapter(ctx, this, userViewModel, getActivity());
        recyclerView.setAdapter(savesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        userViewModel.getSavedWords().observe(getViewLifecycleOwner(), savedWords -> {
            savesAdapter.setSavedWords(savedWords);
            setSavesCount(savedWords.size());
        });
    }

    public void setSavesCount(int size) {
        savesCount.setText(String.valueOf(size));
    }

    @Override
    public void changeSaveCount(int size) {
        setSavesCount(size);
    }

    @Override
    public void getDefinition(int position) {
        ((MainActivity) requireActivity()).showDefinition(userViewModel.getSaveItem(position));
    }
}