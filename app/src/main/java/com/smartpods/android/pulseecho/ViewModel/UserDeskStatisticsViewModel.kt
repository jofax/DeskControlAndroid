package com.smartpods.android.pulseecho.ViewModel

import androidx.lifecycle.MutableLiveData
import com.dariopellegrini.spike.SpikeProvider
import com.dariopellegrini.spike.mapping.fromJson
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.Utilities.Network.*
import com.smartpods.android.pulseecho.Utilities.Utilities
import org.json.JSONObject

class UserDeskStatisticsViewModel : BaseViewModel() {
    private var requestResponse: MutableLiveData<UserReport> = MutableLiveData<UserReport>()
    private val provider = SpikeProvider<UserReportService>()

    fun requestUserStatisticSummary(email: String): MutableLiveData<UserReport> {
        //provider.queue?.cancelAll(this)
        val params = Utilities.addTokenToParameter(email, hashMapOf())
        println("requestUserStatisticSummary params : $params")
        provider.request(SummaryReport(params), { data ->
            println("Success requestUserStatisticSummary response: " + data.results.toString())
            val summaryJson = JSONObject(data.results)
            val mSummary = UserReport(summaryJson)
            println("_generic requestUserStatisticSummary response: $mSummary")
            requestResponse.value = mSummary

        }, { error ->
            println("Failed requestUserStatisticSummary response: " + error.results.toString())
            var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
            mGenericResponse.put("Report",null)
            println("mGenericResponse : ${mGenericResponse}")
            requestResponse.value = UserReport(mGenericResponse)
        })
        return requestResponse

    }
}