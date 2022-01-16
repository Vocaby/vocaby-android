package com.vocaby.application.feature_user.domain.model

import com.github.mikephil.charting.data.BarEntry

data class ChartData(val values: ArrayList<String>, val entries: ArrayList<BarEntry>, val isUserData: Boolean)
