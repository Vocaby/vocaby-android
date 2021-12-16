package com.vocaby.app.ui.customentry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.adapters.CustomEntryAdapter
import com.vocaby.app.models.payload.PayloadState
import com.vocaby.app.states.UserInputState
import com.vocaby.app.utils.LiveDataUtil.observeOnce
import com.vocaby.app.utils.StringFormatter
import com.vocaby.app.viewmodels.DictionaryViewModel
import com.vocaby.app.viewmodels.DictionaryViewModelFactory
import com.vocaby.app.viewmodels.MyEntryViewModel
import com.vocaby.app.viewmodels.MyEntryViewModelFactory

class MyEntryFragment : Fragment(), CustomEntryAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var entryCountView: TextView
    private lateinit var customEntryAdapter: CustomEntryAdapter
    private lateinit var emptyCard: LinearLayout
    private lateinit var entryEditDialog: BottomSheetDialog
    private lateinit var entryEdit: EditText
    private lateinit var entryAlert: TextView

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels{
        DictionaryViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }
    private val entryViewModel: MyEntryViewModel by activityViewModels{
        MyEntryViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_entry, container, false)
        entryCountView = view.findViewById(R.id.entry_count)
        emptyCard = view.findViewById(R.id.empty_card)

        setupButtons(view)
        setupEntryBuilderDialog()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)

        entryViewModel.entries.observeOnce(viewLifecycleOwner, {
                customEntries -> customEntryAdapter.submitList(customEntries)
        })

        entryViewModel.customEntryCount.observe(viewLifecycleOwner, { count ->
                entryCountView.text = StringFormatter.cleanNumber(count)
        })

        entryViewModel.entryResult.observe(viewLifecycleOwner) { entryStatePayload ->
            when (entryStatePayload.state) {
                PayloadState.DELETE -> customEntryAdapter.deleteEntry(entryStatePayload.payload)
                PayloadState.ADD -> customEntryAdapter.addEntry()
            }

            if (customEntryAdapter.itemCount > 0 ) emptyCard.visibility = View.INVISIBLE
            else emptyCard.visibility = View.VISIBLE

            dictionaryViewModel.resetSearchSuggestion()
        }

        entryViewModel.entryCreationState.observe(viewLifecycleOwner) { input ->
            when(input) {
                is UserInputState.EmptyInput -> {
                    entryAlert.text = getString(R.string.custom_entry_header_empty)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.InvalidInput -> {
                    entryAlert.text = getString(R.string.custom_entry_header_invalid)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.LongInput -> {
                    entryAlert.text = getString(R.string.custom_entry_header_long)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.Valid -> {
                    entryEdit.text.clear()
                    entryAlert.visibility = View.INVISIBLE
                    entryEditDialog.dismiss()

                    var startEntryBuilderIntent =
                        Intent(requireActivity(), EntryBuilderActivity::class.java)
                    startEntryBuilderIntent = entryViewModel.addEntryDataToIntent(
                        startEntryBuilderIntent,
                        input.data,
                        -1
                    )

                    entryBuilderActivity.launch(startEntryBuilderIntent)
                }
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.custom_entry_container)
        customEntryAdapter = CustomEntryAdapter(requireActivity(), this)
        recyclerView.adapter = customEntryAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)
    }

    private fun setupButtons(view: View) {
        val addButton = view.findViewById<Button>(R.id.add_entry_button)
        addButton.setOnClickListener { entryEditDialog.show() }
    }

    private fun setupEntryBuilderDialog() {
        entryEditDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        entryEditDialog.setContentView(R.layout.custom_entry_header_dialog)
        entryEdit = entryEditDialog.findViewById(R.id.entry_edit)!!
        entryAlert = entryEditDialog.findViewById(R.id.entry_header_alert)!!

        val button = entryEditDialog.findViewById<Button>(R.id.close_button)
        button?.setOnClickListener { entryEditDialog.dismiss() }

        val createButton = entryEditDialog.findViewById<Button>(R.id.dialog_entry_create_button)

        createButton?.setText(R.string.create)
        createButton?.setOnClickListener {
            entryViewModel.createCustomEntry(entryEdit.text.toString())
        }

        // Clear content on show
        entryEditDialog.setOnShowListener {
            entryEdit.text?.clear()
            entryAlert.visibility = View.INVISIBLE
        }
    }

    private val entryBuilderActivity = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult? ->
        entryViewModel.handleResult(result!!)
    }

    override fun onItemDelete(entry: String, position: Int) {
        entryViewModel.removeCustomEntry(entry, position)
    }

    override fun onItemTouch(entry: String, position: Int) {
        var startEntryBuilderIntent = Intent(requireActivity(), EntryBuilderActivity::class.java)
        startEntryBuilderIntent =
            entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, position)
        entryBuilderActivity.launch(startEntryBuilderIntent)
    }
}