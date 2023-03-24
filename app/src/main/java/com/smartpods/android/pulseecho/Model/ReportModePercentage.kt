package com.smartpods.android.pulseecho.Model
import org.json.JSONObject

class ReportModePercentage(var obj: JSONObject) {

    private var Automatic: Double = 0.0
    private var Manual: Double = 0.0
    private var SemiAutomatic: Double = 0.0

    init{
        SemiAutomatic = obj["SemiAutomatic"] as Double
        Automatic = obj["Automatic"] as Double
        Manual = obj["Manual"] as Double
    }

}