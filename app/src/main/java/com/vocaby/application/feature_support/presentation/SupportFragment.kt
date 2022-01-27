package com.vocaby.application.feature_support.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.vocaby.application.R
import com.vocaby.application.core.util.ResourceState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SupportFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var faqAdapter: FaqAdapter
    private lateinit var feedbackAlert: TextView
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var submitFeedbackButton: Button
    private lateinit var feedbackMessage: EditText
    private lateinit var feedbackEmail: EditText
    private lateinit var submitProgress: ProgressBar
    private val supportViewModel: SupportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_support, container, false)
        feedbackAlert = view.findViewById(R.id.feedback_input_alert)
        submitProgress = view.findViewById(R.id.submit_progress_bar)
        feedbackMessage = view.findViewById(R.id.feedback_input_content)
        feedbackEmail = view.findViewById(R.id.feedback_input_email)
        recyclerView = view.findViewById(R.id.faq_recyclerview)

        setupButtons(view)
        setupRecyclerView()
        return view
    }

    private fun setupButtons(view: View) {
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        textInputLayout = view.findViewById(R.id.feedback_input_type)
        autoCompleteTextView = view.findViewById(R.id.autocomplete_text)
        val types = resources.getStringArray(R.array.feedback_type)
        val arrayAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.item_list_popup_window, types)
        autoCompleteTextView.setAdapter(arrayAdapter)

        submitFeedbackButton = view.findViewById(R.id.feedback_input_submit)
        submitFeedbackButton.setOnClickListener {
            supportViewModel.submitFeedback(
                autoCompleteTextView.text.toString(),
                feedbackMessage.text.toString(),
                feedbackEmail.text.toString()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            supportViewModel.faq.collectLatest { faq ->
                faqAdapter.setList(faq)
            }
        }

        lifecycleScope.launchWhenStarted {
            supportViewModel.feedbackState.collectLatest { state ->
                when (state) {
                    is ResourceState.InProgress -> {
                        feedbackAlert.text = ""
                        submitProgress.visibility = View.VISIBLE
                        enableFeedbackForm(false)
                    }
                    is ResourceState.Error -> {
                        if (state.uiText.text != null) {
                            feedbackAlert.text = state.uiText.text
                        } else if (state.uiText.textResource != null) {
                            feedbackAlert.setText(state.uiText.textResource)
                        }

                        enableFeedbackForm(true)
                        submitProgress.visibility = View.GONE

                    }
                    is ResourceState.Success -> {
                        submitProgress.visibility = View.GONE
                        feedbackAlert.setText(R.string.feedback_input_alert_thankyou)
                        feedbackAlert.setTextColor(requireActivity().applicationContext.getColor(R.color.colorPrimary))
                    }
                }

            }
        }
    }

    private fun enableFeedbackForm(enabled: Boolean) {
        textInputLayout.isEnabled = enabled
        submitFeedbackButton.isEnabled = enabled
        autoCompleteTextView.isFocusable = enabled
        autoCompleteTextView.isFocusableInTouchMode = enabled
        feedbackMessage.isFocusable = enabled
        feedbackMessage.isFocusableInTouchMode = enabled
        feedbackEmail.isFocusable = enabled
        feedbackEmail.isFocusableInTouchMode = enabled
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqAdapter()
        recyclerView.adapter = faqAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext, LinearLayoutManager.HORIZONTAL, false)
    }
}