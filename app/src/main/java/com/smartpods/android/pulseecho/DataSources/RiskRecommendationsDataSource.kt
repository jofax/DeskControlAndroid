package com.smartpods.android.pulseecho.DataSources

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.smartpods.android.pulseecho.Model.RiskRecommendations

class RiskRecommendationsDataSource(resources: Resources, type: Int) {
    private val initialData = RiskAssessmentStaticData(resources, type)
    private val riskRecommendationsListData = initialData

    fun getRiskRecommendations(): List<RiskRecommendations> {
        return riskRecommendationsListData
    }

    companion object {
        private var INSTANCE: RiskRecommendationsDataSource? = null

        fun getDataSource(resources: Resources, type: Int): RiskRecommendationsDataSource {
            return synchronized(RiskRecommendationsDataSource::class) {
                val newInstance = INSTANCE ?: RiskRecommendationsDataSource(resources,type)
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}