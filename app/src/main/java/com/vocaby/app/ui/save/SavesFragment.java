package com.vocaby.app.ui.save;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vocaby.app.R;
import com.vocaby.app.adapters.SavesAdapter;
import com.vocaby.app.models.ItemState;
import com.vocaby.app.ui.MainActivity;
import com.vocaby.app.utils.StringFormatter;
import com.vocaby.app.viewmodels.UserViewModel;


public class SavesFragment extends Fragment implements SavesAdapter.SaveItemTouchListener {
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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getItemStatePayload().observe(getViewLifecycleOwner(), payload -> {
            if (payload.getState() == ItemState.ADD) {
                savesAdapter.addWordToRV();
            } else if (payload.getState() == ItemState.DELETE) {
                savesAdapter.removeWordFromRV(payload.getPayload());
            }
        });

        userViewModel.getSavedWords().observe(getViewLifecycleOwner(),
                savedWords -> savesAdapter.setSavedWords(savedWords)
        );

        userViewModel.getSaveCount().observe(getViewLifecycleOwner(),
                count -> savesCount.setText(StringFormatter.cleanNumber(count))
        );
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.saves_container);
        savesAdapter = new SavesAdapter(ctx, this, getActivity());
        recyclerView.setAdapter(savesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
    }

    @Override
    public void onItemDelete(String entry) {
        userViewModel.removeSaveFromDB(entry);
        userViewModel.setSavesCount();
    }

    @Override
    public void getDefinition(String entry) {
        ((MainActivity) requireActivity()).showDefinition(entry);
    }
}