package com.smartpods.android.pulseecho.ViewModel

import android.content.DialogInterface
import android.provider.Settings.Global.getString
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.dariopellegrini.spike.SpikeProvider
import com.dariopellegrini.spike.mapping.fromJson
import com.google.gson.Gson
import com.smartpods.android.pulseecho.CustomUI.SPAlertDialogView
import com.smartpods.android.pulseecho.Model.GenericResponse
import com.smartpods.android.pulseecho.Model.UserSurvey
import com.smartpods.android.pulseecho.Model.UserSurveyQuestions
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.PulseApp.Companion.appResources
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.Network.GetSurvey
import com.smartpods.android.pulseecho.Utilities.Network.SendSurveyAnswers
import com.smartpods.android.pulseecho.Utilities.Network.SurveyService
import com.smartpods.android.pulseecho.Utilities.SPEventHandler
import com.smartpods.android.pulseecho.Utilities.Utilities
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class SurveyViewModel : BaseViewModel(){
    private var requestResponse: MutableLiveData<UserSurvey> = MutableLiveData<UserSurvey>()
    private var provider = SpikeProvider<SurveyService>()
    var surveyQuestions = mutableListOf<UserSurveyQuestions>()


    fun requestUserSurvey(email: String): MutableLiveData<UserSurvey> {
        //provider.queue?.cancelAll(this)
        val params = Utilities.addTokenToParameter(email, hashMapOf())
        println("requestUserSurvey params : $params")
        provider.request(GetSurvey(params), { data ->
            println("Success requestUserSurvey response: " + data.results.toString())
            val surveyJson = JSONObject(data.results)
            val mSurvey = UserSurvey(surveyJson)
            surveyQuestions = mSurvey.Questions
            println("_generic requestUserSurvey response: $mSurvey")
            requestResponse.value = mSurvey
        }, { error ->
            println("Failed requestUserSurvey response: " + error.results.toString())
            var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
            mGenericResponse.put("Survey",null)
            println("mGenericResponse : ${mGenericResponse}")
            requestResponse.value = UserSurvey(mGenericResponse)
        })
        return requestResponse
    }

    fun requestSubmitSurveyAnswers(email: String) {
        var mAnswers = mutableListOf<HashMap<String, Any>>()
        val mDate = Utilities.stringFormatDate(Date())
        for (item in surveyQuestions) {
            if (item.SelectedAnswer != - 1) {
                mAnswers.add(hashMapOf("AnswerID" to 0,
                    "QuestionRunID" to item.QuestionRunID,
                    "ScaleOptionID" to item.SelectedAnswer,
                    "Email" to email,
                    "Dated" to mDate))
            }
        }

        if (mAnswers.count() == 0) {
            //toastMessage.value = appResources.getString(R.string.survey_no_answers)
            toastMessage.value = SPEventHandler(appResources.getString(R.string.survey_no_answers))
            return
        }
        val allAnswers = hashMapOf<String, Any>("Answers" to mAnswers)
        val params = Utilities.addTokenToParameter(email, allAnswers)

        println("requested survey answers: $params")

        provider.request(SendSurveyAnswers(params), { data ->
            println("Success submit survey answers response: " + data.results.toString())
            var mGenericResponse = Gson().fromJson<GenericResponse>(data.results.toString())
            //toastMessage.value = appResources.getString(R.string.survey_success)
            toastMessage.value = SPEventHandler(appResources.getString(R.string.survey_success))

        }, { error ->
            println("Failed submit survey answers response: " + error.results.toString())
            var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString())
            //toastMessage.value = mGenericResponse.Message
            toastMessage.value = SPEventHandler(mGenericResponse.Message)

        })
    }

    fun updateSurveyQuestion(question: UserSurveyQuestions, idx: Int) {
        println("updateSurveyQuestion index: $idx | QuestionID: ${question.QuestionID} | SelectedAnswer: ${ question.SelectedAnswer }")
        for (item in surveyQuestions) {
            if (item.QuestionID == question.QuestionID) {
                item.SelectedAnswer = question.SelectedAnswer
                break
            }
        }
    }
}