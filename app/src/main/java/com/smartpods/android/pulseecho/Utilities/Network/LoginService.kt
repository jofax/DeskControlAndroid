package com.smartpods.android.pulseecho.Utilities.Network

import com.smartpods.android.pulseecho.Utilities.UserPreference
import com.dariopellegrini.spike.TargetType
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.network.SpikeMethod
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.Model.PulseToken

data class LoginRequest(val username: String, val userPass: String): LoginService()
data class RegisterRequest(val username: String, val userPass: String): LoginService()
data class ActivateAccountRequest(val username: String, val activateCode: String): LoginService()
data class ResendActivateAccountRequest(val username: String): LoginService()
data class ForgotPasswordRequest(val username: String): LoginService()
data class ResetPasswordRequest(val username: String, val userPass: String, val activateCode: String): LoginService()
data class ResetPasswordUserLoggedRequest(val username: String, val userPass: String): LoginService()

sealed class LoginService: TargetType {

    override val baseURL: String
        get() = Constants.PULSE_URL

    override val path: String
        get() {
            return when(this) {
                is LoginRequest -> PulseAPI.account + PulseAPI.login
                is RegisterRequest -> PulseAPI.account + PulseAPI.registration
                is ActivateAccountRequest -> PulseAPI.account + PulseAPI.activate
                is ResendActivateAccountRequest -> PulseAPI.account + PulseAPI.resendActivation
                is ForgotPasswordRequest -> PulseAPI.account + PulseAPI.forgotPassword
                is ResetPasswordRequest -> PulseAPI.account + PulseAPI.forgotPasswordComplete
                is ResetPasswordUserLoggedRequest -> PulseAPI.account + PulseAPI.updatePassword
            }
        }

    override val headers: Map<String, String>?
        get() = mapOf("Content-Type" to "application/json; charset=utf-8" )

    override val method: SpikeMethod
        get() {
            return when(this) {
                is LoginRequest -> SpikeMethod.POST
                is RegisterRequest -> SpikeMethod.POST
                is ActivateAccountRequest -> SpikeMethod.POST
                is ResendActivateAccountRequest -> SpikeMethod.POST
                is ForgotPasswordRequest -> SpikeMethod.POST
                is ResetPasswordRequest -> SpikeMethod.POST
                is ResetPasswordUserLoggedRequest -> SpikeMethod.POST
            }
        }

    override val parameters: Map<String, Any>?
        get() {
            return when(this) {
                is LoginRequest -> mapOf("Email" to username, "Password" to userPass)
                is RegisterRequest-> mapOf("Email" to username, "Password" to userPass)
                is ActivateAccountRequest -> mapOf("Email" to username, "Code" to activateCode)
                is ResendActivateAccountRequest -> mapOf("Email" to username)
                is ForgotPasswordRequest -> mapOf("Email" to username)
                is ResetPasswordRequest -> mapOf("Email" to username, "Password" to userPass, "ResetCode" to activateCode)
                is ResetPasswordUserLoggedRequest -> mapOf()
            }
        }

    override val successClosure: ((String, Map<String, String>?) -> Any?)?
        get() = {
                result, headers ->
            when(this) {
                is RegisterRequest -> {
                    val response = Gson().fromJson<GenericResponse>(result)
                    response
                }
                is LoginRequest -> {
                    var login = Gson().fromJson<LoginModel>(result)
                    val response = Gson().fromJson<GenericResponse>(result)
                    val pulseToken = Gson().fromJson<PulseToken>(result)

                    response
                }
                is ActivateAccountRequest -> {
                    val mResponse = Gson().fromJson<GenericResponse>(result)
                    mResponse
                }
                is ResendActivateAccountRequest -> {
                    val mResponse = Gson().fromJson<GenericResponse>(result)
                    mResponse
                }
                is ForgotPasswordRequest -> {
                    val response = Gson().fromJson<GenericResponse>(result)
                    response
                }
                is ResetPasswordRequest -> {
                    val response = Gson().fromJson<GenericResponse>(result)
                    response
                }
                is ResetPasswordUserLoggedRequest -> {
                    val response = Gson().fromJson<GenericResponse>(result)
                    response
                }
            }
        }
    override val errorClosure: ((String, Map<String, String>?) -> Any?)?
        get() = { errorResult, _ ->
            when(this) {
                is RegisterRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("RegisterRequest errorClosure: $response")
                }
                is LoginRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("LoginRequest errorClosure: $response")
                }
                is ActivateAccountRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("RegisterRequest errorClosure: $response")
                }
                is ResendActivateAccountRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("RegisterRequest errorClosure: $response")
                }
                is ForgotPasswordRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("RegisterRequest errorClosure: $response")}

                is ResetPasswordRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("RegisterRequest errorClosure: $response")
                }
                is ResetPasswordUserLoggedRequest -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("RegisterRequest errorClosure: $response")
                }
            }

        }
}