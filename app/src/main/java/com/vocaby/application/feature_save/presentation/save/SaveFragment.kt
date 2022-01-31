package com.vocaby.application.feature_save.presentation.save

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.vocaby.application.R
import com.vocaby.application.feature_save.presentation.collection.CollectionItemsUiEvent
import com.vocaby.application.feature_save.presentation.collection.SaveCollectionBaseFragment
import com.vocaby.application.feature_save.presentation.collection.SaveCollectionViewModel
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collectLatest

class SaveFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var addCollectionButton: ExtendedFloatingActionButton
    private lateinit var collectionDialog: BottomSheetDialog
    private lateinit var collectionAlert: TextView
    private lateinit var collectionEdit: EditText
    private lateinit var dialogHeader: TextView
    private lateinit var dialogButton: Button
    private lateinit var collectionUpdateDialog: BottomSheetDialog
    private lateinit var collectionEditNameButton: Button
    private lateinit var collectionDeleteButton: Button

    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save, container, false)
        val tabs = view.findViewById<TabLayout>(R.id.save_tab)
        addCollectionButton = view.findViewById(R.id.add_collection_button)

        viewPager = view.findViewById(R.id.save_fragment_container)
        viewPager.adapter = SaveFragmentPagerAdapter(this)
        viewPager.isUserInputEnabled = false

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    if (it.position == 0) {
                        addCollectionButton.hide()
                    } else {
                        addCollectionButton.show()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "ALL SAVES"
            } else {
                tab.text = "COLLECTIONS"
            }
        }.attach()


        setupCollectionDialog()
        setupUpdateDialog()
        setupButtons()

        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is CollectionItemsUiEvent.CloseCollectionDialog -> {
                        collectionDialog.dismiss()
                    }
                    is CollectionItemsUiEvent.ShowUpdateDialog -> {
                        collectionUpdateDialog.dismiss()
                        dialogHeader.setText(R.string.collection_update_name)
                        dialogButton.setText(R.string.update_collection)
                        dialogButton.setOnClickListener {
                            dialogButton.isEnabled = false
                            saveCollectionViewModel.updateCollection(collectionEdit.text.toString())
                        }
                        collectionEdit.setText(event.collectionName)
                        collectionDialog.show()
                    }
                    is CollectionItemsUiEvent.ShowCollectionAlert -> {
                        collectionAlert.visibility = View.VISIBLE
                        collectionAlert.text = event.message
                        dialogButton.isEnabled = true
                    }
                    is CollectionItemsUiEvent.ShowActionsDialog -> {
                        collectionUpdateDialog.show()
                    }
                    else -> {}
                }
            }
        }

        return view
    }

    private fun setupButtons() {
        addCollectionButton.setOnClickListener {
            dialogHeader.setText(R.string.add_a_save_collection)
            dialogButton.setText(R.string.create)
            dialogButton.setOnClickListener {
                dialogButton.isEnabled = false
                saveCollectionViewModel.addSaveCollection(collectionEdit.text.toString())
            }

            collectionDialog.show()
        }
    }

    private fun setupCollectionDialog() {
        collectionDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        collectionDialog.setContentView(R.layout.dialog_collection)
        collectionEdit = collectionDialog.findViewById(R.id.collection_name_input)!!
        collectionAlert = collectionDialog.findViewById(R.id.collection_header_alert)!!
        dialogHeader = collectionDialog.findViewById(R.id.dialog_header)!!
        dialogButton = collectionDialog.findViewById(R.id.dialog_collection_create_button)!!

        // Clear content on show
        collectionDialog.setOnDismissListener {
            collectionEdit.clearFocus()
            collectionEdit.text?.clear()
            collectionAlert.visibility = View.INVISIBLE
            dialogButton.isEnabled = true
        }

        val counter = collectionDialog.findViewById<TextView>(R.id.character_counter)!!
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                counter.text = s.length.toString()
            }
            override fun afterTextChanged(s: Editable) {}
        }

        collectionEdit.addTextChangedListener(textWatcher)
    }

    private fun setupUpdateDialog() {
        collectionUpdateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        collectionUpdateDialog.setContentView(R.layout.dialog_collection_item_action)

        collectionEditNameButton = collectionUpdateDialog.findViewById(R.id.edit_collection_name_button)!!
        collectionDeleteButton = collectionUpdateDialog.findViewById(R.id.delete_collection_button)!!

        collectionEditNameButton.setOnClickListener {
            saveCollectionViewModel.prepareUpdateDialog()
        }

        collectionDeleteButton.setOnClickListener {
            saveCollectionViewModel.removeCollection()
            collectionUpdateDialog.dismiss()
        }
    }


    private inner class SaveFragmentPagerAdapter(fa: Fragment): FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> SaveCollectionBaseFragment()
                else -> AllSavesFragment()
            }
        }
    }
}