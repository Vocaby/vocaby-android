package com.vocaby.application.feature_profile.presentation.profile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.Transformer
import com.google.android.material.button.MaterialButton
import com.vocaby.application.R
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_datatransfer.presentation.DataTransferFragment
import com.vocaby.application.feature_profile.presentation.setting.SettingFragment
import com.vocaby.application.feature_profile.presentation.setting.SettingViewModel
import com.vocaby.application.feature_profile.util.AxisValueFormatter
import com.vocaby.application.feature_support.presentation.SupportFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileHomeFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var barChart: BarChart
    private lateinit var placeholder: ShimmerFrameLayout
    private lateinit var chartAlert: TextView
    private lateinit var indicator: View
    private lateinit var favoriteEntry: TextView
    private lateinit var chartToggleButton: MaterialButton
    private lateinit var saveCounter: TextView
    private lateinit var collectionCounter: TextView
    private lateinit var entryCounter: TextView
    private lateinit var settingsButton: Button
    private lateinit var dataTransferButton: Button
    private lateinit var supportButton: Button

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val settingsViewModel: SettingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_main, container, false)
        ctx = requireActivity().applicationContext
        placeholder = view.findViewById(R.id.chart_placeholder)
        barChart = view.findViewById(R.id.bar_chart)
        chartAlert = view.findViewById(R.id.chart_placeholder_alert)
        indicator = view.findViewById(R.id.indicator)
        favoriteEntry = view.findViewById(R.id.favorite_entry)
        chartToggleButton = view.findViewById(R.id.chart_toggle_button)
        saveCounter = view.findViewById(R.id.save_counter)
        collectionCounter = view.findViewById(R.id.collection_counter)
        entryCounter = view.findViewById(R.id.entry_counter)
        settingsButton = view.findViewById(R.id.settings_button)
        supportButton = view.findViewById(R.id.support_button)
        dataTransferButton = view.findViewById(R.id.data_management_button)

        barChart.apply {
            setTouchEnabled(false)
            setScaleEnabled(false)
            extraBottomOffset = 15f
            axisRight.isEnabled = false
            axisLeft.isEnabled = true
            axisLeft.axisLineColor = Color.WHITE
            axisLeft.setDrawLabels(false)
            axisLeft.axisMinimum = 0f
            legend.isEnabled = false
        }

        barChart.xAxis.apply {
            setCenterAxisLabels(false)
            setDrawGridLines(false)
            setDrawAxisLine(false)
            isGranularityEnabled = true
            granularity = 1f
            textSize = 10f
            textColor = ContextCompat.getColor(ctx, R.color.dark_gray)
            typeface = ResourcesCompat.getFont(ctx, R.font.sourcesanspro_semibold)
            position = XAxis.XAxisPosition.BOTTOM
        }

        placeholder.startShimmer()

        barChart.description = Description().apply {
            text = ""
        }

        setupButtons()

        parentFragmentManager.addOnBackStackChangedListener {
            if (parentFragmentManager.backStackEntryCount == 0) {
                enableNav()
            }
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                collectProfileData()
            }

            launch {
                collectChartData()
            }

            launch {
                collectChartMode()
            }
        }

        return view
    }

    private suspend fun collectChartData() {
        profileViewModel.chartState.collectLatest { state ->
            when (state) {
                is ChartState.Success -> {
                    barChart.visibility = View.VISIBLE
                    placeholder.visibility = View.GONE

                    favoriteEntry.text = state.favourite

                    if (state.chartData.isUserData) {
                        indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadline))
                        chartAlert.text = getString(R.string.chart_placeholder_alert)
                        chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                    } else {
                        indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorPrimary))
                        chartAlert.text = getString(R.string.chart_dictionary_data)
                        chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                    }

                    val dataSet: BarDataSet = BarDataSet(state.chartData.entries, "").apply {
                        setDrawValues(true)
                        valueTextColor = ContextCompat.getColor(ctx, R.color.colorPrimaryAccent)
                        colors = state.colors
                        valueTextSize = 10f
                        valueTypeface = ResourcesCompat.getFont(ctx, R.font.sourcesanspro_semibold)
                        valueFormatter = object: ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return String.format("%.0f",value)
                            }
                        }
                    }

                    barChart.data = BarData(dataSet)

                    barChart.apply {
                        animateXY(600, 1000, Easing.EaseInOutQuad)
                        xAxis.labelCount = state.chartData.values.size
                    }

                    val labelTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, ctx.resources.displayMetrics)
                    val transformer: Transformer = barChart.getTransformer(YAxis.AxisDependency.LEFT)
                    val barWidthInPixels = transformer.getPixelForValues(barChart.barData.barWidth, 0f).x - transformer.getPixelForValues(0f, 0f).x
                    barChart.xAxis.valueFormatter = AxisValueFormatter(state.chartData.values, barWidthInPixels, labelTextSize)
                    barChart.invalidate()
                }

                is ChartState.InProgress -> {
                    indicator.background.setTint(ContextCompat.getColor(ctx, R.color.standby))
                    barChart.visibility = View.INVISIBLE
                    placeholder.visibility = View.VISIBLE
                    chartAlert.text = getString(R.string.chart_placeholder_fetching)
                    chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.standby))
                }

                is ChartState.Error -> {
                    indicator.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadline))
                    barChart.visibility = View.INVISIBLE
                    placeholder.visibility = View.VISIBLE
                    chartAlert.text = getString(R.string.chart_placeholder_error)
                    chartAlert.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                }
            }
        }
    }

    private suspend fun collectChartMode() {
        profileViewModel.chartModeAll.collectLatest { displayAll ->
            chartToggleButton.isChecked = displayAll
            chartToggleButton.text = if (displayAll)
                getString(R.string.chart_toggle_all) else getString(R.string.chart_toggle_monthly)
            profileViewModel.updateChart()
        }
    }

    private suspend fun collectProfileData() {
        profileViewModel.profileState.collectLatest { profile ->
            profile?.let {
                saveCounter.text = it.saveCount.toString()
                collectionCounter.text = it.collectionCount.toString()
                entryCounter.text = it.entryCount.toString()
            } ?: run {
                saveCounter.text = "-"
                collectionCounter.text = "-"
                entryCounter.text = "-"
            }
        }
    }

    private fun setupButtons() {
        chartToggleButton.addOnCheckedChangeListener { _: MaterialButton, checked: Boolean ->
            settingsViewModel.changeChartMode(checked)
        }

        // SETTINGS
        settingsButton.setOnClickListener {
            disableNav()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        // DATA TRANSFER
        dataTransferButton.setOnClickListener {
            disableNav()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, DataTransferFragment())
                .addToBackStack(null)
                .commit()
        }

        supportButton.setOnClickListener {
            disableNav()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, SupportFragment())
                .addToBackStack(null)
                .commit()
        }
    }


    private fun disableNav() {
        settingsButton.isEnabled = false
        dataTransferButton.isEnabled = false
        supportButton.isEnabled = false
    }

    private fun enableNav() {
        settingsButton.isEnabled = true
        dataTransferButton.isEnabled = true
        supportButton.isEnabled = true
    }
}