package com.vocaby.app.ui.profile

import androidx.appcompat.app.AppCompatActivity
import com.vocaby.app.viewmodels.DataTransferViewModel
import android.os.Bundle
import com.vocaby.app.R
import android.widget.ProgressBar
import android.widget.TextView
import android.content.res.ColorStateList
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import com.vocaby.app.VocabyApplication
import com.vocaby.app.utils.LiveDataUtil.observeOnce
import com.vocaby.app.viewmodels.DataTransferViewModelFactory

class DataTransferActivity : AppCompatActivity() {
    private val dataTransferViewModel: DataTransferViewModel by viewModels {
        DataTransferViewModelFactory((application as VocabyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transfer)

        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            dataTransferViewModel.cancelJob()
            setResult(RESULT_OK, dataTransferViewModel.addResult())
            finish()
        }

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.progress_text)

        val directoryPickerIntent = dataTransferViewModel.handleReceived(intent)
        directorySelector.launch(directoryPickerIntent)
        dataTransferViewModel.progressText.observeOnce(this) { text ->
            progressText.setText(text)
        }

        dataTransferViewModel.transferStatus.observe(this) { successful: Boolean? ->
            progressBar.isIndeterminate = false
            progressBar.max = 1
            progressBar.incrementProgressBy(1)
            if (!successful!!) progressBar.progressTintList =
                ColorStateList.valueOf(getColor(R.color.colorHeadline))
        }
    }

    private val directorySelector = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) dataTransferViewModel.handleResult(result.data)
        else finish()
    }
}