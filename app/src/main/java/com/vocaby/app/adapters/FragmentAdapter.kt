package com.vocaby.app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vocaby.app.ui.customentry.MyEntryFragment
import com.vocaby.app.ui.dictionary.DictionaryFragment
import com.vocaby.app.ui.profile.ProfileFragment
import com.vocaby.app.ui.save.SavesFragment

class FragmentAdapter (
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            3 -> ProfileFragment()
            2 -> MyEntryFragment()
            1 -> SavesFragment()
            else -> DictionaryFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}