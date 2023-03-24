package com.smartpods.android.pulseecho.Utilities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.SystemClock
import android.text.TextUtils
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import io.realm.BuildConfig
import io.realm.RealmConfiguration
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.round
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap
import kotlin.experimental.and

object Utilities {

    init {
        print("Utilities initialized")
    }

    //Utilities properties

    var isCalibrationOn: Boolean = false
    var DeskBytes: List<UByte> = listOf()
    var desktopApphasPriority: Boolean = false
    var isDeskCurrentlyBooked: Boolean = false
    var isDeskHasIncomingBooking: Boolean = false
    var isDeskEnabled: Boolean = false

    var bookingSchedulerCount = 0
    var bookingSchedulerLimit = 1

    var hasProfileSync = false

    lateinit var SP_AES_IV: IvParameterSpec
    lateinit var SP_AES_KEY: SecretKeySpec

    fun disconnectDevice(device: BluetoothDevice) {
        val email = getLoggedEmail()
        SPBleManager.forceDisconnect(device)
        SPBleManager.forceDisconnect(getSmartpodsDevice())

        SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf("InteractivePopUpShowed" to false,
            "SafetyPopUpShowed" to false,
            "AutomaticControls" to false,
            "LegacyControls" to false,
            "IsNotifiedForHeightAdjustments" to false,
            "DeviceDisconnectedByUser" to true,
            "AppSaveProfile" to ""), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)

    }

    fun getUserEmail(): String {
        println("USER EMAIL VALUE: " + UserPreference.prefs.read("email", ""))
        return UserPreference.prefs.read(Constants.USER_EMAIL, "") ?: ""
    }

    fun Boolean.toInt() = if (this) 1 else 0

    fun getNavigationBarSize(context: Context): Point? {
        val appUsableSize: Point = getAppUsableScreenSize(context)
        val realScreenSize: Point = getRealScreenSize(context)

        // navigation bar on the side
        if (appUsableSize.x < realScreenSize.x) {
            return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
        }

        // navigation bar at the bottom
        return if (appUsableSize.y < realScreenSize.y) {
            Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
        } else Point()

        // navigation bar is not present
    }

    fun getAppUsableScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size)
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                size.y = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
            } catch (e: IllegalAccessException) {

            } catch (e: InvocationTargetException) {

            } catch (e: NoSuchMethodException) {

            }
        }
        return size
    }

    fun doubleToPercentage(data: Double): String {
//        val percentFormat: NumberFormat = NumberFormat.getPercentInstance()
//        percentFormat.minimumFractionDigits = 1
//        val number2digits:Double = Math.round(data * 100.0) / 100.0
//        val mPercent = percentFormat.format(number2digits)
//        val filterUserPrice: String = "%.2f".format(mPercent)
//        return filterUserPrice

        var percentage = (round(data) / 100) * 100
        val percentVal = "${percentage} %"
        return percentVal

    }

    fun getDurationAngle(duration: Int): Double {
        return when (duration) {
            5 -> Math.toDegrees((5 * PI) / 3)
            10 -> Math.toDegrees((11 * PI) / 6)
            15 ->  Math.toDegrees(2 * PI)
            20 -> Math.toDegrees(PI / 6)
            25 -> Math.toDegrees(PI / 3)
            30 -> Math.toDegrees(PI / 2)
            35 -> Math.toDegrees((2 * PI) / 3)
            40 -> Math.toDegrees((5 * PI) / 6)
            45 -> Math.toDegrees(PI)
            50 -> Math.toDegrees((7 * PI) / 6)
            55 -> Math.toDegrees((4 * PI) / 3)
            else -> {
                Math.toDegrees(3 * PI) / 2
            }
        }
    }

    fun getSweepAngle(duration: Int) : Double {
        return when (duration) {
            5   ->  30.0
            10  ->  60.0
            15  ->  90.0
            20  ->  120.0
            25  ->  150.0
            30  ->  180.0
            35  ->  210.0
            40  ->  240.0
            45  ->  270.0
            50  ->  300.0
            55  ->  330.0
            else -> {
                360.0
            }
        }
    }

    fun pulseAppInForground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        return runningAppProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

    fun getCombineByte(hbyte: UByte, lbyte: UByte) : Int {
        return (hbyte.toInt() shl 8) or lbyte.toInt()
    }

    fun getTimeCodes(code: Int) : Int {
        return when (code) {
            0 -> 3
            1 -> 300
            2 -> 600
            3 -> 900
            4 -> 1200
            5 -> 1500
            6 -> 1620
            7 -> 1800
            8 -> 2100
            9 -> 2400
            10 -> 2700
            11 -> 3000
            12 -> 3300
            13 -> 3420
            14 -> -1
            15 -> 0
            else -> { 0 }
        }
    }

    fun convertBitField(value: Int, index: Int) : Boolean {
        val str = value.toString(2)
        val binaryString = pad(str)
        val binStr: String = binaryString.get(index).toString()
        if (binStr == "1") {
            return true
        } else {
            return false
        }
    }

    fun calculateCrc16(input: UByte, polynomial: UShort): UShort {
        val bigEndianInput = input.toUShort() shl 8
        return (0 until 8).fold(bigEndianInput) { result, _ ->
            val isMostSignificantBitOne =
                result and 0x8000.toUShort() != 0.toUShort()
            val shiftedResult = result shl 1
            when (isMostSignificantBitOne) {
                true -> shiftedResult xor polynomial
                false -> shiftedResult
            }
        }
    }

    fun calculatCrc16FromArray(inputs: UByteArray, initialValue: UShort = 0.toUShort()): UShort {
        val crc16Table = (0 until 256).map {
            calculateCrc16(it.toUByte(), 0xBAAD.toUShort())
        }

        return inputs.fold(initialValue) { remainder, byte ->
            val bigEndianInput = byte.toUShort() shl 8
            val index = (bigEndianInput xor remainder) shr 8
            crc16Table[index.toInt()] xor (remainder shl 8)
        }
    }

    fun calculateCRCWithoutInitialValue(inputs: UByteArray): UShort {
        val crc16Table = (0 until 256).map {
            calculateCrc16(it.toUByte(), 0xBAAD.toUShort())
        }
        return inputs.fold(0.toUShort()) { remainder, byte ->
            val bigEndianInput = byte.toUShort() shl 8
            val index = (bigEndianInput xor remainder) shr 8
            crc16Table[index.toInt()] xor (remainder shl 8)
        }
    }

    fun calculateDesktopCRC16(inputs: UByteArray): UShort  {
//        val crc16Table = (0 until 256).map {
//            calculateCrc16(it.toUByte(), Constants.CRC16_ENCRYPTED_POLY.toUShort())
//        }
//
//        return inputs.fold(Constants.BIT15_0_MASK.toUShort()) { remainder, byte ->
//            val bigEndianInput = byte.toUShort() shl 8
//            val index = (bigEndianInput xor remainder) shr 8
//            crc16Table[index.toInt()] xor (remainder shl 8)
//        }

        val crc16Table = (0 until 256).map {
            calculateCrc16(it.toUByte(), 0x1021.toUShort())
        }

        return inputs.fold(0xFFFF.toUShort()) { remainder, byte ->
            val bigEndianInput = byte.toUShort() shl 8
            val index = (bigEndianInput xor remainder) shr 8
            crc16Table[index.toInt()] xor (remainder shl 8)
        }
    }

    fun bytesToUnsignedShort(byte1 : Byte, byte2 : Byte, bigEndian : Boolean) : Int {
        if (bigEndian)
            return (((byte1.toInt() and 255) shl 8) or (byte2.toInt() and 255))

        return (((byte2.toInt() and 255) shl 8) or (byte1.toInt() and 255))
    }

    fun calculateCrcWithBytes(dataBytes: ByteArray, bigEndian : Boolean) : Int {
        return dataBytes.fold(0) { remainder, byte ->
            if (bigEndian) {
                (((remainder and 255) shl 8) or (byte.toInt() and 255))
            } else {
                (((byte.toInt() and 255) shl 8) or (remainder and 255))
            }
        }

    }

    fun validCRC16(packet: ByteArray): Boolean {

        if (packet.count() > 2) else return false
        val uBytePackets = packet.map { it.toUByte() }

        //println("uBytePackets : $uBytePackets")

        val rawPacket = uBytePackets.slice(0..packet.count() - 3)
        var crcArr = uBytePackets.slice((packet.count() - 2)..(packet.count() - 1))

        //println("rawPacket : $rawPacket")
        //println("crcArr : $crcArr")

        val dataPacket = calculatCrc16FromArray(rawPacket.toUByteArray(), 0xFFFF.toUShort())
        val crcPacket = calculateCrcWithBytes(crcArr.map { it.toByte() }.toByteArray(), true)

        //println("dataPacket : ${dataPacket.toInt()}")
        //println("crcPacket : $crcPacket")

        //println("validCRC16 : ${(dataPacket.toInt() == crcPacket)}")

        return (dataPacket.toInt() == crcPacket)

    }

    fun bytesToShort(bytes: ByteArray?): Short {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).short
    }

//    fun toBytes(s: Short): ByteArray {
//        return byteArrayOf((s.toInt() and 0x00FF).toByte(), ((s.toInt() and 0xFF00) shr (8)).toByte())
//    }

    fun asByteArray(value: Short): List<UByte> {
        val buffer: ByteBuffer = ByteBuffer.allocate(2)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(value)
        buffer.flip()

        val _byteArr = buffer.array().map {
            it.toUByte()
        }
        return _byteArr
    }

    fun asByteArray2(value: Short): List<UByte> {
        val buffer: ByteBuffer = ByteBuffer.allocate(4)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(value)
        buffer.flip()

        val _byteArr = buffer.array().map {
            it.toUByte()
        }
        return _byteArr
    }

    fun pad(string: String, toSize: Int = 8) : String {
        var padded = string
        val strSize = (toSize - string.length)
        for (i in 0 until strSize) {
            padded = "0" + padded
        }

        return padded
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun generateRandomBytes(): ByteArray? {
        val nonce = ByteArray(16)
        SecureRandom().nextBytes(nonce)
        return nonce
    }

    //COLORS

    fun getCustomRawColor(colorCode: Int): Int {
        return ContextCompat.getColor(
            PulseApp.appContext,
            colorCode
        )
    }

    // Realm Function
    fun getRealmForUser(username: String, migrate: Boolean): RealmConfiguration {
        //val secureKey = UserPreference.encryptedPrefs.read(Constants.APP_KEY_ACCESS, "")
        var schemaVersion = UserPreference.prefs.read("REALM_SCHEMA", 1)

        if (schemaVersion != BuildConfig.VERSION_CODE) {
            schemaVersion += 1
        }

        //println("GET REALM KEY: ${secureKey}")

//        return if (migrate) {
//            RealmConfiguration.Builder()
//                .name("$username.realm")
//                .allowWritesOnUiThread(true)
//                .encryptionKey(Constants.APP_KEY.toByteArray())
//                .schemaVersion(schemaVersion.toLong())
//                .migration(DBRealmMigration())
//                .build()
//        } else {
//            RealmConfiguration.Builder()
//                .name("$username.realm")
//                .allowWritesOnUiThread(true)
//                .encryptionKey(Constants.APP_KEY.toByteArray())
//                .build()
//        }

        return if (migrate) {
            RealmConfiguration.Builder()
                .name("$username.realm")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .schemaVersion(schemaVersion.toLong())
                //.migration(DBRealmMigration())
                .build()
        } else {
            RealmConfiguration.Builder()
                .name("$username.realm")
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build()
        }

    }

    fun feetToCentimeter(feetval: String): HashMap<String, Any> {
        var heightInFeet = 0.0
        var heightInInches = 0.0
        var feet = ""
        var inches = ""
        if (!TextUtils.isEmpty(feetval)) {
            if (feetval.contains("'")) {
                feet = feetval.substring(0, feetval.indexOf("'"))
            }
            if (feetval.contains("\"")) {
                inches = feetval.substring(feetval.indexOf("'") + 1, feetval.indexOf("\""))
            }
        }
        try {
            if (feet.trim { it <= ' ' }.isNotEmpty()) {
                heightInFeet = feet.toDouble()
            }
            if (inches.trim { it <= ' ' }.isNotEmpty()) {
                heightInInches = inches.toDouble()
            }
        } catch (nfe: NumberFormatException) {
        }

        val result: HashMap<String, Any> = hashMapOf("data" to (((heightInFeet * 12.0) + heightInInches) * 2.54).toString(),
            "raw" to ((heightInFeet * 12.0) + heightInInches) * 2.54)

        //return (((heightInFeet * 12.0) + heightInInches) * 2.54).toString()
        return result
    }

    fun centimeterToFeet(centimeter: String?): HashMap<String, Any> {
        var feetPart = 0
        var inchesPart = 0
        if (!TextUtils.isEmpty(centimeter)) {
            val dCentimeter = java.lang.Double.valueOf(centimeter!!)
            feetPart = Math.floor((dCentimeter / 2.54) / 12).toInt()
            println(dCentimeter / 2.54 - feetPart * 12)
            inchesPart = Math.floor((dCentimeter / 2.54) - (feetPart * 12)).toInt()
        }
        val result: HashMap<String, Any> = hashMapOf("data" to String.format("%d' %d\"", feetPart, inchesPart),
            "feet" to feetPart, "inches" to inchesPart)
        //return String.format("%d' %d\"", feetPart, inchesPart)
        return result
    }

    fun kilogramsToPounds(data: Double): String {
        val mPounds = data * 2.20462262
        return "%.1f".format(mPounds)
    }

    fun poundsToKilograms(data: Double): String {
        val mKilogram = data / 2.20462262
        return "%.1f".format(mKilogram)
    }

    fun toJSONParameters(obj: Any): JSONObject {
        val mObj =  Gson().toJson(obj)
        return JSONObject(mObj)
    }

    fun defaultJSONParameters(): JSONObject {
        val defaultJSONObj= JSONObject()
        defaultJSONObj.put("Success",false)
        defaultJSONObj.put("ResultCode",0)
        defaultJSONObj.put("Message","")
        return defaultJSONObj
    }

    @SuppressLint("SimpleDateFormat")
    fun stringFormatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ").format(date)
    }

    fun convertDate(dateString: String): Date? {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateString)
    }

    fun hideSoftKeyBoard(context: Context, view: View) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }

    }

    fun addTokenToParameter(email:String, params: HashMap<String, Any>): HashMap<String, Any> {
        var parameters = params
        val info = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.SPCLIENTINFO)
        if (info != null) {
            val mInfo = info as SPClientInfo
            parameters["SessionKey"] = mInfo.SessionKey
            parameters["SessionDated"] = mInfo.SessionDated
        }

        return parameters
    }

    fun Double.format(fracDigits: Int): String {
        val df = DecimalFormat()
        df.setMaximumFractionDigits(fracDigits)
        return df.format(this)
    }

    // BLE Utility Functions
    fun getSmartpodsDevice(): BluetoothDevice {
        val bleAdpter = BluetoothAdapter.getDefaultAdapter()
        return bleAdpter.getRemoteDevice(Constants.BLE_UUID)
    }


    fun typeOfUserLogged(): CURRENT_LOGGED_USER? {
        val mlogged  = UserPreference.prefs.read(Constants.current_logged_user_type, CURRENT_LOGGED_USER.None.rawValue)
        val logged = mlogged?.let { CURRENT_LOGGED_USER.invoke(it) }
        return logged
    }

    fun getLoggedEmail(): String {
        return when(typeOfUserLogged()) {
            CURRENT_LOGGED_USER.Guest -> "guest"
            CURRENT_LOGGED_USER.Local -> "local"
            CURRENT_LOGGED_USER.Cloud -> getUserEmail()
            else -> ""
        }
    }

    fun getSerialNumber(): String? {
        return UserPreference.prefs.read("SerialNumber", "")
    }

    fun convertByteArrayToInt(bytes:ByteArray):Int {
//        if (bytes.size != 0) {
//            throw Exception("wrong len")
//        }
        //bytes.reverse()
        return ByteBuffer.wrap(bytes).int
    }

    fun convertByteArrayToString(bytes:ByteArray): String? {
        var strVal = ByteBuffer.wrap(bytes, 0, bytes.count()).toString()
        val mValue = String(bytes, charset("UTF-8"))
        //val encodedString = Base64.getEncoder().withoutPadding().encodeToString(strVal.toByteArray())
        //return encodedString
        return encodeToBase64(strVal)
    }

    fun encodeToBase64(s: String): String? {
        var data = ByteArray(s.count())
        try {
            data = s.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } finally {
            return Base64.getEncoder().encodeToString(data)
        }
    }

    fun byteArrayToInt(bytes: ByteArray): Int {
        var result = 0
        var shift = 0
        for (byte in bytes) {
            result = result or (byte.toInt() shl shift)
            shift += 8
        }
        return result
    }

    fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() shl 8 * i)
        }
        return result
    }

    fun bigEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i] shl 8 * i)
        }
        return result
    }

    fun bigEndianConversion32Bytes(list: List<UInt>): Int {
        var result = 0
        for (i in list.indices) {
            result = (result or (list[i].toInt() and 255))  //result or (list[i].toInt() shl 8 * i)
        }
        return result
    }

    fun savePushCredentials(aes: String, iv: String) {
        val loggedUser = typeOfUserLogged()
        (loggedUser != CURRENT_LOGGED_USER.Cloud).guard {  }
        val email = getLoggedEmail()
        val mSerial = getSerialNumber()

        (mSerial != "").guard {  }

        var params: HashMap<String, Any> = hashMapOf()

        if (aes.isNotEmpty()) {
            params = hashMapOf("Serial" to mSerial.toString(),
                "Email" to email,
                "AESKey" to aes)
        }

        if (iv.isNotEmpty()) {
            params = hashMapOf("Serial" to mSerial.toString(),
                "Email" to email,
                "AESIV" to iv)
        }

        SPRealmHelper.saveOrUpdateObjectWithData(params,email, REALM_OBJECT_TYPE.PULSEDATAPUSH)

        /*var mPushData = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEDATAPUSH, params)
        val currentTimestamp = System.currentTimeMillis()
        if (mPushData == null) {
            println("PULSEDATAPUSH null")

            if (aes.isNotEmpty()) {
                val pushParams = PulseDataPush(currentTimestamp.toString(), mSerial, email, aes, iv)
                SPRealmHelper.saveObject(pushParams, email, REALM_OBJECT_TYPE.PULSEDATAPUSH)
            }

            if (iv.isNotEmpty()) {
                val pushParams = PulseDataPush(currentTimestamp.toString(), mSerial, email, aes, iv)
                SPRealmHelper.saveObject(pushParams, email, REALM_OBJECT_TYPE.PULSEDATAPUSH)
            }

        } else {
            println("PULSEDATAPUSH it not null")
            val pushObj = mPushData as PulseDataPush

            if (aes.isNotEmpty()) {
                val newPushObj = PulseDataPush(pushObj.id, pushObj.Serial, email, aes, pushObj.AESIV)
                SPRealmHelper.saveObject(newPushObj, email, REALM_OBJECT_TYPE.PULSEDATAPUSH)
            }

            if (iv.isNotEmpty()) {
                val newPushObj = PulseDataPush(pushObj.id, pushObj.Serial, email, pushObj.AESKey, iv)
                SPRealmHelper.saveObject(newPushObj, email, REALM_OBJECT_TYPE.PULSEDATAPUSH)
            }
        }*/
    }

    fun getBookingTime(bookingDate: String, periods: Array<DeskBookingPeriods>, offset: Int) : HashMap<String, Any> {
        (periods.count() > 0).guard { return hashMapOf("BookFrom" to Date(),"BookTo" to Date()) }
         val durationPeriod: DeskBookingPeriods = periods.first()
         val _timeFrom = durationPeriod.TimeIdFrom
         val _timeTo = (durationPeriod.TimeIdTo) + 1

         var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
         val dateBooked = LocalDateTime.parse(bookingDate, formatter)  //LocalDateTime.now().format(formatter)

         var bookingStartDate = dateBooked
         var bookingEndDate = dateBooked

         bookingStartDate.plus(((30 * _timeFrom) * 60).toLong(), ChronoUnit.MINUTES)
         bookingEndDate.plus(((30 * _timeTo) * 60).toLong(), ChronoUnit.MINUTES)
         return hashMapOf("BookFrom" to bookingStartDate,
            "BookTo" to bookingEndDate)
    }

    fun getDefaultActivityProfile(email: String): HashMap<String, Any> {
        return hashMapOf("email" to email,
            "StandingTime1" to 5,
            "StandingTime2" to 0,
            "ProfileSettingType" to 0,
            "SittingPosition" to Constants.defaultSittingPosition,
            "StandingPosition" to Constants.defaultStandingPosition,
            "IsInteractive" to false)
    }

    fun saveOrUpdatePulseDevice(data: HashMap<String, Any>) {
        val email = getLoggedEmail()
        SPRealmHelper.saveOrUpdateObjectWithData(data,email,REALM_OBJECT_TYPE.PULSEDEVICES)
    }

    fun getDeviceAppState(): PulseAppStates? {
        val email = getLoggedEmail()
        return if (SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEAPPSTATE) != null)
            SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEAPPSTATE) as PulseAppStates else null
    }

    fun getUserNotifiedStatus(key: String): Boolean {
        var isNotifyUser = false
        val mAppState = getDeviceAppState()
        if (mAppState != null) {
            when(key) {
                "Height" -> {
                    isNotifyUser = mAppState.IsNotifiedForHeightAdjustments
                }
                "Safety" -> {
                    isNotifyUser = mAppState.SafetyPopUpShowed
                }
                "Device" -> {
                    isNotifyUser = mAppState.DeviceDisconnectedByUser
                }
                "Interactive" -> {
                    isNotifyUser = mAppState.InteractivePopUpShowed
                }
            }
        }

        return isNotifyUser
    }
}

data class StopWatch(val totalSeconds: Int) {
    fun years() : Int {
        return totalSeconds / 31536000
    }

    fun days(): Int {
        return (totalSeconds % 31536000) / 86400
    }

    fun hours(): Int {
        return (totalSeconds % 86400) / 3600
    }

    fun minutes(): Int {
        return (totalSeconds % 3600) / 60
    }

    fun seconds(): Int {
        return totalSeconds % 60
    }

}

class SPTimer {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    public fun startCoroutineTimer(delayMillis: Long = 0, repeatMillis: Long = 0, action: () -> Unit) = scope.launch(
        Dispatchers.IO) {
        delay(delayMillis)
        if (repeatMillis > 0) {
            while (true) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }

    public val timer: Job = startCoroutineTimer(delayMillis = 0, repeatMillis = 20000) {
        //Log.d(TAG, "Background - tick")
        //doSomethingBackground()
        scope.launch(Dispatchers.Main) {
            //Log.d(TAG, "Main thread - tick")
            //doSomethingMainThread()
        }
    }

    fun startTimer() {
        timer.start()
    }

    fun cancelTimer() {
        timer.cancel()
    }
}

class OnClickListenerThrottled(private val block: () -> Unit, private val wait: Long) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < wait) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        block()
    }
}

/**
 * A throttled click listener that only invokes [block] at most once per every [wait] milliseconds.
 */
fun View.setOnClickListenerThrottled(wait: Long = 1000L, block: () -> Unit) {
    setOnClickListener(OnClickListenerThrottled(block, wait))
}