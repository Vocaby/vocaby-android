package com.vocaby.application.feature_profile.presentation.dangerzone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_dictionary_custom.presentation.home.MyEntryViewModel
import com.vocaby.application.feature_dictionary_custom.presentation.type.TypeManagementViewModel
import com.vocaby.application.feature_profile.presentation.profile.ProfileViewModel
import com.vocaby.application.feature_save.presentation.save.SaveViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DangerZoneFragment : Fragment() {
    private lateinit var builder: MaterialAlertDialogBuilder
    private val saveViewModel: SaveViewModel by activityViewModels()
    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()
    private val myEntryViewModel: MyEntryViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val typeManagementViewModel: TypeManagementViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        builder = MaterialAlertDialogBuilder(requireActivity())
        val view = inflater.inflate(R.layout.fragment_profile_danger_zone, container, false)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        val eraseHistoryButton = view.findViewById<Button>(R.id.erase_history_button)
        eraseHistoryButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to delete your search history?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("DELETE") { _, _ -> dictionaryViewModel.clearHistory() }
                .setNegativeButton("CANCEL", null)
            val alert = builder.create()
            alert.show()
        }

        val eraseSavesButton = view.findViewById<Button>(R.id.erase_saves_button)
        eraseSavesButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to delete your saves?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("DELETE") { _, _ -> saveViewModel.clearSaves()}
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseSaveCollectionsButton = view.findViewById<Button>(R.id.erase_save_collections_button)
        eraseSaveCollectionsButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to delete your save collections?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("DELETE") { _, _ -> saveViewModel.clearSaveCollections()}
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseEntriesButton = view.findViewById<Button>(R.id.erase_custom_entries_button)
        eraseEntriesButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to delete your entries?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("DELETE") { _, _ -> myEntryViewModel.clearUserEntries() }
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseChartDataButton = view.findViewById<Button>(R.id.erase_chart_button)
        eraseChartDataButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to delete your chart data?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("DELETE") { _, _ -> profileViewModel.eraseChartData() }
                .setNegativeButton("CANCEL", null).create().show()
        }

        val resetEntryTypesButton = view.findViewById<Button>(R.id.reset_entry_type_button)
        resetEntryTypesButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to reset?")
                .setMessage("This action will reset your entry types to factory data")
                .setPositiveButton("RESET") { _, _ -> typeManagementViewModel.resetTypes() }
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseDataButton = view.findViewById<Button>(R.id.erase_data_button)
        eraseDataButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to delete all of your data?")
                .setMessage("All of your data will be deleted. This action is irreversible.")
                .setPositiveButton("DELETE") { _, _ ->
                    profileViewModel.reset()
                    typeManagementViewModel.resetTypes()
                }.setNegativeButton("CANCEL", null).create().show()
        }

        return view
    }
}