package com.smartpods.android.pulseecho.Model
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import org.json.JSONObject

interface User {
    var user: JSONObject
    var GenericResponse: GenericResponseFromJSON
    var DepartmentID: Int
    var Email: String
    var Firstname: String
    var Lastname: String
    var YearOfBirth: Int
    var Gender: Int
    var Language: Int
    var AcknowledgedWaiver: Boolean
    var WatchedSafetyVideo: Boolean
    var StepType: Int
    var LifeStyle: Int
    var JobDescription: Int
    var LogoutWhenNotDetected: Boolean
    var Height: Int
    var Weight: Double
    var BMI: Double
    var BMR: Double
    var Hearts: Hearts
    var TaskBarNotification: Boolean
    var AutoLogin: Boolean
    var AcknowledgedWaiverDate: String
    var IsImperial: Boolean
}

open class UserModel: RealmObject() {
    @PrimaryKey var Email: String = ""
    var DepartmentID: Int = 0
    var Firstname: String = ""
    var Lastname: String = ""
    var YearOfBirth: Int = 0
    var Gender: Int = 0
    var Language: Int = 0
    var AcknowledgedWaiver: Boolean = false
    var WatchedSafetyVideo: Boolean = false
    var StepType: Int = 0
    var LifeStyle: Int = 0
    var JobDescription: Int = 0
    var LogoutWhenNotDetected: Boolean = false
    var Height: Int = 0
    var Weight: Double = 0.0
    var BMI: Double = 0.0
    var BMR: Double = 0.0
    var HeartsToday: Double = 0.0
    var HeartsTotal: Int = 0
    var AvgHoursFillHeart: Double = 0.0
    var TaskBarNotification: Boolean = false
    var AutoLogin: Boolean = false
    var AcknowledgedWaiverDate: String = ""
    var IsImperial: Boolean = false

    fun initWithObject(data: UserObject, create: Boolean) {
        if (create) {
            Email = data.Email

        }
        DepartmentID = data.DepartmentID
        Firstname = data.Firstname
        Lastname = data.Lastname
        YearOfBirth = data.YearOfBirth
        Gender = data.Gender
        Language = data.Language
        AcknowledgedWaiver = data.AcknowledgedWaiver
        WatchedSafetyVideo = data.WatchedSafetyVideo
        StepType = data.StepType
        LifeStyle = data.LifeStyle
        JobDescription = data.JobDescription
        LogoutWhenNotDetected = data.LogoutWhenNotDetected
        Height = data.Height
        Weight = data.Weight
        BMI = data.BMI
        BMR = data.BMR
        HeartsToday = data.Hearts.Today
        HeartsTotal = data.Hearts.Total
        AvgHoursFillHeart = data.Hearts.AvgHoursFillHeart
        TaskBarNotification = data.TaskBarNotification
        AutoLogin = data.AutoLogin
        AcknowledgedWaiverDate = data.AcknowledgedWaiverDate
        IsImperial = data.IsImperial
    }

    fun initWithHash(data: HashMap<String, Any>) {
        if (data.containsKey("DepartmentID")) {
            DepartmentID = data["DepartmentID"] as Int
        }

        if (data.containsKey("Firstname")) {
            Firstname = data["Firstname"] as String
        }

        if (data.containsKey("Lastname")) {
            Lastname = data["Lastname"] as String
        }

        if (data.containsKey("YearOfBirth")) {
            YearOfBirth = data["YearOfBirth"] as Int
        }

        if (data.containsKey("Gender")) {
            Gender = data["Gender"] as Int
        }

        if (data.containsKey("Language")) {
            Language = data["Language"] as Int
        }

        if (data.containsKey("AcknowledgedWaiver")) {
            AcknowledgedWaiver = data["AcknowledgedWaiver"] as Boolean
        }

        if (data.containsKey("WatchedSafetyVideo")) {
            WatchedSafetyVideo = data["WatchedSafetyVideo"] as Boolean
        }

        if (data.containsKey("StepType")) {
            StepType = data["StepType"] as Int
        }

        if (data.containsKey("LifeStyle")) {
            LifeStyle = data["LifeStyle"] as Int
        }

        if (data.containsKey("JobDescription")) {
            JobDescription = data["JobDescription"] as Int
        }

        if (data.containsKey("LogoutWhenNotDetected")) {
            LogoutWhenNotDetected = data["LogoutWhenNotDetected"] as Boolean
        }

        if (data.containsKey("Height")) {
            Height = data["Height"] as Int
        }

        if (data.containsKey("Weight")) {
            Weight = data["Weight"] as Double
        }

        if (data.containsKey("BMI")) {
            BMI = data["BMI"] as Double
        }

        if (data.containsKey("BMR")) {
            BMR = data["BMR"] as Double
        }

        if (data.containsKey("Hearts")) {
            val _hearts: HashMap<String, Any> =  data["Hearts"] as HashMap<String, Any>
            HeartsToday = _hearts["Today"] as Double
            HeartsTotal = _hearts["Total"] as Int
            AvgHoursFillHeart = _hearts["AvgHoursFillHeart"] as Double
        }

        if (data.containsKey("TaskBarNotification")) {
            TaskBarNotification = data["TaskBarNotification"] as Boolean
        }

        if (data.containsKey("AutoLogin")) {
            AutoLogin = data["AutoLogin"] as Boolean
        }

        if (data.containsKey("AcknowledgedWaiverDate")) {
            AcknowledgedWaiverDate = data["AcknowledgedWaiverDate"] as String
        }

        if (data.containsKey("IsImperial")) {
            IsImperial = data["IsImperial"] as Boolean
        }
    }
}

open class UserObject(var obj: JSONObject) : User {
    override var Email: String = ""
    override var DepartmentID: Int = 0
    override var Firstname: String = ""
    override var Lastname: String = ""
    override var YearOfBirth: Int = 0
    override var Gender: Int = 0
    override var Language: Int = 0
    override var AcknowledgedWaiver: Boolean = false
    override var WatchedSafetyVideo: Boolean = false
    override var StepType: Int = 0
    override var LifeStyle: Int = 0
    override var JobDescription: Int = 0
    override var LogoutWhenNotDetected: Boolean = false
    override var Height: Int = 0
    override var Weight: Double = 0.0
    override var BMI: Double = 0.0
    override var BMR: Double = 0.0
    override var Hearts: Hearts = Hearts(0.0,0,0.0)
    override var TaskBarNotification: Boolean = false
    override var AutoLogin: Boolean = false
    override var AcknowledgedWaiverDate: String = ""
    override var IsImperial: Boolean = false
    lateinit override var user: JSONObject
    override var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(obj)

    init {
        if (obj.has("User") && !obj.isNull("User")) {
            user = obj["User"] as JSONObject
            Email = user["Email"] as String
            DepartmentID = user["DepartmentID"] as Int
            Firstname = user["Firstname"] as String
            Lastname = user["Lastname"] as String
            YearOfBirth = user["YearOfBirth"] as Int
            Gender = user["Gender"] as Int
            Language = user["Language"] as Int
            AcknowledgedWaiver = user["AcknowledgedWaiver"] as Boolean
            WatchedSafetyVideo = user["WatchedSafetyVideo"] as Boolean
            StepType = user["StepType"] as Int
            LifeStyle = user["LifeStyle"] as Int
            JobDescription = user["JobDescription"] as Int
            LogoutWhenNotDetected = user["LogoutWhenNotDetected"] as Boolean
            Height = user["Height"] as Int
            Weight = user["Weight"] as Double
            BMI = user["BMI"] as Double
            BMR = user["BMR"] as Double

            val _hearts =  user["Hearts"] as JSONObject

            Hearts = Hearts(_hearts["Today"] as Double,
                            _hearts["Total"] as Int,
                            _hearts["AvgHoursFillHeart"] as Double)

            TaskBarNotification = user["AcknowledgedWaiver"] as Boolean
            AutoLogin = user["AutoLogin"] as Boolean
            AcknowledgedWaiverDate = user["AcknowledgedWaiverDate"] as String
            IsImperial = user["IsImperial"] as Boolean
        }
    }

    fun toHashParameters(): HashMap<String,Any> {
        return hashMapOf(
            "DepartmentID" to DepartmentID,
            "Firstname" to Firstname,
            "Lastname" to Lastname,
            "YearOfBirth" to YearOfBirth,
            "Gender" to Gender,
            "Language" to Language,
            "AcknowledgedWaiverDate" to AcknowledgedWaiverDate,
            "WatchedSafetyVideo" to WatchedSafetyVideo,
            "StepType" to StepType,
            "LifeStyle" to LifeStyle,
            "JobDescription" to JobDescription,
            "LogoutWhenNotDetected" to LogoutWhenNotDetected,
            "Height" to Height,
            "Weight" to Weight,
            "BMI" to BMI,
            "BMR" to BMR,
            "Hearts" to hashMapOf("Today" to Hearts.Today,
                "Total" to Hearts.Total,
                "AvgHoursFillHeart" to Hearts.AvgHoursFillHeart),
            "TaskBarNotification" to TaskBarNotification,
            "AutoLogin" to AutoLogin,
            "AcknowledgedWaiverDate" to AcknowledgedWaiverDate,
            "IsImperial" to IsImperial)
    }

    fun getUserParameters(): MutableMap<String, Any> {
        return hashMapOf("Email" to Email,
            "DepartmentID" to DepartmentID,
            "Firstname" to Firstname,
            "Lastname" to Lastname,
            "Weight" to Weight,
            "Height" to Height,
            "YearOfBirth" to YearOfBirth,
            "Gender" to Gender,
            "LifeStyle" to LifeStyle,
            "AcknowledgedWaiverDate" to AcknowledgedWaiverDate)
    }
}