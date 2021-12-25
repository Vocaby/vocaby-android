package com.vocaby.app.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vocaby.app.Constants.VOCABY_BASE_URL
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.states.GenericState
import com.vocaby.app.ui.WebActivity
import com.vocaby.app.utils.AxisValueFormatter
import com.vocaby.app.viewmodels.ProfileViewModel
import com.vocaby.app.viewmodels.ProfileViewModelFactory

class ProfileHomeFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var barChart: BarChart
    private lateinit var placeholder: ShimmerFrameLayout
    private lateinit var chartContainer: LinearLayout
    private lateinit var chartAlert: TextView
    private lateinit var indicator: View
    private lateinit var favoriteEntry: TextView

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_main, container, false)
        ctx = requireActivity().applicationContext
        placeholder = view.findViewById(R.id.chart_placeholder)
        chartContainer = view.findViewById(R.id.chart_container)
        barChart = view.findViewById(R.id.bar_chart)
        chartAlert = view.findViewById(R.id.chart_placeholder_alert)
        indicator = view.findViewById(R.id.indicator)
        favoriteEntry = view.findViewById(R.id.favorite_entry)

        barChart.apply {
            setTouchEnabled(false)
            setScaleEnabled(false)
            setViewPortOffsets(0f, 40f, 0f, 50f)
            minOffset = 0f
            axisRight.isEnabled = false
            axisLeft.isEnabled = true
            axisLeft.axisLineColor = Color.WHITE
            axisLeft.axisMinimum = 0f
            legend.isEnabled = false
        }

        barChart.xAxis.apply {
            setCenterAxisLabels(false)
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textSize = 10f
            textColor = ContextCompat.getColor(ctx, R.color.dark_gray)
            typeface = ResourcesCompat.getFont(ctx, R.font.sourcesanspro_semibold)
            position = XAxis.XAxisPosition.BOTTOM
        }

        placeholder.startShimmer()

        barChart.description = Description().apply {
            text = ""
        }

        setupButtons(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.updateChart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.values.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GenericState.Success -> {
                    if (state.data.values.size < 3) {
                        indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadline))
                        chartAlert.text = getString(R.string.chart_placeholder_alert)
                        chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                        chartContainer.visibility = View.GONE
                    } else {
                        indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorPrimary))
                        chartContainer.visibility = View.VISIBLE
                        placeholder.visibility = View.GONE
                        chartAlert.text = ""

                        barChart.apply {
                            animateXY(600, 1000, Easing.EaseInOutQuad)
                            xAxis.labelCount = state.data.values.size
                        }

                        val colorsList: ArrayList<Int> = ArrayList()
                        colorsList.add(Color.parseColor("#7BB38D"))
                        colorsList.add(Color.parseColor("#89BB99"))
                        colorsList.add(Color.parseColor("#A6CCB0"))
                        colorsList.add(Color.parseColor("#C3DDC7"))
                        colorsList.add(Color.parseColor("#D1E5D3"))

                        val dataSet = BarDataSet(state.data.entries, "").apply {
                            setDrawValues(true)
                            colors = colorsList
                            valueTextColor = ContextCompat.getColor(ctx, R.color.colorPrimaryAccent)
                            valueTextSize = 10f
                            valueTypeface = ResourcesCompat.getFont(ctx, R.font.sourcesanspro_black)
                            valueFormatter = object: ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return String.format("%.0f",value)
                                }
                            }
                        }

                        val data = BarData(dataSet).apply {
                            barWidth = 0.85f
                        }

                        barChart.data = data
                        barChart.xAxis.valueFormatter = AxisValueFormatter(state.data.values, 12)
                        barChart.invalidate()
                    }
                }

                is GenericState.InProgress -> {
                    indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorPrimary))
                    chartContainer.visibility = View.GONE
                    placeholder.visibility = View.VISIBLE
                    chartAlert.text = getString(R.string.chart_placeholder_fetching)
                    chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                }

                else -> {
                    indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadline))
                    chartContainer.visibility = View.GONE
                    placeholder.visibility = View.VISIBLE
                    chartAlert.text = getString(R.string.chart_placeholder_error)
                    chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                }
            }
        }

        profileViewModel.favoriteEntry.observe(viewLifecycleOwner) { entry ->
            favoriteEntry.text = entry
        }
    }

    private fun setupButtons(view: View) {
        // NOTIFICATION
        val notificationButton = view.findViewById<Button>(R.id.notification_button)
        notificationButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, NotificationFragment())
                .addToBackStack(null)
                .commit()
        }

        // DATA MANAGEMENT
        val dataButton = view.findViewById<Button>(R.id.data_management_button)
        dataButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, DataManagementFragment())
                .addToBackStack(null)
                .commit()
        }

        val supportButton = view.findViewById<Button>(R.id.support_button)
        supportButton.setOnClickListener {
            val intent = Intent(requireActivity().applicationContext, WebActivity::class.java)
            intent.putExtra("URL", VOCABY_BASE_URL + "support")
            startActivity(intent)
        }

        // DANGER ZONE
        val dangerButton = view.findViewById<Button>(R.id.danger_zone_button)
        dangerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, DangerZoneFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}