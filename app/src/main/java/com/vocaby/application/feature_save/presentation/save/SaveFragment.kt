package com.vocaby.application.feature_save.presentation.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vocaby.application.R
import com.vocaby.application.feature_save.presentation.collection.SaveCollectionFragment
import com.vocaby.application.feature_save.presentation.collection.SaveCollectionViewModel


class SaveFragment : Fragment() {
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        saveCollectionViewModel.updateSaveCollections()
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
        val view = inflater.inflate(R.layout.fragment_save, container, false)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().replace(
                R.id.save_collection_fragment_container,
                SaveCollectionFragment()
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