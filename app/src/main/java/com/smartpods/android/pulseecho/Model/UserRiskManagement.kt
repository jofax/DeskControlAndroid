package com.smartpods.android.pulseecho.Model

import org.json.JSONObject

interface RiskAssessmentManagement {
    val Level: Int
    val Progress: Double
    var GenericResponse: GenericResponseFromJSON
}

class UserRiskManagement(var response: JSONObject): RiskAssessmentManagement {
    override var Level: Int = -1
    override var Progress: Double = 0.0
    override var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(response)

    init {
        if (GenericResponse.Success) {
            Level = response["Level"] as Int
            Progress = response["Progress"] as Double
        }

    }
}