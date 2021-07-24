package com.vocaby.app;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class SavesFragment extends Fragment implements SavesAdapter.OnItemTouchListener {
    private DataManager dataManager;
    private Context ctx;

    public SavesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireActivity().getApplicationContext();
        dataManager = DataManager.getInstance(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saves, container, false);
        List<String> saves = dataManager.getSaves();

        if(saves.size() > 0) {
            TextView savesAlert = view.findViewById(R.id.saves_alert);
            savesAlert.setVisibility(View.INVISIBLE);
        }

        RecyclerView recyclerView = view.findViewById(R.id.saves_container);
        SavesAdapter adapter = new SavesAdapter(ctx, saves, this, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
    }

    @Override
    public void onItemTouch(int position) {
        String search = dataManager.getSaves().get(position);
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
        .setCustomAnimations(
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
        ).replace(R.id.fragment_container, HomeFragment.newInstance(search)).commit();

        ((MainActivity)requireActivity()).changePrevPage(R.id.search);
    }
}