package com.smartpods.android.pulseecho.Model
import org.json.JSONObject

class ReportActivityByDesk(var obj: JSONObject) {

    var SerialNumber: String = ""
    var PercentStanding: Double = 0.0

    init {
        SerialNumber = obj["SerialNumber"] as String
        PercentStanding = obj["PercentStanding"] as Double
    }
}