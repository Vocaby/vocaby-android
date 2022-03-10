package com.vocaby.application.core.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryFragment
import com.vocaby.application.feature_profile.presentation.profile.ProfileFragment
import com.vocaby.application.feature_save.presentation.save.SaveFragment

class FragmentAdapter (
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            2 -> ProfileFragment()
            0 -> SaveFragment()
            else -> DictionaryFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}