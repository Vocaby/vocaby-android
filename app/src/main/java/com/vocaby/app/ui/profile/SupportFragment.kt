package com.vocaby.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.adapters.FaqAdapter
import com.vocaby.app.utils.LiveDataUtil.observeOnce
import com.vocaby.app.viewmodels.SupportViewModel
import com.vocaby.app.viewmodels.SupportViewModelFactory

class SupportFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var faqAdapter: FaqAdapter
    private val supportViewModel: SupportViewModel by viewModels {
        SupportViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_support, container, false)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        recyclerView = view.findViewById(R.id.faq_recyclerview)
        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportViewModel.faq.observeOnce(viewLifecycleOwner) { faq ->
            faqAdapter.setList(faq)
        }
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqAdapter()
        recyclerView.adapter = faqAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext, LinearLayoutManager.HORIZONTAL, false)
    }
}