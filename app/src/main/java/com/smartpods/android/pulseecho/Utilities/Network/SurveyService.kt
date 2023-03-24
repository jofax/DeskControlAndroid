package com.smartpods.android.pulseecho.Utilities.Network

import com.dariopellegrini.spike.TargetType
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.network.SpikeMethod
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Model.*


data class GetSurvey    (val params: HashMap<String, Any>) : SurveyService()
data class SendSurveyAnswers(val params: HashMap<String, Any>) : SurveyService()

sealed class SurveyService: TargetType {
    override val baseURL: String
        get() = Constants.PULSE_URL

    override val path: String
        get() {
            return when(this) {
                is GetSurvey -> PulseAPI.survey + PulseAPI.nextSurveyRun
                is SendSurveyAnswers -> PulseAPI.survey + PulseAPI.surveyAnswers
            }
        }

    override val headers: Map<String, String>?
        get() = mapOf("Content-Type" to "application/json; charset=utf-8" )

    override val method: SpikeMethod
        get() {
            return when(this) {
                is GetSurvey -> SpikeMethod.POST
                is SendSurveyAnswers -> SpikeMethod.POST
            }
        }

    override val parameters: Map<String, Any>?
        get() {
            return when(this) {
                is GetSurvey -> params
                is SendSurveyAnswers -> params
            }
        }

    override val successClosure: ((String, Map<String, String>?) -> Any?)?
        get() = {
                result, headers ->
            when(this) {

                is GetSurvey -> {
                    val response = Gson().fromJson<UserSurvey>(result)
                    response
                }

                is SendSurveyAnswers -> {
                    val response = Gson().fromJson<GenericResponse>(result)
                    response
                }
            }
        }
    override val errorClosure: ((String, Map<String, String>?) -> Any?)?
        get() = { errorResult, _ ->
            when(this) {
                is GetSurvey -> {
                    val response = Gson().fromJson<UserSurvey>(errorResult)
                    println("SurveyService errorClosure: $response")
                    response
                }

                is SendSurveyAnswers -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("SurveyService errorClosure: $response")
                    response
                }
            }
        }
}
