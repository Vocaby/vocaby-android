package com.vocaby.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.vocaby.app.R

class ProfileFragment : Fragment() {
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onResume() {
        super.onResume()
        onBackPressedCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.isEnabled = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().replace(
                R.id.profile_fragment_container,
                ProfileHomeFragment()
            ).commit()
        }

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 0) childFragmentManager.popBackStack()
                if (childFragmentManager.backStackEntryCount == 0) {
                    this.isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, onBackPressedCallback)

        return view
    }
}