package com.smartpods.android.pulseecho.Utilities.Network

import com.dariopellegrini.spike.TargetType
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.network.SpikeMethod
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Model.*

data class GetProfileSettings(val params: HashMap<String, Any>) : UserProfileService()
data class UpdateProfileSettings(val params: HashMap<String, Any>) : UserProfileService()

sealed class UserProfileService: TargetType {
    override val baseURL: String
        get() = Constants.PULSE_URL

    override val path: String
        get() {
            return when(this) {
                is GetProfileSettings -> PulseAPI.settings + PulseAPI.profileSettingSelect
                is UpdateProfileSettings -> PulseAPI.settings + PulseAPI.profileSettingUpdate
            }
        }

    override val headers: Map<String, String>?
        get() = mapOf("Content-Type" to "application/json; charset=utf-8" )

    override val method: SpikeMethod
        get() {
            return when(this) {
                is GetProfileSettings -> SpikeMethod.POST
                is UpdateProfileSettings -> SpikeMethod.POST
            }
        }

    override val parameters: Map<String, Any>?
        get() {
            return when(this) {
                is GetProfileSettings -> params
                is UpdateProfileSettings -> params
            }
        }

    override val successClosure: ((String, Map<String, String>?) -> Any?)?
        get() = {
                result, headers ->
            when(this) {

                is GetProfileSettings -> {
                    val response = Gson().fromJson<ProfileObject>(result)
                    response
                }

                is UpdateProfileSettings -> {
                    val response = Gson().fromJson<ProfileObject>(result)
                    response
                }
            }
        }
    override val errorClosure: ((String, Map<String, String>?) -> Any?)?
        get() = { errorResult, _ ->
            when(this) {
                is GetProfileSettings -> {
                    val response = Gson().fromJson<ProfileObject>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    response
                }

                is UpdateProfileSettings -> {
                    val response = Gson().fromJson<ProfileObject>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    response
                }

            }
        }
}