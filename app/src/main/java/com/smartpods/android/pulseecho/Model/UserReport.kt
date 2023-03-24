package com.smartpods.android.pulseecho.Model
import com.smartpods.android.pulseecho.Utilities.Utilities
import org.json.JSONObject
import org.json.JSONArray

interface UserStatistic {
    var report: JSONObject
    var ModePercentage: ReportModePercentage
    var UpDownPerHour: Double
    var TotalActivity: Double
    var ActivityByDesk: MutableList<UserStatisticItem>
    var GenericResponse: GenericResponseFromJSON
    var ModePercentageList: MutableList<UserStatisticItem>
}

class UserReport(var obj: JSONObject): UserStatistic {

    override lateinit var report: JSONObject
    override lateinit var ModePercentage: ReportModePercentage
    override var UpDownPerHour: Double = 0.0
    override var TotalActivity: Double = 0.0
    override var ActivityByDesk: MutableList<UserStatisticItem> = mutableListOf()
    override  var ModePercentageList: MutableList<UserStatisticItem> = mutableListOf()
    override var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(obj)

    init {
        if (obj.has("Report") && !obj.isNull("Report")) {
            val mreport = obj["Report"] as JSONObject
            report = mreport
            val mPercentage = report["ModePercentage"] as JSONObject
            val mDeskActivity = report["ActivityByDesk"] as JSONArray

            if (mPercentage.has("SemiAutomatic")) {
                val mSemiAutomatic = Utilities.doubleToPercentage(mPercentage["SemiAutomatic"] as Double)
                ModePercentageList.add(UserStatisticItem("Semi Automatic", mSemiAutomatic))
            }

            if (mPercentage.has("Automatic")) {
                val mAutomatic = Utilities.doubleToPercentage(mPercentage["Automatic"] as Double)
                ModePercentageList.add(UserStatisticItem("Automatic", mAutomatic))
            }

            if (mPercentage.has("Manual")) {
                val mManual = Utilities.doubleToPercentage(mPercentage["Manual"] as Double)
                ModePercentageList.add(UserStatisticItem("Manual", mManual))
            }

            ModePercentage = ReportModePercentage(mPercentage)
            UpDownPerHour =  report["UpDownPerHour"] as Double
            TotalActivity = report["TotalActivity"] as Double

            (0 until mDeskActivity.length()).forEach {
                val mActivity = ReportActivityByDesk(mDeskActivity.getJSONObject(it))
                val deskValPercentage = Utilities.doubleToPercentage(mActivity.PercentStanding)
                val deskItem = UserStatisticItem("Serial #: ${mActivity.SerialNumber}",deskValPercentage)
                ActivityByDesk.add(deskItem)
            }

        }

    }
}