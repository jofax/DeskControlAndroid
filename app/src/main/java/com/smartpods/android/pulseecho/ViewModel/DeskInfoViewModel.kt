package com.smartpods.android.pulseecho.ViewModel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.dariopellegrini.spike.SpikeProvider
import com.dariopellegrini.spike.mapping.fromJson
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Interfaces.PulseServerData
import com.smartpods.android.pulseecho.Interfaces.ServerDataObject
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.Utilities.*
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.Network.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Handler
import kotlin.collections.HashMap
import kotlin.experimental.xor
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.success

class DeskInfoViewModel(): BaseViewModel()  {
    private var requestDeskResponse: MutableLiveData<DeskInformationData> = MutableLiveData<DeskInformationData>()
    private var requestBookingInfo: MutableLiveData<DeskBooking> = MutableLiveData<DeskBooking>()
    private val provider = SpikeProvider<DeskService>()

    fun deskConnectRequest(email: String, params: HashMap<String, Any>): MutableLiveData<DeskInformationData> {
        val parameters = Utilities.addTokenToParameter(email, params)
        runBlocking {
            provider.request(DeviceConnect(parameters), { data ->
                println("Success deskConnectRequest response: " + data.results.toString())
                val mrawJsonObj = JSONObject(data.results)
                val deviceObj = DeskInformationData(mrawJsonObj)
                requestDeskResponse.value = deviceObj

            }, { error ->
                println("Failed deskConnectRequest response: " + error.results.toString())
                var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
                mGenericResponse.put("DeskBookingInfo",null)
                println("mGenericResponse : ${mGenericResponse}")
                val deviceObj = DeskInformationData(mGenericResponse)
                requestDeskResponse.value = deviceObj
            })
        }
        return requestDeskResponse
    }

    suspend fun getBookingInfo(params: HashMap<String, Any>): MutableLiveData<DeskBooking>{
        runBlocking {
            provider.request(GetBookingInfo(params), { data ->
                println("Success desk booking response: " + data.results.toString())
                val mrawJsonObj = JSONObject(data.results)
                val deviceObj = DeskBooking(mrawJsonObj)
                requestBookingInfo.value = deviceObj
            }, { error ->
                println("Failed desk booking response: " + error.results.toString())
                var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
                mGenericResponse.put("DeskBookingInfo",null)
                println("mGenericResponse : ${mGenericResponse}")
                val deviceObj = DeskBooking(mGenericResponse)
                requestBookingInfo.value = deviceObj
            })
        }

        return requestBookingInfo
    }

    suspend fun checkDeskBookingInformation() {
        val email = Utilities.getLoggedEmail()
        val serial = Utilities.getSerialNumber().toString()
        val mCredentials = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEDATAPUSH, hashMapOf("Serial" to serial)) ?: return
        (serial.isEmpty()).guard { return }
        val credentials:PulseDataPush  = mCredentials as PulseDataPush
        val aes = credentials.AESKey.hexStringToByteArray()
        val dataSerial = serial.hexStringToByteArray()
        val randomIV = Utilities.generateRandomBytes()
        val encryptedPacket = randomIV?.toUByteArray()?.let {
            SPPulseEncryption(aes,
                it.toByteArray(),dataSerial.toUByteArray()).encryptPacketWithCBC()
        }

        val base64EncodedIV = Base64.getEncoder().encodeToString(randomIV)
        val mParameters: HashMap<String, Any> = hashMapOf("SerialNumber" to serial,
            "EncryptedData" to (encryptedPacket?.toUByteArray() ?: ubyteArrayOf()), "Iv" to base64EncodedIV)


        getBookingInfo(mParameters)

        runBlocking {
            if (requestBookingInfo != null) {
                val bookId = requestBookingInfo.value?.BookingId
                if (bookId != 0) {
                    val bookedUser = requestBookingInfo.value?.Email.toString()
                    val bookingDate = requestBookingInfo.value?.BookingDate
                    val bookPeriods = if (requestBookingInfo.value?.Periods != null) requestBookingInfo.value?.Periods!! else arrayOf()
                    val timeOffset = if (requestBookingInfo.value?.TzOffset != null)  requestBookingInfo.value?.TzOffset!! else 0

                    if (bookPeriods.count() > 0 ) else return@runBlocking

                    val mBooking = Utilities.getBookingTime(
                        bookingDate.toString(),
                        bookPeriods,
                        timeOffset)

                    val startTime = mBooking["BookFrom"] as LocalDate
                    val endTime = mBooking["BookTo"] as LocalDate

                    val mStartDate = LocalDate.parse(startTime.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(
                        DateTimeFormatter.ofPattern("h:mm a"))

                    val mEndDate = LocalDate.parse(endTime.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(
                        DateTimeFormatter.ofPattern("h:mm a"))

                    val mBookingDate = LocalDate.parse(startTime.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(
                        DateTimeFormatter.ofPattern("MMM. dd, yyyy"))

                    val username = bookedUser.substringBefore("@")

                    val timeDisplay = "The desk has an incoming booking by $username on $mBookingDate from $mStartDate to $mEndDate"
                    toastMessage.value = SPEventHandler(timeDisplay)
                }
            }
        }
    }

    fun testPingPacket() {

        //val rawArray: List<UByte> = listOf(8u, 16u, 112u, 105u, 110u, 103u, 116u, 227u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u)
        val rawArray: List<UByte> = listOf(20u, 5u, 5u, 1u, 44u, 0u, 0u, 0u, 0u, 0u, 228u, 0u, 0u, 0u, 72u, 0u, 0u, 1u)
        val packetArray = rawArray.dropLast(2)

        println("packetArray: $packetArray")

        val aes = "ce4b083e5dd0e2ab0b985f6c28f95071".hexStringToByteArray()
        val iv = "86a8537b6bdb669f204da8ea93da12f2".hexStringToByteArray()

        var initialPacket: MutableList<UByte>  = mutableListOf()
        initialPacket.addAll(packetArray.toMutableList())

        val crc16 = Utilities.calculateDesktopCRC16(packetArray.toUByteArray())
        val byteConverted = Utilities.asByteArray(crc16.toShort()) //shortToByteArray(crc16.toShort())

        println("crc16: $crc16 | byteConverted: {$byteConverted}")

        byteConverted?.forEach{
            println("byteConverted: ${it.toUByte()}")
        }

        val extraPacket: List<UByte> = listOf(255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u ,255u)
        initialPacket.addAll(byteConverted)
        initialPacket.addAll(extraPacket)

        var byteArrayPackets: UByteArray = initialPacket.toUByteArray()
        val encryptedPacket = SPPulseEncryption(aes,iv,byteArrayPackets).encryptCBC()

        println("initialPacket structure: $initialPacket")

        var finalPacketBytes: MutableList<UByte> = mutableListOf(0u, 0u, 6u, 30u)
        finalPacketBytes.add(124u)
        finalPacketBytes.addAll(encryptedPacket.map { it.toUByte() })
        println("finalPacketBytes : $finalPacketBytes")


//        val (request, response, result) = "https://smartapi.smartpods.ca/api/Results/Queue".httpPost()
//            .body(finalPacketBytes.toUByteArray().toByteArray())
//            .header(mapOf("Content-Type" to "application/octet-stream"))
//            .response()
//
//        println("packetArray: $packetArray")

//        val httpAsync = "https://smartapi.smartpods.ca/api/Results/Queue"
//            .httpPost()
//            .body(finalPacketBytes.toUByteArray().toByteArray())
//            .header(mapOf("Content-Type" to "application/octet-stream"))
//            .response { request, response, result ->
//                when (result) {
//                    is Result.Failure -> {
//                        val ex = result.getException()
//                        println(ex)
//                    }
//                    is Result.Success -> {
//                        val data = result.get()
//                        val responsePacket = data.drop(5)
//                        val decryptedPacket = SPPulseEncryption(aes,iv,responsePacket.map { it.toUByte() }.toUByteArray()).decryptCBC()
//                        val strValue = String(decryptedPacket, charset("ASCII"))
//                    }
//                }
//            }
//
//        httpAsync.join()

//        val (request, response, result) = "https://smartapi.smartpods.ca/api/Results/Queue".httpPost()
//            .body(finalPacketBytes.toUByteArray().toByteArray())
//            .header(mapOf("Content-Type" to "application/octet-stream"))
//            .response()
//
//        val data = result.get()
//
//        if (data != null) else return
//
//        val responsePacket = data.drop(5)
//        val decryptedPacket = SPPulseEncryption(aes,iv,responsePacket.map { it.toUByte() }.toUByteArray()).decryptCBC()
//        val strValue = String(decryptedPacket, charset("ASCII"))
//        println("Success PushDeskData mPacket: ${data}")
//        println("Success PushDeskData responsePacket: ${responsePacket}")
//        println("Success PushDeskData strValue: $strValue")

    }


    fun pushDeskData(data: ServerDataObject) {
        val email = Utilities.getLoggedEmail()
        val packetArray = data.serverData.dropLast(2)
        val mCredentials = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEDATAPUSH, hashMapOf("Serial" to Utilities.getSerialNumber().toString())) ?: return

        println("pushDeskData proceed : $mCredentials")
        val credentials:PulseDataPush  = mCredentials as PulseDataPush
        println("packetArray: $packetArray")

        val aes = credentials.AESKey.hexStringToByteArray()
        val iv = credentials.AESIV.hexStringToByteArray()

        var initialPacket: MutableList<UByte>  = mutableListOf()
        initialPacket.addAll(packetArray.toMutableList())

        val crc16 = Utilities.calculateDesktopCRC16(packetArray.toUByteArray())
        val byteConverted = Utilities.asByteArray(crc16.toShort())

        println("crc16: $crc16 | byteConverted: {$byteConverted}")

        val extraPacket: List<UByte> = listOf(255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u ,255u)
        initialPacket.addAll(byteConverted)
        initialPacket.addAll(extraPacket)

        var byteArrayPackets: UByteArray = initialPacket.toUByteArray()
        val encryptedPacket = SPPulseEncryption(aes,iv,byteArrayPackets).encryptCBC()


        var finalPacketBytes: MutableList<UByte> = mutableListOf(0u, 0u, 6u, 30u)
        finalPacketBytes.add(124u)
        finalPacketBytes.addAll(encryptedPacket.map { it.toUByte() })
        //finalPacketBytes += encryptedPacket.map { it }

        //println("finalPacketBytes : $finalPacketBytes")

        //println("pushDeskData encrypted packet: $encryptedPacket")

//        provider.request(PushDeskData(finalPacketBytes.toUByteArray().toByteArray()), { data ->
//            println("Success PushDeskData response: " + data.results.toString().toByteArray(charset("ASCII")))
//
//            var mPacket = data.results.toString().toByteArray()
//            val responsePacket = mPacket.drop(5)
//            val decryptedPacket = SPPulseEncryption(aes,iv,responsePacket.map { it.toUByte() }.toUByteArray()).decryptPacketWithCBC()
//            val strValue = String(decryptedPacket.toByteArray(), charset("ASCII"))
//            println("Success PushDeskData mPacket: ${mPacket}")
//            println("Success PushDeskData responsePacket: ${responsePacket}")
//
//        }, { error ->
//            println("Failed PushDeskData response: " + error.results.toString().toByteArray(charset("ASCII")))
//        })

        val (request, response, result) = "https://smartapi.smartpods.ca/api/Results/Queue".httpPost()
            .body(finalPacketBytes.toUByteArray().toByteArray())
            .header(mapOf("Content-Type" to "application/octet-stream"))
            .response()

        val data = result.get()
        val responsePacket = data.drop(5)

        val extraPacket2: List<UByte> = listOf(255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u, 255u ,255u, 255u, 255u, 255u, 255u)

        var initialResponsePacket: MutableList<UByte>  = mutableListOf()
        initialResponsePacket.addAll(responsePacket.toByteArray().toUByteArray())
        initialResponsePacket.addAll(extraPacket2)


        val decryptedPacket = SPPulseEncryption(aes,iv,initialResponsePacket.toUByteArray()).decryptCBC()
        val strValue = String(decryptedPacket, charset("ASCII"))
        println("Success PushDeskData mPacket: ${data}")
        println("Success PushDeskData responsePacket: ${responsePacket}")
        println("Success PushDeskData strValue: $strValue")
    }
}
