package com.vocaby.app.ui.dictionary

import androidx.activity.OnBackPressedCallback
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.viewmodels.DictionaryViewModelFactory
import com.vocaby.app.viewmodels.DictionaryViewModelKt

class DictionaryFragment : Fragment() {
    private lateinit var backPressedCallback: OnBackPressedCallback
    override fun onResume() {
        super.onResume()
        if (childFragmentManager.backStackEntryCount > 0) backPressedCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.isEnabled = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().replace(
                R.id.dictionary_fragment_container,
                DictionaryHomeFragment()
            ).commit()
        }

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 0) childFragmentManager.popBackStack()
                if (childFragmentManager.backStackEntryCount == 0) {
                    this.isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, backPressedCallback)

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dictionaryViewModel: DictionaryViewModelKt by activityViewModels{
            DictionaryViewModelFactory((requireActivity().application as VocabyApplication).repository)
        }

        dictionaryViewModel.searchedEntry.observe(viewLifecycleOwner, {
            if (parentFragmentManager.backStackEntryCount == 0) {
                backPressedCallback.isEnabled = true
            }
        })
    }
}