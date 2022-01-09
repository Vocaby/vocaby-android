package com.vocaby.app.ui.profile

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
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.adapters.FaqAdapter
import com.vocaby.app.states.GenericState
import com.vocaby.app.states.UserInputState
import com.vocaby.app.utils.LiveDataUtil.observeOnce
import com.vocaby.app.viewmodels.SupportViewModel
import com.vocaby.app.viewmodels.SupportViewModelFactory

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
    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_support, container, false)
        feedbackAlert = view.findViewById(R.id.feedback_input_alert)
        submitProgress = view.findViewById(R.id.submit_progress_bar)
        feedbackMessage = view.findViewById(R.id.feedback_input_content)
        feedbackEmail = view.findViewById(R.id.feedback_input_email)

        setupButtons(view)
        recyclerView = view.findViewById(R.id.faq_recyclerview)
        setupRecyclerView()
        return view
    }

    fun setupButtons(view: View) {
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
                    submitProgress.visibility = View.VISIBLE
                }
                is GenericState.Success -> {
                    submitProgress.visibility = View.INVISIBLE
                    feedbackAlert.setText(R.string.feedback_input_alert_thankyou)
                    feedbackAlert.setTextColor(requireActivity().applicationContext.getColor(R.color.colorPrimary))
                }
                is GenericState.Error -> {
                    submitProgress.visibility = View.INVISIBLE
                    feedbackAlert.setText(R.string.feedback_input_alert_oops)
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