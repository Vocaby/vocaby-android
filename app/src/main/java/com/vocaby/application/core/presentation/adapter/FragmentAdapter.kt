package com.vocaby.application.core.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryFragment
import com.vocaby.application.feature_dictionary_custom.presentation.ui.MyEntryFragment
import com.vocaby.application.feature_save.presentation.save.SavesFragment
import com.vocaby.application.feature_user.presentation.profile.ProfileFragment

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