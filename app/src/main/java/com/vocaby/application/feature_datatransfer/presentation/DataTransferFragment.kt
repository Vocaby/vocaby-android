package com.vocaby.application.feature_datatransfer.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.vocaby.application.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataTransferFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.fragment_profile_data_transfer, container, false)
        val dataTransferActivity = Intent(requireActivity(), DataTransferActivity::class.java)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        val exportSaveForBackup = view.findViewById<Button>(R.id.export_save_backup_button)
        exportSaveForBackup.setOnClickListener {
            dataTransferActivity.putExtra("TYPE", DataTransferViewModel.EXPORT_SAVE_BACKUP)
            startActivity(dataTransferActivity)
        }

        val exportEntriesForBackup = view.findViewById<Button>(R.id.export_entries_button)
        exportEntriesForBackup.setOnClickListener {
            dataTransferActivity.putExtra("TYPE", DataTransferViewModel.EXPORT_ENTRY_BACKUP)
            startActivity(dataTransferActivity)
        }

        val importSave = view.findViewById<Button>(R.id.import_saves_button)
        importSave.setOnClickListener {
            dataTransferActivity.putExtra("TYPE", DataTransferViewModel.IMPORT_SAVE)
            startActivity(dataTransferActivity)
        }

        val importEntries = view.findViewById<Button>(R.id.import_entries_button)
        importEntries.setOnClickListener {
            dataTransferActivity.putExtra("TYPE", DataTransferViewModel.IMPORT_ENTRY)
            startActivity(dataTransferActivity)
        }

        return view
    }
}