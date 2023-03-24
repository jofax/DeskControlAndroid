package com.smartpods.android.pulseecho.Constants

import java.nio.charset.Charset
import kotlin.math.PI

object Constants {
    const val PULSE_URL = "https://smartapi.smartpods.ca/api"
    const val SPLASH_TIMEOUT = 2000
    const val SHARED_REF = "PULSE_APP_REFERENCE"
    const val USER_EMAIL = "email"
    const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    const val LOCATION_PERMISSION_REQUEST_CODE = 2
    const val APP_KEY = "1E55B758773022899498EDEF044CC28E917C04D2F3B1E691BBBAC8155F252CD4"
    const val APP_KEY_ACCESS = "SPAPPKEYCHAIN"

    const val app_token = "app_token"
    const val token_dated = "token_dated"
    const val token_expiry = "token_expiry"
    const val renewwal_key = "renewwal_key"
    const val organization_code = "OrgCode"
    const val email = "email"
    const val current_logged_user_type = "current_logged_user_type"
    const val users_table = "User"
    const val settings_table = "ProfileSettings"
    const val user_app_states = "UserAppStates"
    const val SPBLEUUID = "SPBLEUUID"
    const val HasLaunched = "HasLaunched"
    const val supportUrl = "https://www.smartpodstech.com/support/"

    const val arcBaseAngle = (3 * PI) / 2
    const val sitStandDifference = 7
    const val max_desk_height = 1000
    const val min_desk_height = 41

    const val presenceSentCount = 0
    const val presenceSentLimit = 2

    const val heartProgressBonus = 25
    const val kiloJoulsToKiloCalories = 0.000239006
    const val hoursPerDayActivity = 8
    val profileProgressOffState = listOf((mapOf("key" to "7",
                                        "end" to 3.141592653589793,
                                        "start" to 4.71238898038469,
                                        "value" to 0)))
    const val defaultProfileSettingsCommand = "P3,7|3300,4~"
    val defaultProfileSettingsMovement = listOf(hashMapOf("sweep" to 360,"start" to 270, "key" to 7, "value" to 2700, "raw" to 3),
        hashMapOf("sweep" to 30,"start" to 240,"key" to 4, "value" to 3300, "raw" to 3300))

    const val defaultProfileHexString = "1404704cffff02da03f2026703f7000d00007174"
    const val defaultSittingPosition = 730
    const val defaultStandingPosition = 1100

    /** BIT MASK AND CRC */

    const val BIT15_0_MASK = 0xFFFF
    const val BIT16_MASK = 0x8000
    const val INIT_CRC16_VAL = 0xffff
    const val CRC16_POLY = 0xBAAD
    const val CRC16_ENCRYPTED_POLY = 0x1021

    /** UUID of the Client Characteristic Configuration Descriptor (0x2902). */
    const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
    const val CCC_SERVICE_UUID = "00FF"
    const val BLE_SERVICE_UUID = "0D18"
    const val BLE_UUID = "F0:08:D1:57:C8:D2"
    const val BLE_CHARACTERISTICS = "0000ff01-0000-1000-8000-00805f9b34fb"

    /** SP BLE Responses  */
    const val CHANGE_NAME_HEARTBEAT = "0x14 1F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8C 83" //"0x04 1F F8 21 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
    const val BLEPairingSuccessResponse =  "141f"
    const val PairingDesktopPriorityResponse = "1420"
    const val NewBlePairAttemptResponse = "1421"
    const val DesktopAppPriorityResponse = "1422"
    const val InvalidateCommandResponse = "1423"
    const val ResumeNormalBLEDataResponse = "1424"
    const val BLEGenericError = "4e"

    const val Pingkey = "NDVHRDg1SlU0NDNGMjM0NQ=="
    const val Pingiv = "Z0ZhNDMxMTIzNUdGNEZlZQ=="


    /** VIEW TAGS */
    const val HexadecimalRegex: String = "[0-9A-F]+"
    const val bleKeyConnected: String = "smartpods"

    const val surveyBadgeTag = 1909
    const val loginCode = 100
    const val registrationCode = 101
    const val activateCode = 102
    const val forgotPassword = 103
    const val resetPasswordCode = 104

}

enum class POP_UP_VIEW_TYPE {
    DEVICE_LIST,
    DEPARTMENT_LIST,
    BMI_INFO,
    UNKNOWN;

    companion object {
        operator fun invoke(rawValue: Int): POP_UP_VIEW_TYPE? {
            return when (rawValue) {
                0 -> POP_UP_VIEW_TYPE.DEVICE_LIST
                1 -> POP_UP_VIEW_TYPE.DEPARTMENT_LIST
                2 -> POP_UP_VIEW_TYPE.BMI_INFO
                4 -> POP_UP_VIEW_TYPE.UNKNOWN
                else -> null
            }
        }
    }
}

enum class PulseState {
    Unknown,
    Scanning,
    Connecting,
    EnterPinCode,
    BoxChangeName,
    BoxReconnect,
    Connected,
    Disconnected,
    BoxError;

    companion object {
        operator fun invoke(rawValue: Int): PulseState? {
            return when (rawValue) {
                0 -> PulseState.Unknown
                1 -> PulseState.Scanning
                2 -> PulseState.Connecting
                3 -> PulseState.EnterPinCode
                4 -> PulseState.BoxChangeName
                5 -> PulseState.BoxReconnect
                200 -> PulseState.Connected
                400 -> PulseState.Disconnected
                404 -> PulseState.BoxError
                else -> null
            }
        }
    }
}

enum class CommandType {
    ASCIIType,
    INTType,
    SHORTType,
    BYTEType,
    BYTEHEXType,
    BYTESHORTType;

    companion object{
        operator fun invoke(rawValue: Int): CommandType? {
            return when (rawValue) {
                0 -> ASCIIType
                1 -> INTType
                2 -> SHORTType
                3 -> BYTEType
                4 -> BYTEHEXType
                5 -> BYTESHORTType
                else -> null
            }
        }
    }
    val rawValue: Int
        get() {
            return when (this) {
                ASCIIType -> 0
                INTType -> 1
                SHORTType -> 2
                BYTEType -> 3
                BYTEHEXType -> 4
                BYTESHORTType -> 5
            }
        }
}

internal enum class LOGIN_RESULT_CODE {
    SUCCEEDED,
    REQUIRES_EMAIL_VERIFICATION,
    ACCOUNT_IS_LOCKED,
    FAILED_VERIFY_USER,
    INVALID_ORGANIZATION_OR_DESK,
    UNKNOWN;

    companion object {
        operator fun invoke(rawValue: Int): LOGIN_RESULT_CODE? {
            return when (rawValue) {
                0 -> LOGIN_RESULT_CODE.SUCCEEDED
                1 -> LOGIN_RESULT_CODE.REQUIRES_EMAIL_VERIFICATION
                2 -> LOGIN_RESULT_CODE.ACCOUNT_IS_LOCKED
                3 -> LOGIN_RESULT_CODE.FAILED_VERIFY_USER
                4 -> LOGIN_RESULT_CODE.INVALID_ORGANIZATION_OR_DESK
                5 -> LOGIN_RESULT_CODE.UNKNOWN
                else -> null
            }
        }
    }

    val rawValue: Int
        get() {
            return when (this) {
                LOGIN_RESULT_CODE.SUCCEEDED -> 0
                LOGIN_RESULT_CODE.REQUIRES_EMAIL_VERIFICATION -> 1
                LOGIN_RESULT_CODE.ACCOUNT_IS_LOCKED -> 2
                LOGIN_RESULT_CODE.FAILED_VERIFY_USER -> 3
                LOGIN_RESULT_CODE.INVALID_ORGANIZATION_OR_DESK -> 4
                LOGIN_RESULT_CODE.UNKNOWN -> 5
            }
        }
}

internal enum class MovementType {
    DOWN,
    UP,
    INVALID;

    val movement: String
        get() {
            return when (this) {
                MovementType.DOWN -> "DOWN"
                MovementType.UP -> "UP"
                MovementType.INVALID -> "INVALID"
            }
        }

    val readableMovement: String
        get() {
            return when (this) {
                MovementType.DOWN -> "Sit"
                MovementType.UP -> "Stand"
                MovementType.INVALID -> "Invalid"
            }
        }

    val movementRawString: String
        get() {
            return when (this) {
                MovementType.DOWN -> "DOWN"
                MovementType.UP -> "UP"
                MovementType.INVALID -> "INVALID"
            }
        }

    val rawValue: Int
        get() {
            return when (this) {
                MovementType.DOWN -> 7
                MovementType.UP -> 4
                MovementType.INVALID -> 100
            }
        }
}

enum class ACTIVITY_PROFILE_TYPE {
    PRESETFIVE,
    PRESETFIFTEEN,
    PRESETTHIRTY,
    CUSTOMTHIRTY,
    CUSTOMSIXTY;

    companion object{
        operator fun invoke(rawValue: Int): ACTIVITY_PROFILE_TYPE? {
            return when (rawValue) {
                0 -> PRESETFIVE
                1 -> PRESETFIFTEEN
                2 -> PRESETTHIRTY
                3 -> CUSTOMTHIRTY
                4 -> CUSTOMSIXTY
                else -> null
            }
        }
    }
    val rawValue: Int
        get() {
            return when (this) {
                PRESETFIVE -> 0
                PRESETFIFTEEN -> 1
                PRESETTHIRTY -> 2
                CUSTOMTHIRTY -> 3
                CUSTOMSIXTY -> 4
            }
        }
}

enum class CUSTOM_ACTIVITY_PROFILE {
    NONE,
    THIRTY,
    SIXTY;

    companion object{
        operator fun invoke(rawValue: Int): CUSTOM_ACTIVITY_PROFILE? {
            return when (rawValue) {
                0 -> NONE
                30 -> THIRTY
                60 -> SIXTY
                else -> NONE
            }
        }
    }
    val rawValue: Int
        get() {
            return when (this) {
                NONE -> 0
                THIRTY -> 30
                SIXTY -> 30
            }
        }
}

enum class DESK_MODE {
    AUTOMATIC,
    INTERACTIVE,
    MANUAL,
    NONE;

    companion object{
        operator fun invoke(rawValue: String): DESK_MODE? {
            return when (rawValue) {
                "Automatic" -> AUTOMATIC
                "Interactive" -> INTERACTIVE
                "Manual" -> MANUAL
                else -> NONE
            }
        }
    }
    val rawValue: String
        get() {
            return when (this) {
                AUTOMATIC -> "Automatic"
                INTERACTIVE -> "Interactive"
                MANUAL -> "Manual"
                NONE -> "none"
            }
        }
}

enum class CURRENT_LOGGED_USER {
    Guest,
    Cloud,
    Local,
    None;

    companion object{
        operator fun invoke(rawValue: String): CURRENT_LOGGED_USER? {
            return when (rawValue) {
                "guest" -> CURRENT_LOGGED_USER.Guest
                "cloud" -> CURRENT_LOGGED_USER.Cloud
                "local" -> CURRENT_LOGGED_USER.Local
                "none" -> CURRENT_LOGGED_USER.None
                else -> CURRENT_LOGGED_USER.None
            }
        }
    }
    val rawValue: String
        get() {
            return when (this) {
                CURRENT_LOGGED_USER.Guest -> "guest"
                CURRENT_LOGGED_USER.Cloud -> "cloud"
                CURRENT_LOGGED_USER.Local -> "local"
                CURRENT_LOGGED_USER.None -> "none"
            }
        }
}

enum class INVERT_TYPE {
    SIT,
    STAND,
    NONE;

    companion object{
        operator fun invoke(rawValue: Int): INVERT_TYPE? {
            return when (rawValue) {
                0 -> SIT
                1 -> STAND
                else -> NONE
            }
        }
    }
    val rawValue: Int
        get() {
            return when (this) {
                SIT -> 0
                STAND -> 1
                else -> -1
            }
        }
}

enum class GENDER {
    MALE,
    FEMALE,
    EMPTY;

    companion object{
        operator fun invoke(rawValue: Int): GENDER? {
            return when (rawValue) {
                1 -> MALE
                2 -> FEMALE
                else -> EMPTY
            }
        }
    }

    val rawValue: String
        get() {
            return when (this) {
                GENDER.MALE -> "MALE"
                GENDER.FEMALE -> "FEMALE"
                GENDER.EMPTY -> ""
            }
        }
}

enum class LIFESTYLE {
    SEDENTRY,
    MODERATELYACTIVE,
    VERYACTIVE,
    INVALID;


    companion object{
        operator fun invoke(rawValue: Int): LIFESTYLE? {
            return when (rawValue) {
                0 -> SEDENTRY
                1 -> MODERATELYACTIVE
                2 -> VERYACTIVE
                else -> INVALID
            }
        }
    }

    val rawValue: String
        get() {
            return when (this) {
                SEDENTRY -> "Sedentary"
                MODERATELYACTIVE -> "Moderately Active"
                VERYACTIVE -> "Very Active"
                INVALID -> ""
            }
        }
}

enum class LANGUAGE {
    EN,
    FR,
    ES,
    EMPTY;

    companion object{
        operator fun invoke(rawValue: Int): LANGUAGE? {
            return when (rawValue) {
                0 -> EN
                1 -> FR
                2 -> ES
                else -> EMPTY
            }
        }
    }

    val rawValue: String
        get() {
            return when (this) {
                EN -> "EN"
                FR -> "FR"
                ES -> "ES"
                EMPTY -> ""
            }
        }
}

enum class STEPTYPE {
    NONE,
    MANUAL,
    AUTOMATIC;

    companion object{
        operator fun invoke(rawValue: Int): STEPTYPE? {
            return when (rawValue) {
                1 -> MANUAL
                2 -> AUTOMATIC
                else -> NONE
            }
        }
    }

    val rawValue: String
        get() {
            return when (this) {
                MANUAL -> "MANUAL"
                AUTOMATIC -> "AUTOMATIC"
                NONE -> "NONE"
            }
        }
}

enum class ProfileSettingsType {
    ACTIVE,
    MODERATELYACTIVE,
    VERYACTIVE,
    CUSTOM;

    companion object{
        operator fun invoke(rawValue: Int): ProfileSettingsType {
            return when (rawValue) {
                0 -> ACTIVE
                1 -> MODERATELYACTIVE
                2 -> VERYACTIVE
                else -> CUSTOM
            }
        }
    }

    val rawValue: Int
        get() {
            return when (this) {
                ACTIVE -> 0
                MODERATELYACTIVE -> 1
                VERYACTIVE -> 2
                CUSTOM -> 3
            }
        }
}

enum class REALM_OBJECT_TYPE {
    USERMODEL,
    SPCLIENTINFO,
    PULSEDEVICES,
    PULSEDATAPUSH,
    PULSEPROFILE,
    PULSEAPPSTATE,
    NONE;

    companion object{
        operator fun invoke(rawValue: Int): REALM_OBJECT_TYPE? {
            return when (rawValue) {
                0 -> USERMODEL
                1 -> SPCLIENTINFO
                2 -> PULSEDEVICES
                3 -> PULSEDATAPUSH
                4 -> PULSEPROFILE
                5 -> PULSEAPPSTATE
                else -> NONE
            }
        }
    }
    val rawValue: Int
        get() {
            return when (this) {
                USERMODEL -> 0
                SPCLIENTINFO -> 1
                PULSEDEVICES -> 2
                PULSEDATAPUSH -> 3
                PULSEPROFILE -> 4
                PULSEAPPSTATE -> 5
                else -> -1
            }
        }
}
