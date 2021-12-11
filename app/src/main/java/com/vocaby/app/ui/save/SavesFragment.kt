package com.vocaby.app.ui.save

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.adapters.SaveListAdapter
import com.vocaby.app.ui.MainActivity
import com.vocaby.app.viewmodels.UserViewModel
import com.vocaby.app.viewmodels.UserViewModelFactory

class SavesFragment : Fragment(), SaveListAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var savesAdapter: SaveListAdapter
    private lateinit var savesCount: TextView
    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saves, container, false)
        savesCount = view.findViewById(R.id.saves_count)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)

        userViewModel.savedWords.observe(viewLifecycleOwner) { saves ->
            saves?.let {
                savesAdapter.submitList(saves)
            }
        }

        userViewModel.savesCount.observe(viewLifecycleOwner) { count ->
            count?.let {
                savesCount.text = count.toString()
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.saves_container)
        savesAdapter = SaveListAdapter(requireActivity(), this)
        recyclerView.adapter = savesAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)
    }

    override fun onItemDelete(entry: String) {
        userViewModel.removeSaveItem(entry)
    }

    override fun onItemTouch(entry: String) {
        (requireActivity() as MainActivity).showDefinition(entry)
    }
}