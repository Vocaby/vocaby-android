package com.vocaby.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vocaby.app.ui.customentry.MyEntryFragment;
import com.vocaby.app.ui.dictionary.DictionaryFragment;
import com.vocaby.app.ui.profile.ProfileFragment;
import com.vocaby.app.ui.save.SavesFragment;

public class FragmentAdapter extends FragmentStateAdapter {
    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 3:
                return new ProfileFragment();
            case 2:
                return new MyEntryFragment();
            case 1:
                return new SavesFragment();
            default:
                return new DictionaryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
