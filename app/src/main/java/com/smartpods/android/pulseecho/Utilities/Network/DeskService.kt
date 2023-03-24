package com.smartpods.android.pulseecho.Utilities.Network

import com.dariopellegrini.spike.TargetType
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.multipart.SpikeMultipartEntity
import com.dariopellegrini.spike.network.SpikeMethod
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Model.*

data class DeviceConnect(val params: HashMap<String, Any>) : DeskService()
data class GetBookingInfo(val params: HashMap<String, Any>) : DeskService()
data class PushDeskData(val packetData: ByteArray) : DeskService()

sealed class DeskService: TargetType {
    override val baseURL: String
        get() = Constants.PULSE_URL

    override val path: String
        get() {
            return when(this) {
                is DeviceConnect -> PulseAPI.device + PulseAPI.connect
                is GetBookingInfo -> PulseAPI.configuration + PulseAPI.getBooking
                is PushDeskData -> PulseAPI.dataResults + PulseAPI.queue
            }
        }

    override val headers: Map<String, String>?
        get() {
            return when(this ) {
                is DeviceConnect -> mapOf("Content-Type" to "application/json; charset=utf-8" )
                is GetBookingInfo -> mapOf("Content-Type" to "application/json; charset=utf-8" )
                is PushDeskData -> mapOf("Content-Type" to "application/octet-stream" )
            }
        }

    override val multipartEntities: List<SpikeMultipartEntity>?
        get() {
            return when(this) {
                is DeviceConnect -> null
                is GetBookingInfo -> null
                is PushDeskData -> listOf(SpikeMultipartEntity("application/octet-stream", packetData, "data", "data"))
            }
        }

    override val method: SpikeMethod
        get() {
            return when(this) {
                is DeviceConnect -> SpikeMethod.POST
                is GetBookingInfo -> SpikeMethod.POST
                is PushDeskData -> SpikeMethod.POST
            }
        }

    override val parameters: Map<String, Any>?
        get() {
            return when(this) {
                is DeviceConnect -> params
                is GetBookingInfo -> params
                is PushDeskData -> null
            }
        }

    override val successClosure: ((String, Map<String, String>?) -> Any?)?
        get() = {
                result, headers ->
            when(this) {

                is DeviceConnect -> {
                    val response = Gson().fromJson<GenericResponse>(result)
                    response
                }

                is GetBookingInfo -> {
                    val response = Gson().fromJson<DeskBooking>(result)
                    response
                }

                is PushDeskData -> {

                    //data.results.toString().toByteArray(charset("ASCII")))
                    val response = result.encodeToByteArray()
                    println("PushDeskData successClosure result: $response");
                    result
                }
            }
        }
    override val errorClosure: ((String, Map<String, String>?) -> Any?)?
        get() = { errorResult, _ ->
            when(this) {
                is DeviceConnect -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    //response
                    errorResult
                }

                is GetBookingInfo -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    //response
                    errorResult
                }

                is PushDeskData -> {
                    val response = Gson().fromJson<GenericResponse>(errorResult)
                    println("UserProfileService errorClosure: $response")
                    //response
                    errorResult
                }
            }
        }
}