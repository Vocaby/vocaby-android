package com.vocaby.application.feature_user.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.vocaby.application.R
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.core.util.LiveDataUtil.observeOnce
import com.vocaby.application.feature_user.presentation.adapter.FaqAdapter
import com.vocaby.application.feature_user.presentation.viewmodel.SupportViewModel
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.AndroidEntryPoint

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
        val arrayAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.list_popup_window_item, types)
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

        supportViewModel.faq.observeOnce(viewLifecycleOwner) { faq ->
            faqAdapter.setList(faq)
        }

        supportViewModel.feedbackInput.observe(viewLifecycleOwner) { userInput ->
            when(userInput) {
                is UserInputState.EmptyInput -> {
                    feedbackAlert.setText(R.string.feedback_input_alert_message)
                }
                is UserInputState.NoInput -> {
                    feedbackAlert.setText(R.string.feedback_input_alert_type)
                }
                is UserInputState.InvalidInput -> {
                    feedbackAlert.setText(R.string.feedback_input_alert_email)
                }
                is UserInputState.Valid -> {
                    textInputLayout.isEnabled = false
                    submitFeedbackButton.isEnabled = false
                    autoCompleteTextView.isFocusable = false
                    autoCompleteTextView.isFocusableInTouchMode = false
                    feedbackMessage.isFocusable = false
                    feedbackMessage.isFocusableInTouchMode = false
                    feedbackEmail.isFocusable = false
                    feedbackEmail.isFocusableInTouchMode = false
                }
                else -> {}
            }
        }

        supportViewModel.feedbackState.observe(viewLifecycleOwner) { feedbackState ->
            when(feedbackState) {
                is GenericState.InProgress -> {
                    feedbackAlert.text = ""
                    submitProgress.visibility = View.VISIBLE
                }
                is GenericState.Success -> {
                    submitProgress.visibility = View.GONE
                    feedbackAlert.setText(R.string.feedback_input_alert_thankyou)
                    feedbackAlert.setTextColor(requireActivity().applicationContext.getColor(R.color.colorPrimary))
                }
                is GenericState.Error -> {
                    submitProgress.visibility = View.GONE
                    feedbackAlert.text = feedbackState.exception.message
                    textInputLayout.isEnabled = true
                    submitFeedbackButton.isEnabled = true
                    autoCompleteTextView.isFocusable = true
                    autoCompleteTextView.isFocusableInTouchMode = true
                    feedbackMessage.isFocusable = true
                    feedbackMessage.isFocusableInTouchMode = true
                    feedbackEmail.isFocusable = true
                    feedbackEmail.isFocusableInTouchMode = true
                }
            }
        }
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqAdapter()
        recyclerView.adapter = faqAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext, LinearLayoutManager.HORIZONTAL, false)
    }
}