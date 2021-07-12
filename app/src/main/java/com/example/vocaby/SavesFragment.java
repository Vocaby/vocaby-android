package com.example.vocaby;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavesFragment extends Fragment implements SavesAdapter.OnItemTouchListener {
    private static final String SAVE_DATA = "saves";

    private DataManager dataManager;
    private Context ctx;

    public SavesFragment() {
        // Required empty public constructor
    }

    public static SavesFragment newInstance(DataManager dataManager) {
        SavesFragment fragment = new SavesFragment();
        Bundle args = new Bundle();
        args.putSerializable(SAVE_DATA, dataManager);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataManager = (DataManager) getArguments().getSerializable(SAVE_DATA);
        }

        ctx = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saves, container, false);
        List<String> saves = dataManager.getSaves();

        RecyclerView recyclerView = view.findViewById(R.id.saves_container);
        SavesAdapter adapter = new SavesAdapter(ctx, saves, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }

    @Override
    public void onItemTouch(int position) {
        String search = dataManager.getSaves().get(position);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().replace(R.id.fragment_container, HomeFragment.newInstance(dataManager, search)).commit();
    }
}