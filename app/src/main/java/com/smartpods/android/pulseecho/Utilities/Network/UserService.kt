package com.smartpods.android.pulseecho.Utilities.Network

import com.dariopellegrini.spike.TargetType
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.network.SpikeMethod
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Model.*

data class GetUser(val params: HashMap<String, Any>) : UserService()
data class UpdateUser(val params: HashMap<String, Any>) : UserService()
data class GetDepartments(val params: HashMap<String, Any>) : UserService()
data class GetUserRiskAssessment(val params: HashMap<String, Any>): UserService()

sealed class UserService: TargetType {
    override val baseURL: String
        get() = Constants.PULSE_URL

    override val path: String
        get() {
            return when(this) {
                is GetUser -> PulseAPI.settings + PulseAPI.clientSelect
                is UpdateUser -> PulseAPI.settings + PulseAPI.clientUpdate
                is GetDepartments -> PulseAPI.configuration + PulseAPI.listDepartments
                is GetUserRiskAssessment -> PulseAPI.settings + PulseAPI.clientUserRiskAssessment
            }
        }

    override val headers: Map<String, String>?
        get() = mapOf("Content-Type" to "application/json; charset=utf-8" )

    override val method: SpikeMethod
        get() {
            return when(this) {
                is GetUser -> SpikeMethod.POST
                is UpdateUser -> SpikeMethod.POST
                is GetDepartments -> SpikeMethod.POST
                is GetUserRiskAssessment -> SpikeMethod.POST
            }
        }

    override val parameters: Map<String, Any>?
        get() {
            return when(this) {
                is GetUser -> params
                is UpdateUser -> params
                is GetDepartments -> params
                is GetUserRiskAssessment -> params
            }
        }

    override val successClosure: ((String, Map<String, String>?) -> Any?)?
        get() = {
                result, headers ->
            when(this) {

                is GetUser -> {
                    val response = Gson().fromJson<UserObject>(result)
                    response
                }

                is UpdateUser -> {
                    val response = Gson().fromJson<UserObject>(result)
                    response
                }

                is GetDepartments -> {
                    val response = Gson().fromJson<Departments>(result)
                    response
                }

                is GetUserRiskAssessment -> {
                    val response = Gson().fromJson<UserRiskManagement>(result)
                    response
                }
            }
        }
    override val errorClosure: ((String, Map<String, String>?) -> Any?)?
        get() = { errorResult, _ ->
            when(this) {
                is GetUser -> {
                    val response = Gson().fromJson<UserObject>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    response
                }

                is UpdateUser -> {
                    val response = Gson().fromJson<UserObject>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    response
                }

                is GetDepartments -> {
                    val response = Gson().fromJson<Departments>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    response
                }

                is GetUserRiskAssessment -> {
                    val response = Gson().fromJson<UserRiskManagement>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    response
                }

            }
        }
}