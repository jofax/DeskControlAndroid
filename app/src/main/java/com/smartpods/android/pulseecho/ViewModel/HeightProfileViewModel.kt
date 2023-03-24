package com.smartpods.android.pulseecho.ViewModel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.dariopellegrini.spike.SpikeProvider
import com.dariopellegrini.spike.mapping.fromJson
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.Utilities.Network.*
import com.smartpods.android.pulseecho.Utilities.SPRealmHelper
import com.smartpods.android.pulseecho.Utilities.Utilities
import org.json.JSONObject

class HeightProfileViewModel : BaseViewModel() {
    private var requestResponse: MutableLiveData<ProfileObject> = MutableLiveData<ProfileObject>()
    private val provider = SpikeProvider<UserProfileService>()
    lateinit var userProfile: PulseUserProfile

    fun requestProfileSettings(email: String): MutableLiveData<ProfileObject> {
        val params = Utilities.addTokenToParameter(email, hashMapOf("email" to email))
        provider.request(GetProfileSettings(params), { data ->
            println("Success requestProfileSettings response: " + data.results.toString())
            val profileJSON = JSONObject(data.results)
            if (profileJSON.optJSONObject("Settings")!=null) {

                val mProfile = ProfileObject(profileJSON)
                val mProfileObject = PulseUserProfile()
                mProfileObject.initWithObject(mProfile)
                mProfileObject.Email = email
                SPRealmHelper.saveObject(mProfileObject, email, REALM_OBJECT_TYPE.PULSEPROFILE)
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        setProfileSettings(email)
                    },
                    1200 // value in milliseconds
                )
                requestResponse.value = mProfile
            } else {
                requestResponse.value = ProfileObject(profileJSON)
                userProfile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE) as PulseUserProfile
            }

        }, { error ->
            println("Failed requestProfileSettings response: " + error.results.toString())
            var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
            mGenericResponse.put("Settigs",null)
            println("mGenericResponse : ${mGenericResponse}")
            requestResponse.value = ProfileObject(mGenericResponse)
        })
        return requestResponse
    }

    fun requestUpdateProfileSettings(email: String,
                                     settings: HashMap<String, Any>): MutableLiveData<ProfileObject>  {
        val mParams: HashMap<String, Any> = hashMapOf("settings" to settings)
        val mRequestParameters = Utilities.addTokenToParameter(email, mParams)
        print("mRequestParameters : $mRequestParameters")

        provider.request(UpdateProfileSettings(mRequestParameters), { data ->
            println("Success requestUpdateProfileSettings response: " + data.results.toString())
            val profileJSON = JSONObject(data.results)
            if (profileJSON.optJSONObject("Settings")!=null) {
                val mProfile = ProfileObject(profileJSON)
                val mProfileObject = PulseUserProfile()
                mProfileObject.initWithObject(mProfile)
                mProfileObject.Email = email
                SPRealmHelper.saveObject(mProfileObject, email, REALM_OBJECT_TYPE.PULSEPROFILE)
                requestResponse.value = mProfile
            } else {
                requestResponse.value = ProfileObject(profileJSON)
            }
        }, { error ->
            println("Failed requestUpdateProfileSettings response: " + error.results.toString())
            var mGenericResponse = Gson().fromJson<GenericResponse>(error.results.toString()).toJSONObj()
            mGenericResponse.put("Settigs",null)
            println("mGenericResponse : ${mGenericResponse}")
            requestResponse.value = ProfileObject(mGenericResponse)
        })
        return requestResponse

    }

    fun getProfileSettings(email: String): PulseUserProfile? {
       val mProfile =  SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE) as? PulseUserProfile
        println("HeightProfileViewModel getProfileSettings is null: $mProfile")
       if (mProfile == null) {
            println("getProfileSettings is null")
       }

        return mProfile
    }

    private fun setProfileSettings(email: String) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                userProfile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE) as PulseUserProfile
            },
            1000 // value in milliseconds
        )
    }
}