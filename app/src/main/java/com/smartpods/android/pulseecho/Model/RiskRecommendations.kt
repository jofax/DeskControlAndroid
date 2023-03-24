package com.smartpods.android.pulseecho.Model

import androidx.annotation.DrawableRes
import org.json.JSONObject

class RiskRecommendations(
    val riskType: Int,
    val content: String,
    val status: Int,
    @DrawableRes
    val image: Int?
)