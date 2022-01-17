package com.vocaby.application.feature_user.presentation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vocaby.application.R
import com.vocaby.application.feature_customdictionary.presentation.viewmodel.MyEntryViewModel
import com.vocaby.application.feature_user.presentation.viewmodel.DataTransferViewModel
import com.vocaby.application.feature_user.presentation.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataManagementFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val entryViewModel: MyEntryViewModel by activityViewModels()

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.fragment_profile_data_management, container, false)
        val startDataTransferActivity = Intent(requireActivity(), DataTransferActivity::class.java)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        val exportSaveForBackup = view.findViewById<Button>(R.id.export_save_backup_button)
        exportSaveForBackup.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.EXPORT_SAVE_BACKUP)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        val exportEntriesForBackup = view.findViewById<Button>(R.id.export_entries_button)
        exportEntriesForBackup.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.EXPORT_ENTRY_BACKUP)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        val importSave = view.findViewById<Button>(R.id.import_saves_button)
        importSave.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.IMPORT_SAVE)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        val importEntries = view.findViewById<Button>(R.id.import_entries_button)
        importEntries.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.IMPORT_ENTRY)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        val connectionSwitch = view.findViewById<Switch>(R.id.connections_switch)
        connectionSwitch.isChecked = userViewModel.isUseConnectionEnabled()
        connectionSwitch.setOnCheckedChangeListener { _, enabled ->
            userViewModel.setConnectionSettings(enabled)
        }

        val dataShareSwitch = view.findViewById<Switch>(R.id.data_share_switch)
        dataShareSwitch.isChecked = userViewModel.isDataShareEnabled()
        dataShareSwitch.setOnCheckedChangeListener { _, enabled ->
            userViewModel.setDataShareSettings(enabled)
        }

        return view
    }

    private val dataTransferActivity  = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.data!!.getIntExtra("TYPE", -1)) {
                DataTransferViewModel.IMPORT_ENTRY -> {
                    entryViewModel.initializeEntries()
                }
            }
        }
    }
}