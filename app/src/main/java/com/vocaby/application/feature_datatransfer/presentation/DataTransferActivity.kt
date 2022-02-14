package com.vocaby.application.feature_datatransfer.presentation

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vocaby.application.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DataTransferActivity : AppCompatActivity() {
    private val dataTransferViewModel: DataTransferViewModel by viewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transfer)

        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            dataTransferViewModel.cancelJob()
            setResult(Activity.RESULT_CANCELED, dataTransferViewModel.addResult())
            finish()
        }


        cancelButton = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            dataTransferViewModel.cancelJob()
        }

        progressBar = findViewById(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.progress_text)
        val progressCounter = findViewById<TextView>(R.id.progress_counter)

        val directoryPickerIntent = dataTransferViewModel.handleReceived(intent)
        directorySelector.launch(directoryPickerIntent)

        lifecycleScope.launchWhenStarted {
            dataTransferViewModel.transferState.collectLatest { transferState ->
                when (transferState) {
                    is DataTransferState.Success -> {
                        setProgressBar(true)
                        cancelButton.visibility = View.GONE
                        transferState.message.text?.let { progressText.text = it }
                            ?: transferState.message.textResource?.let { progressText.setText(it) }

                        closeButton.setOnClickListener {
                            setResult(Activity.RESULT_OK, dataTransferViewModel.addResult())
                            finish()
                        }
                    }
                    is DataTransferState.InProgress -> {
                        transferState.message.text?.let { progressText.text = it }
                            ?: transferState.message.textResource?.let { progressText.setText(it) }

                        transferState.countMessage?.let {
                            progressCounter.text = it
                        }
                    }
                    is DataTransferState.Error -> {
                        setProgressBar(false)
                        cancelButton.visibility = View.GONE
                        transferState.uiText.text?.let { progressText.text = it }
                            ?: transferState.uiText.textResource?.let { progressText.setText(it) }

                        progressCounter.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun setProgressBar(successful: Boolean) {
        progressBar.isIndeterminate = false
        progressBar.max = 1
        progressBar.incrementProgressBy(1)
        if (!successful) progressBar.progressTintList = ColorStateList.valueOf(getColor(R.color.colorHeadline))
    }

    private val directorySelector = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, dataTransferViewModel.addResult())
            dataTransferViewModel.handleResult(result.data)
        }
        else finish()
    }
}