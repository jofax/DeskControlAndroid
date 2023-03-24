package com.smartpods.android.pulseecho.ViewModel

import androidx.lifecycle.MutableLiveData
import com.dariopellegrini.spike.SpikeProvider
import com.dariopellegrini.spike.mapping.fromJson
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.Utilities.Network.*
import com.smartpods.android.pulseecho.Utilities.SPRealmHelper
import com.smartpods.android.pulseecho.Utilities.UserPreference
import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class UserProfileViewModel : BaseViewModel() {
    private var requestUserResponse: MutableLiveData<UserObject> = MutableLiveData<UserObject>()
    private var requestDepartmentsResponse: MutableLiveData<Departments> = MutableLiveData<Departments>()
    private var requestUserRiskAssementResponse: MutableLiveData<UserRiskManagement> = MutableLiveData<UserRiskManagement>()
    private val provider = SpikeProvider<UserService>()

    fun requestUserDetails(email: String): MutableLiveData<UserObject> {
        //provider.queue?.cancelAll(this)
        val params = Utilities.addTokenToParameter(email, hashMapOf("email" to email))
        runBlocking {
            provider.request(GetUser(params), { data ->
                println("Success requestUserDetails response: " + data.results.toString())
                val mrawJsonObj = JSONObject(data.results)
                if (mrawJsonObj.optJSONObject("User")!=null) {
                    val userObject = UserObject(mrawJsonObj)
                    SPRealmHelper.saveOrUpdateObjectWithData(userObject.toHashParameters(), email, REALM_OBJECT_TYPE.USERMODEL)
                    requestUserResponse.value = userObject

                } else {
                    requestUserResponse.value = UserObject(mrawJsonObj)
                }

            }, { error ->
                println("Failed requestUserDetails response: " + error.results.toString())
//                var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
//                mGenericResponse.put("User",null)
//                println("mGenericResponse : ${mGenericResponse}")
//                val mUser = UserObject(mGenericResponse)
//                requestUserResponse.value = mUser
            })
        }

        return requestUserResponse
    }

    fun requestDepartmentList(email: String): MutableLiveData<Departments> {
        //provider.queue?.cancelAll(this)
        val params = Utilities.addTokenToParameter(email, hashMapOf("email" to email))

        runBlocking {
            provider.request(GetDepartments(params), { data ->
                println("Success requestDepartmentList response: " + data.results.toString())
                val mrawJsonObj = JSONObject(data.results)
                requestDepartmentsResponse.value = Departments(mrawJsonObj)

            }, { error ->
                println("Failed requestDepartmentList response: " + error.results.toString())
                var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
                mGenericResponse.put("User",null)
                val mDepartments = Departments(mGenericResponse)
                requestDepartmentsResponse.value = mDepartments
            })
        }

        return requestDepartmentsResponse
    }

    fun requestUpdateUser(email: String, userObject: UserObject): MutableLiveData<UserObject>   {
        //provider.queue?.cancelAll(this)

        val mUserParams: MutableMap<String, Any> = userObject.getUserParameters()
        var mParams: HashMap<String, Any> = hashMapOf("User" to mUserParams)
        val mRequestParameters = Utilities.addTokenToParameter(email, mParams)

        runBlocking {
            provider.request(UpdateUser(mRequestParameters), { data ->
                println("Success requestUpdateUser response: " + data.results.toString())
                val mrawJsonObj = JSONObject(data.results)
                if (mrawJsonObj.optJSONObject("User")!=null) {
                    val userObject = UserObject(mrawJsonObj)
                    val mUserModel = UserModel()
                    mUserModel.initWithObject(userObject, false)
                    SPRealmHelper.saveObject(mUserModel, email, REALM_OBJECT_TYPE.USERMODEL)
                    requestUserResponse.value = userObject
                } else {
                    requestUserResponse.value = UserObject(mrawJsonObj)
                }
            }, { error ->
                println("Failed requestUpdateUser response: " + error.results.toString())
                var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
                mGenericResponse.put("User",null)
                println("mGenericResponse : ${mGenericResponse}")
                val mUser = UserObject(mGenericResponse)
                requestUserResponse.value = mUser
            })
        }

        return requestUserResponse

    }

    fun requestUserRiskAssement(email: String): MutableLiveData<UserRiskManagement> {
        val params = Utilities.addTokenToParameter(email, hashMapOf("email" to email))
        runBlocking {
            provider.request(GetUserRiskAssessment(params), { data ->
                println("Success requestUserRiskAssements response: " + data.results.toString())
                val riskResponse = JSONObject(data.results)
                val mRiskAssessment = UserRiskManagement(riskResponse)
                requestUserRiskAssementResponse.value = mRiskAssessment
            }, { error ->
                println("Failed requestUserRiskAssements response: " + error.results.toString())
                var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
                println("mGenericResponse : ${mGenericResponse}")
                val mrisk = UserRiskManagement(mGenericResponse)
                requestUserRiskAssementResponse.value = mrisk
            })
        }
        return requestUserRiskAssementResponse
    }

    fun getUserLocally(email: String): UserModel {
        return SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.USERMODEL) as UserModel
    }
}