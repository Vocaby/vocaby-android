package com.vocaby.application.feature_save.presentation.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.vocaby.application.R
import com.vocaby.application.feature_save.presentation.collection.SaveCollectionBaseFragment

class SaveFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var appBar: AppBarLayout
    private lateinit var tabs: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save, container, false)
        tabs = view.findViewById(R.id.save_tab)
        appBar = view.findViewById(R.id.save_app_bar)
        appBar.outlineProvider = null

        viewPager = view.findViewById(R.id.save_fragment_container)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter = SaveFragmentPagerAdapter(this)
        viewPager.addItemDecoration(DividerItemDecoration(requireActivity(), RecyclerView.HORIZONTAL))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "COLLECTIONS"
            } else {
                tab.text = "ALL SAVES"
            }
        }.attach()
    }


    private inner class SaveFragmentPagerAdapter(fa: Fragment): FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> AllSavesFragment()
                else -> SaveCollectionBaseFragment()
            }
        }
    }
}