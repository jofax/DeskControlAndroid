package com.smartpods.android.pulseecho.Model
import com.google.gson.Gson
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject


interface UserProfile {
    var Settings: JSONObject
    var ProfileID: Int
    var Email: String
    var StandingTime1: Int
    var StandingTime2: Int
    var ProfileSettingType: Int
    var SittingPosition: Int
    var StandingPosition: Int
    var IsInteractive: Boolean
    var GenericResponse: GenericResponseFromJSON
}

open class ProfileObject(var obj: JSONObject): UserProfile {
    override lateinit var Settings: JSONObject
    override var ProfileID: Int = 0
    override var Email: String = ""
    override var StandingTime1: Int = 0
    override var StandingTime2: Int = 0
    override var ProfileSettingType: Int = 0
    override var SittingPosition: Int = 0
    override var StandingPosition: Int = 0
    override var IsInteractive: Boolean = false
    override var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(obj)


    init {
        if (obj.has("Settings") && !obj.isNull("Settings")) {
            Settings = obj["Settings"] as JSONObject
            Email = Settings["Email"] as String
            ProfileID = Settings["ProfileID"] as Int
            StandingTime1 = Settings["StandingTime1"] as Int
            StandingTime2 = Settings["StandingTime2"] as Int
            ProfileSettingType = Settings["ProfileSettingType"] as Int
            SittingPosition = Settings["SittingPosition"] as Int
            StandingPosition = Settings["StandingPosition"] as Int
            IsInteractive = Settings["IsInteractive"] as Boolean
        }
    }
}

open class PulseUserProfile: RealmObject() {
    @PrimaryKey var Email: String = ""
    var ProfileID: Int = 0
    var StandingTime1: Int = 0
    var StandingTime2: Int = 0
    var ProfileSettingType: Int = 0
    var SittingPosition: Int = 0
    var StandingPosition: Int = 0
    var IsInteractive: Boolean = false

    fun initWithObject(profile: ProfileObject) {
        Email = profile.Email
        ProfileID = profile.ProfileID
        StandingTime1 = profile.StandingTime1
        StandingTime2 = profile.StandingTime2
        ProfileSettingType = profile.ProfileSettingType
        SittingPosition = profile.SittingPosition
        StandingPosition = profile.StandingPosition
        IsInteractive = profile.IsInteractive
    }

    fun initWithHash(data: HashMap<String, Any>, new: Boolean) {
        if (data.containsKey("Email")) {
            if (!new) {
                Email = data["Email"] as String
             }
        }

        if (data.containsKey("ProfileID")) {
            ProfileID = data["ProfileID"] as Int
        }

        if (data.containsKey("StandingTime1")) {
            StandingTime1 = data["StandingTime1"] as Int
        }

        if (data.containsKey("StandingTime2")) {
            StandingTime2 = data["StandingTime2"] as Int
        }

        if (data.containsKey("ProfileSettingType")) {
            ProfileSettingType = data["ProfileSettingType"] as Int
        }

        if (data.containsKey("SittingPosition")) {
            SittingPosition = data["SittingPosition"] as Int
        }

        if (data.containsKey("StandingPosition")) {
            StandingPosition = data["StandingPosition"] as Int
        }

        if (data.containsKey("IsInteractive")) {
            IsInteractive = data["IsInteractive"] as Boolean
        }
    }

    fun toJSONObject(): JSONObject {
        val pulseJSONObj = JSONObject()
        pulseJSONObj.put("Email",Email)
        pulseJSONObj.put("ProfileID",ProfileID)
        pulseJSONObj.put("StandingTime1",StandingTime1)
        pulseJSONObj.put("StandingTime2",StandingTime2)
        pulseJSONObj.put("ProfileSettingType",ProfileSettingType)
        pulseJSONObj.put("SittingPosition",SittingPosition)
        pulseJSONObj.put("StandingPosition",StandingPosition)
        pulseJSONObj.put("IsInteractive",IsInteractive)

        return JSONObject().put("Settings", pulseJSONObj)
    }

    fun getProfileParameters(obj: HashMap<String, Any> = hashMapOf()): HashMap<String, Any> {

        if (obj.count() > 0) {
            return hashMapOf("Email" to obj["Email"].toString(),
                "ProfileID" to obj["ProfileID"].toString().toInt(),
                "StandingTime1" to obj["StandingTime1"].toString().toInt(),
                "StandingTime2" to obj["StandingTime2"].toString().toInt(),
                "ProfileSettingType" to obj["ProfileSettingType"].toString().toInt(),
                "SittingPosition" to obj["SittingPosition"].toString().toInt(),
                "StandingPosition" to obj["StandingPosition"].toString().toInt(),
                "IsInteractive" to obj["IsInteractive"].toString().toInt())
        } else {
            return hashMapOf("Email" to Email,
                "ProfileID" to ProfileID,
                "StandingTime1" to StandingTime1,
                "StandingTime2" to StandingTime2,
                "ProfileSettingType" to ProfileSettingType,
                "SittingPosition" to SittingPosition,
                "StandingPosition" to StandingPosition,
                "IsInteractive" to IsInteractive)
        }
    }
}