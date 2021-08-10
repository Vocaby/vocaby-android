package com.vocaby.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;


public class SavesFragment extends Fragment implements SavesAdapter.OnItemTouchListener {
    private DataManager dataManager;
    private Context ctx;
    private TextView savesCount;

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
        savesCount = view.findViewById(R.id.saves_count);
        setSavesCount(saves.size());

        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.token_key), Context.MODE_PRIVATE);
        String token = sharedPref.getString(ctx.getString(R.string.token_key), "");
        if(!token.isEmpty()) {
            TextView status = view.findViewById(R.id.network_status_text);
            status.setText("Synced");
            View indicator = view.findViewById(R.id.network_indicator);
            indicator.setBackgroundTintList(ctx.getColorStateList(R.color.colorPrimary));
        }

        RecyclerView recyclerView = view.findViewById(R.id.saves_container);
        SavesAdapter adapter = new SavesAdapter(ctx, this, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        return view;
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
        String search = dataManager.getSaves().get(position);
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
        .setCustomAnimations(
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
        ).replace(R.id.fragment_container, HomeFragment.newInstance(search), "HOME").commit();

        ((MainActivity)requireActivity()).changePrevPage(R.id.search);
    }
}