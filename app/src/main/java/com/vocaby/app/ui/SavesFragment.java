package com.vocaby.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.vocaby.app.R;
import com.vocaby.app.adapters.SavesAdapter;
import com.vocaby.app.viewmodels.UserViewModel;


public class SavesFragment extends Fragment implements SavesAdapter.OnItemTouchListener {
    private Context ctx;
    private TextView savesCount;
    private SavesAdapter savesAdapter;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saves, container, false);
        savesCount = view.findViewById(R.id.saves_count);

        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.token_key), Context.MODE_PRIVATE);
        String token = sharedPref.getString(ctx.getString(R.string.token_key), "");
        if(!token.isEmpty()) {
            TextView status = view.findViewById(R.id.network_status_text);
            status.setText(getString(R.string.synced));
            View indicator = view.findViewById(R.id.network_indicator);
            indicator.setBackgroundTintList(ctx.getColorStateList(R.color.colorPrimary));
        }

        RecyclerView recyclerView = view.findViewById(R.id.saves_container);
        savesAdapter = new SavesAdapter(ctx, this, getActivity());
        recyclerView.setAdapter(savesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getSavedWords().observe(getViewLifecycleOwner(), savedWords -> {
            savesAdapter.updateSavedWords(savedWords);
            setSavesCount(savedWords.size());
        });
    }

    public void setSavesCount(int size) {
        savesCount.setText(String.valueOf(size));
    }

    @Override
    public void onSaveDelete(int size) {
        setSavesCount(size);
    }

    @Override
    public void onItemTouch(int position) {
        ((MainActivity) requireActivity()).showDefinition(userViewModel.getSaveItem(position));
    }
}