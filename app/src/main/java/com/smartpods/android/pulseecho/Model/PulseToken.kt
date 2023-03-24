package com.smartpods.android.pulseecho.Model
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject

open class PulseToken(var response: JSONObject) {

        val SessionDated: String = ""
        val SessionKey: String = ""
        val SessionExpiryDated: String = ""
        val RenewalKey: String = ""
        val OrgCode: String = ""
        val OrgName: String = ""

        init { }

        fun toHashParameters() : HashMap<String, Any> {
                return hashMapOf("SessionDated" to SessionDated,
                        "SessionKey" to SessionKey,
                        "SessionExpiryDated" to SessionExpiryDated,
                        "RenewalKey" to RenewalKey,
                        "OrgCode" to OrgCode,
                        "OrgName" to OrgName)
        }
}

open class SPClientInfo: RealmObject() {
        @PrimaryKey var Email: String = ""
        var SessionDated: String = ""
        var SessionKey: String = ""
        var SessionExpiryDated: String = ""
        var RenewalKey: String = ""
        var OrgCode: String = ""
        var OrgName: String = ""

        fun initWithObject(data: PulseToken, email: String) {
                Email = email
                SessionDated = data.SessionDated
                SessionKey = data.SessionKey
                SessionExpiryDated = data.SessionExpiryDated
                RenewalKey = data.RenewalKey
                OrgCode = data.OrgCode
                OrgName = data.OrgName

        }

        fun initWithHash(data: HashMap<String, Any>) {
                if (data.containsKey("SessionDated")) {
                        SessionDated = data["SessionDated"] as String
                }
                if (data.containsKey("SessionKey")) {
                        SessionKey = data["SessionKey"] as String
                }
                if (data.containsKey("SessionExpiryDated")) {
                        SessionExpiryDated = data["SessionExpiryDated"] as String
                }
                if (data.containsKey("RenewalKey")) {
                        RenewalKey = data["RenewalKey"] as String
                }
                if (data.containsKey("OrgCode")) {
                        OrgCode = data["OrgCode"] as String
                }
                if (data.containsKey("OrgName")) {
                        OrgName = data["OrgName"] as String
                }
        }
}