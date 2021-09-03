package com.vocaby.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vocaby.app.ui.DictionaryFragment;
import com.vocaby.app.ui.ProfileFragment;
import com.vocaby.app.ui.ProfileHomeFragment;
import com.vocaby.app.ui.SavesFragment;

public class FragmentAdapter extends FragmentStateAdapter {
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 1:
                return new SavesFragment();
            case 2:
                return new ProfileFragment();
            default:
                return DictionaryFragment.newInstance();
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
