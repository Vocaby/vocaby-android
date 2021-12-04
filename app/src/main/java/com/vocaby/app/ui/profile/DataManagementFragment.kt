package com.vocaby.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.vocaby.app.R
import com.vocaby.app.utils.Logger
import com.vocaby.app.viewmodels.DataTransferViewModel

class DataManagementFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.fragment_profile_data_management, container, false)
        val startDataTransferActivity = Intent(requireActivity(), DataTransferActivity::class.java)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        val exportSave = view.findViewById<Button>(R.id.export_save_button)
        exportSave.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.EXPORT_SAVE)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        val exportSaveForBackup = view.findViewById<Button>(R.id.export_save_backup_button)
        exportSaveForBackup.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.EXPORT_SAVE_BACKUP)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        val importSave = view.findViewById<Button>(R.id.import_saves_button)
        importSave.setOnClickListener {
            startDataTransferActivity.putExtra("TYPE", DataTransferViewModel.IMPORT_SAVE)
            dataTransferActivity.launch(startDataTransferActivity)
        }

        return view
    }

    private val dataTransferActivity  = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            when (result.data!!.getIntExtra("TYPE", -1)) {
                DataTransferViewModel.IMPORT_SAVE -> {}
                DataTransferViewModel.IMPORT_ENTRY -> {}
            }
        } else {
            Logger.reportToDebug("CANCELED")
        }
    }
}