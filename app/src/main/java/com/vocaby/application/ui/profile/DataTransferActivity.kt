package com.vocaby.application.ui.profile

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vocaby.application.R
import com.vocaby.application.VocabyApplication
import com.vocaby.application.utils.LiveDataUtil.observeOnce
import com.vocaby.application.viewmodels.DataTransferViewModel
import com.vocaby.application.viewmodels.DataTransferViewModelFactory

class DataTransferActivity : AppCompatActivity() {
    private val dataTransferViewModel: DataTransferViewModel by viewModels {
        DataTransferViewModelFactory((application as VocabyApplication).repository)
    }
    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transfer)

        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            dataTransferViewModel.cancelJob()
            setResult(Activity.RESULT_CANCELED, dataTransferViewModel.addResult())
            finish()
        }

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.progress_text)
        val progressCounter = findViewById<TextView>(R.id.progress_counter)

        val directoryPickerIntent = dataTransferViewModel.handleReceived(intent)
        directorySelector.launch(directoryPickerIntent)
        dataTransferViewModel.progressText.observe(this) { text ->
            progressText.setText(text)
        }

        dataTransferViewModel.progressCounter.observe(this) { count ->
            val text = if (count < 2) {
                "$count entry"
            } else {
                "$count entries"
            }

            progressCounter.text = text
        }

        dataTransferViewModel.transferStatus.observe(this) { successful: Boolean ->
            progressBar.isIndeterminate = false
            progressBar.max = 1
            progressBar.incrementProgressBy(1)
            if (!successful) progressBar.progressTintList =
                ColorStateList.valueOf(getColor(R.color.colorHeadline))
            if (successful) {
                closeButton.setOnClickListener {
                    setResult(Activity.RESULT_OK, dataTransferViewModel.addResult())
                    finish()
                }
            }
        }
    }

    private val directorySelector = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, dataTransferViewModel.addResult())
            dataTransferViewModel.handleResult(result.data)
        }
        else finish()
    }
}