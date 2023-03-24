package com.smartpods.android.pulseecho.ViewModel

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartpods.android.pulseecho.DataSources.RiskRecommendationsDataSource
import com.smartpods.android.pulseecho.Model.RiskRecommendations

class HomeHealthRiskViewModel() : BaseViewModel() {
    fun getRiskRecommendationList(resource: Resources, type: Int): List <RiskRecommendations> {
        var dataSource = RiskRecommendationsDataSource(resource,type)
        return  dataSource.getRiskRecommendations()
    }
}