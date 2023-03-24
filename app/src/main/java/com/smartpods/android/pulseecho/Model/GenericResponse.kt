package com.smartpods.android.pulseecho.Model

import org.json.JSONObject

interface PulseGeneric {
        var Success: Boolean
        var ResultCode: Int
        var Message: String
}


class GenericResponse(
        override var Success: Boolean,
        override var ResultCode: Int,
        override var Message: String): PulseGeneric {

         fun toJSONObj(): JSONObject {
                 var mData = JSONObject()
                 mData.put("Success",Success)
                 mData.put("ResultCode",ResultCode)
                 mData.put("Message",Message)
                 return mData
         }
}

class GenericResponseFromJSON(val mObj: JSONObject): PulseGeneric {

        override var Success: Boolean = false
        override var ResultCode: Int = 0
        override var Message: String = ""

        init {
                Success = mObj["Success"] as Boolean
                ResultCode = mObj["ResultCode"] as Int
                Message = mObj["Message"] as String
        }

        fun initWithObject(mObj: JSONObject) {
                Success = mObj["Success"] as Boolean
                ResultCode = mObj["ResultCode"] as Int
                Message = mObj["Message"] as String
        }
}
