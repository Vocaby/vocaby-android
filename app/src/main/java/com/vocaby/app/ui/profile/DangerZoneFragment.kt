package com.vocaby.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.viewmodels.*

class DangerZoneFragment : Fragment() {
    private lateinit var builder: MaterialAlertDialogBuilder
    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }
    private val dictionaryViewModel: DictionaryViewModel by activityViewModels {
        DictionaryViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    private val myEntryViewModel: MyEntryViewModel by activityViewModels {
        MyEntryViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    private val profileViewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

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
            builder.setTitle("Are you sure you wish to clear your search history?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("CLEAR") { _, _ -> dictionaryViewModel.clearHistory() }
                .setNegativeButton("CANCEL", null)
            val alert = builder.create()
            alert.show()
        }

        val eraseSavesButton = view.findViewById<Button>(R.id.erase_saves_button)
        eraseSavesButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to clear your saves?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("CLEAR") { _, _ -> userViewModel.clearSaves()}
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseEntriesButton = view.findViewById<Button>(R.id.erase_custom_entries_button)
        eraseEntriesButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to clear your entries?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("CLEAR") { _, _ -> myEntryViewModel.clearEntries() }
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseChartDataButton = view.findViewById<Button>(R.id.erase_chart_button)
        eraseChartDataButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to clear your entries?")
                .setMessage("This action is irreversible.")
                .setPositiveButton("CLEAR") { _, _ -> profileViewModel.eraseChartData() }
                .setNegativeButton("CANCEL", null).create().show()
        }

        val eraseDataButton = view.findViewById<Button>(R.id.erase_data_button)
        eraseDataButton.setOnClickListener {
            builder.setTitle("Are you sure you wish to erase your data?")
                .setMessage("All of your data will be deleted. This action is irreversible.")
                .setPositiveButton("ERASE") { _, _ ->
                    userViewModel.clearSaves()
                    dictionaryViewModel.clearHistory()
                    myEntryViewModel.clearEntries()
                    profileViewModel.eraseChartData()
                }.setNegativeButton("CANCEL", null).create().show()
        }

        return view
    }
}