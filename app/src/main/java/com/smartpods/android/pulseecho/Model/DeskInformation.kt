package com.smartpods.android.pulseecho.Model

import org.json.JSONArray
import org.json.JSONObject

interface DeskInformation {
    var PulseTokenObject: PulseToken
    var GenericResponse: GenericResponseFromJSON
    var BookingInfo: DeskBooking
    var mJSON: JSONObject
}

interface DeskBookingInfo {
    var IsEnabled: Boolean
    var IsHotelingStateEnabled: Boolean
    var TzOffset: Int
    var BookingId: Int
    var IsLoggedIn: Boolean
    var Email: String
    var BookingDate: String
    var UtcDateTime: String
    var Periods: Array<DeskBookingPeriods>
}

interface DeskBookingPeriods {
    var TimeIdFrom: Int
    var TimeIdTo: Int
}

class DeskInformationData(mObj: JSONObject) : DeskInformation {
    override var PulseTokenObject: PulseToken = PulseToken(mObj)
    override var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(mObj)
    override var BookingInfo: DeskBooking = DeskBooking(mObj)
    override  var mJSON: JSONObject = mObj
    init { }
}

class DeskBooking(mObj: JSONObject): DeskBookingInfo {
    override var IsEnabled: Boolean = false
    override var IsHotelingStateEnabled: Boolean = false
    override var TzOffset: Int = 0
    override var BookingId: Int = 0
    override var IsLoggedIn: Boolean = false
    override var Email: String = ""
    override var BookingDate: String = ""
    override var UtcDateTime: String = ""
    override var Periods: Array<DeskBookingPeriods> = arrayOf()
    init {
        if (mObj.has("DeskBookingInfo") && !mObj.isNull("DeskBookingInfo")) {
            val mDeskBooking = mObj["DeskBookingInfo"] as JSONObject
            IsEnabled = mDeskBooking["IsEnabled"] as Boolean
            IsHotelingStateEnabled = mDeskBooking["IsHotelingStateEnabled"] as Boolean
            TzOffset = mDeskBooking["TzOffset"] as Int
            BookingId = mDeskBooking["BookingId"] as Int
            IsLoggedIn = mDeskBooking["IsLoggedIn"] as Boolean

            Email = if (mDeskBooking.has("Email") && !mDeskBooking.isNull("Email")) {
                mDeskBooking["Email"] as String
            } else {
                ""
            }


            BookingDate = mDeskBooking["BookingDate"] as String
            UtcDateTime = mDeskBooking["UtcDateTime"] as String

            val mPeriods = mDeskBooking["Periods"] as JSONArray

            (0 until mPeriods.length()).forEach {
                val mObj = mPeriods.getJSONObject(it)
                Periods.plus(BookingPeriods(mObj))
            }
        }
    }
}

class BookingPeriods(mObj: JSONObject): DeskBookingPeriods {
    override var TimeIdFrom: Int = 0
    override var TimeIdTo: Int = 0

    init {
        TimeIdFrom = mObj["TimeIdFrom"] as Int
        TimeIdTo = mObj["TimeIdTo"] as Int
    }
}