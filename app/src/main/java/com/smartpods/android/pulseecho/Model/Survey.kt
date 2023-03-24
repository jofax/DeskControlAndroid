package com.smartpods.android.pulseecho.Model

import org.json.JSONArray
import org.json.JSONObject

interface Survey {
    var survey: JSONObject
    var GenericResponse: GenericResponseFromJSON
    var SurveyID: Int
    var RuntimeID: Int
    var Category: Int
    var Name: String
    var StartDate: String
    var EndDate: String
    var Status: Int
    var QuestionCount: Int
    var PercentComplete: Double
    var Questions: MutableList<UserSurveyQuestions>
    var LastAnswerDated: String
}

open interface SurveyQuestions {
    var QuestionRunID: Int
    var QuestionID: Int
    var Answer: Int
    var Rank: Int
    var Language: Int
    var Text: String
    var ImageURL: String
    var ScaleOptions: MutableList<UserSurveyScaleOptions>
    open var SelectedScaleOptionID: Int
    var SelectedAnswer: Int
}

interface SurveyScaleOptions {
    var ID: Int
    var LanguageEnum: Int
    var Text: String
    var Rank: Int
    var RiskScore: Int
}


class UserSurvey(var response: JSONObject): Survey {
    override lateinit var survey: JSONObject
    override var SurveyID: Int = -1
    override var RuntimeID: Int = -1
    override var Category: Int = -1
    override var Name: String = ""
    override var StartDate: String = ""
    override var EndDate: String = ""
    override var Status: Int = -1
    override var QuestionCount: Int = -1
    override var PercentComplete: Double = 0.0
    override var Questions: MutableList<UserSurveyQuestions> = mutableListOf()
    override var LastAnswerDated: String = ""
    override var GenericResponse: GenericResponseFromJSON = GenericResponseFromJSON(response)

    init {
        if (response.has("Survey") && !response.isNull("Survey")) {
            val mSurvey = response["Survey"] as JSONObject
            survey = mSurvey
            SurveyID =  survey["SurveyID"] as Int
            RuntimeID =  survey["RuntimeID"] as Int
            Name =  survey["Name"] as String
            StartDate =  survey["StartDate"] as String
            EndDate =  survey["EndDate"] as String
            Status =  survey["Status"] as Int
            QuestionCount =  survey["QuestionCount"] as Int
            PercentComplete =  survey["PercentComplete"] as Double

            if (survey.has("LastAnswerDated") && !survey.isNull("LastAnswerDated")) {
                LastAnswerDated =  survey["LastAnswerDated"] as String
            }

            val mQuestions = survey["Questions"] as JSONArray

            (0 until mQuestions.length()).forEach { it ->
                val mQuestion = mQuestions.getJSONObject(it)
                var question = UserSurveyQuestions(mQuestion)
                Questions.add(question)
            }
        } else {
            survey = response
        }
    }
}

class UserSurveyQuestionsInit : SurveyQuestions {
    override var QuestionRunID: Int = -1
    override var QuestionID: Int = -1
    override var Answer: Int = -1
    override var Rank: Int = -1
    override var Language: Int = 0
    override var Text: String = ""
    override var ImageURL: String = ""
    override var ScaleOptions: MutableList<UserSurveyScaleOptions> = mutableListOf()
    override var SelectedScaleOptionID: Int = -1
    override var SelectedAnswer: Int = -1

    init {

    }
}

class UserSurveyQuestions(var response: JSONObject):SurveyQuestions {
    override var QuestionRunID: Int = -1
    override var QuestionID: Int = -1
    override var Answer: Int = -1
    override var Rank: Int = -1
    override var Language: Int = 0
    override var Text: String = ""
    override var ImageURL: String = ""
    override var ScaleOptions: MutableList<UserSurveyScaleOptions> = mutableListOf()
    override var SelectedScaleOptionID: Int = -1
    override var SelectedAnswer: Int = -1

    init {
        QuestionRunID =  response["QuestionRunID"] as Int
        QuestionID =  response["QuestionID"] as Int

        if (response.has("Answer") && !response.isNull("Answer")) {
            Answer =  response["Answer"] as Int
        }

        Rank =  response["Rank"] as Int
        Language =  response["Language"] as Int
        Text = response["Text"] as String

        if (response.has("ImageURL") && !response.isNull("ImageURL")) {
            ImageURL = response["ImageURL"] as String
        }


        SelectedScaleOptionID = response["SelectedScaleOptionID"] as Int

        if (response.has("SelectedAnswer") && !response.isNull("SelectedAnswer")) {
            SelectedAnswer = response["SelectedAnswer"] as Int
        }

        val mScaleOptions = response["ScaleOptions"] as JSONArray
        (0 until mScaleOptions.length()).forEach {
            val mOptions = UserSurveyScaleOptions(mScaleOptions.getJSONObject(it))
            ScaleOptions.add(mOptions)
        }
    }
}

class UserSurveyScaleOptions(var response: JSONObject): SurveyScaleOptions {
    override var ID: Int = -1
    override var LanguageEnum: Int = 0
    override var Text: String = ""
    override var Rank: Int = -1
    override var RiskScore: Int = -1

    init {
        ID =  response["ID"] as Int
        LanguageEnum =  response["LanguageEnum"] as Int
        Text =  response["Text"] as String
        Rank =  response["Rank"] as Int
        RiskScore =  response["RiskScore"] as Int
    }
}