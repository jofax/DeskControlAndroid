package com.smartpods.android.pulseecho.DataSources

import android.content.res.Resources
import com.smartpods.android.pulseecho.Model.RiskRecommendations
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R

fun LowRiskRecommendationsList(resources: Resources) : List <RiskRecommendations> {
    return  listOf(
        RiskRecommendations(0, appContext.getString(R.string.risk_management_content1), 1, R.drawable.activity_sub),
        RiskRecommendations(0, appContext.getString(R.string.risk_management_content2), 1, R.drawable.desk_sub),
        RiskRecommendations(0, appContext.getString(R.string.risk_management_content3), 1, R.drawable.profile_sub),
        RiskRecommendations(0, appContext.getString(R.string.risk_management_content4), 0, R.drawable.survey_sub),
    )
}

fun MediumAndLowRiskRecommendationsList(resources: Resources) : List <RiskRecommendations> {
    return  listOf(
        RiskRecommendations(0, "15-30 ,min of standing/h", 0, R.drawable.activity_sub),
        RiskRecommendations(0, "Automatic or Interactive", 0, R.drawable.desk_sub),
        RiskRecommendations(0, "Height, weight and age", 0, R.drawable.profile_sub),
        RiskRecommendations(0, "Health survey", 0, R.drawable.survey_sub),
    )
}

fun RiskAssessmentStaticData(resources: Resources, type: Int):  List <RiskRecommendations>{
    return when(type) {
        0 -> {
            LowRiskRecommendationsList(resources)
        }
        1,2 -> {
            MediumAndLowRiskRecommendationsList(resources)
        }
        else -> {
            listOf<RiskRecommendations>()
        }
    }
}