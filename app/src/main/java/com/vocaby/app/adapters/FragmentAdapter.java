package com.vocaby.app.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vocaby.app.ui.DictionaryFragment;
import com.vocaby.app.ui.ProfileFragment;
import com.vocaby.app.ui.ProfileHomeFragment;
import com.vocaby.app.ui.SavesFragment;

import java.util.ArrayList;

public class FragmentAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments.add(new DictionaryFragment());
        fragments.add(new SavesFragment());
        fragments.add(new ProfileFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
