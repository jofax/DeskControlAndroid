package com.smartpods.android.pulseecho.Utilities.Network

import com.dariopellegrini.spike.TargetType
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.network.SpikeMethod
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Model.*

data class SummaryReport(val params: HashMap<String, Any>): UserReportService()
data class DeskModeReport(val params: HashMap<String, Any>): UserReportService()
data class ActivityReport(val params: HashMap<String, Any>): UserReportService()

sealed class UserReportService: TargetType {
    override val baseURL: String
        get() = Constants.PULSE_URL

    override val path: String
        get() {
            return when(this) {
                is SummaryReport -> PulseAPI.userReport + PulseAPI.summaryReport
                is DeskModeReport -> PulseAPI.userReport + PulseAPI.deskModeReport
                is ActivityReport -> PulseAPI.userReport + PulseAPI.activityReport
            }
        }

    override val headers: Map<String, String>?
        get() = mapOf("Content-Type" to "application/json; charset=utf-8" )

    override val method: SpikeMethod
        get() {
            return when(this) {
                is SummaryReport -> SpikeMethod.POST
                is DeskModeReport -> SpikeMethod.POST
                is ActivityReport -> SpikeMethod.POST
            }
        }

    override val parameters: Map<String, Any>?
        get() {
            return when(this) {
                is SummaryReport -> params
                is DeskModeReport -> params
                is ActivityReport -> params
            }
        }

    override val successClosure: ((String, Map<String, String>?) -> Any?)?
        get() = {
                result, headers ->
            when(this) {

                is SummaryReport -> {
                    val response = Gson().fromJson<UserReport>(result)
                    response
                }

                is DeskModeReport -> {
                    val response = Gson().fromJson<UserReport>(result)
                    response
                }

                is ActivityReport -> {
                    val response = Gson().fromJson<UserReport>(result)
                    response
                }

            }
        }
    override val errorClosure: ((String, Map<String, String>?) -> Any?)?
        get() = { errorResult, _ ->
            when(this) {
                is SummaryReport -> {
                    val response = Gson().fromJson<UserReport>(errorResult)
                    println("SummaryReport errorClosure: $response")
                    response
                }

                is DeskModeReport -> {
                    val response = Gson().fromJson<UserReport>(errorResult)
                    println("DeskModeReport errorClosure: $response")
                    response
                }

                is ActivityReport -> {
                    val response = Gson().fromJson<UserReport>(errorResult)
                    println("ActivityReport errorClosure: $response")
                    response
                }
            }
        }
}