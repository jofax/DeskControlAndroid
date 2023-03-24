package com.smartpods.android.pulseecho.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dariopellegrini.spike.SpikeProvider
import com.dariopellegrini.spike.mapping.fromJson
import com.dariopellegrini.spike.mapping.toJSONMap
import com.google.gson.Gson
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.Model.PulseToken
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.Network.*
import com.smartpods.android.pulseecho.Utilities.SPRealmHelper
import com.smartpods.android.pulseecho.Utilities.UserPreference
import com.smartpods.android.pulseecho.Utilities.Utilities
import io.realm.Realm
import org.json.JSONObject

class AuthenticateViewModel: BaseViewModel() {
    private var requestResponse: MutableLiveData<GenericResponse> = MutableLiveData<GenericResponse>()
    private val provider = SpikeProvider<LoginService>()

    fun requestLogin(email: String, pass: String): MutableLiveData<GenericResponse> {
        provider.request(LoginRequest(email,pass), { data ->
            println("Success response: " + data.results.toString())
            val loginModel = Gson().fromJson<LoginModel>(data.results.toString())
            val mGenericResponse = Gson().fromJson<GenericResponse>(data.results.toString())
            val pulseToken = Gson().fromJson<PulseToken>(data.results.toString())
            val mrawJsonObj = JSONObject(data.results)
            //val user = UserObject(mrawJsonObj)
            println("user response: " + mrawJsonObj.get("User"))
            println("generic response: $mGenericResponse")
            println("pulseToken response: $pulseToken")

            if (mGenericResponse.Success) {
                val realmConfiguration = Utilities.getRealmForUser(email, true)
                Realm.setDefaultConfiguration(realmConfiguration)

                if (mrawJsonObj.optJSONObject("User")!=null) {
                    val userObject = UserObject(mrawJsonObj)
                    SPRealmHelper.saveOrUpdateObjectWithData(userObject.toHashParameters(), email, REALM_OBJECT_TYPE.USERMODEL)
                    print("PREFERENCES STORED: " + UserPreference.prefs.getAll())
                }

                UserPreference.prefs.write(Constants.current_logged_user_type,CURRENT_LOGGED_USER.Cloud.rawValue)

                if (pulseToken.SessionKey != null) {
                    //save to db
                    UserPreference.prefs.write(Constants.USER_EMAIL,email)
                    UserPreference.prefs.write("token", pulseToken.SessionKey)

                    SPRealmHelper.saveOrUpdateObjectWithData(pulseToken.toHashParameters(), email, REALM_OBJECT_TYPE.SPCLIENTINFO)

                    val userProfileViewModel = HeightProfileViewModel()
                    userProfileViewModel.requestProfileSettings(email)

                    SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf("InteractivePopUpShowed" to false,
                        "SafetyPopUpShowed" to false,
                        "AutomaticControls" to false,
                        "LegacyControls" to false,
                        "IsNotifiedForHeightAdjustments" to false,
                        "DeviceDisconnectedByUser" to false,
                        "AppSaveProfile" to ""), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)
                }
            }
            requestResponse.value = GenericResponse(true,0,"")
            }, {error ->
                println("Failed response: " + error.results.toString())
                val mResponse = GenericResponse(false,3,"")
                requestResponse.value = mResponse
            }
        )

        return requestResponse
    }

    fun requestRegister(email: String, pass: String): MutableLiveData<GenericResponse> {
        provider.request(RegisterRequest(email, pass), { data ->
            println("Success requestRegister response: " + data.results.toString())
            val mGeneric = Gson().fromJson<GenericResponse>(data.results.toString())
            println("_generic requestRegister response: $mGeneric")
            requestResponse.value = mGeneric

        }, { error ->
            println("Failed requestRegister response: " + error.results.toString())
            val mResponse = GenericResponse(false,3,"")
            requestResponse.value = mResponse
        })
        return requestResponse
    }

    fun requestResendActivationCode(email: String): MutableLiveData<GenericResponse> {
        provider.request(ResendActivateAccountRequest(email), { data ->
            val mGeneric = Gson().fromJson<GenericResponse>(data.results.toString())
            println("Success requestResendActivationCode response: $mGeneric")
            requestResponse.value = mGeneric
        }, { error ->
            println("Failed requestResendActivationCode response: " + error.results.toString())
            val mResponse = GenericResponse(false,0,"")
            requestResponse.value = mResponse
        })

        return requestResponse
    }

    fun requestActivateAccount(email: String, userPass: String, actCode: String): MutableLiveData<GenericResponse> {
        println("requestActivateAccount params: $email | $userPass | $actCode")
        provider.request(ActivateAccountRequest(email,actCode), { data ->
            val mGeneric = Gson().fromJson<GenericResponse>(data.results.toString())
            println("Success requestActivateAccount response: $mGeneric")

            if (mGeneric.ResultCode == 0 && mGeneric.Success) {
                this.requestLogin(email, userPass)
            } else {
                val mResponse = GenericResponse(false,4,appContext.getString(R.string.login_invalid_code))
                requestResponse.value = mResponse
            }

        }, { error ->
            println("Failed requestActivateAccount response: " + error.results.toString())
            val mResponse = GenericResponse(false,0,"")
            requestResponse.value = mResponse
        })

        return requestResponse
    }

    fun requestForgotPasswordCode(email :String) :MutableLiveData<GenericResponse> {
        println("requestForgotPasswordCode params: $email")

        provider.request(ForgotPasswordRequest(email), { data ->
            val mGeneric = Gson().fromJson<GenericResponse>(data.results.toString())
            println("Success requestForgotPasswordCode response: $mGeneric")

            if (mGeneric.Success) {
                requestResponse.value = mGeneric
            } else {
                val mResponse = GenericResponse(false,0,appContext.getString(R.string.login_invalid_code))
                requestResponse.value = mResponse
            }

        }, { error ->
            println("Failed requestForgotPasswordCode response: " + error.results.toString())
            val mResponse = GenericResponse(false,4,"")
            requestResponse.value = mResponse
        })

        return requestResponse
    }

    fun requestResetPassword(email: String, pass: String, activateCode: String): MutableLiveData<GenericResponse> {
        provider.request(ResetPasswordRequest(email, pass, activateCode), { data ->
            println("Success requestResetPassword response: " + data.results.toString())
            val mGeneric = Gson().fromJson<GenericResponse>(data.results.toString())
            println("_generic requestResetPassword response: $mGeneric")
            requestResponse.value = mGeneric

        }, { error ->
            println("Failed requestResetPassword response: " + error.results.toString())
            val mResponse = GenericResponse(false,0,"")
            requestResponse.value = mResponse
        })
        return requestResponse
    }

    fun guestLogin() {
        val email = "guest"
        val realmConfiguration = Utilities.getRealmForUser(email, true)
        Realm.setDefaultConfiguration(realmConfiguration)
        UserPreference.prefs.write(Constants.current_logged_user_type,CURRENT_LOGGED_USER.Guest.rawValue)

        /*val mInfo = SPClientInfo()
        mInfo.initWithObject(pulseToken, email)
        SPRealmHelper.saveObject(mInfo,email,REALM_OBJECT_TYPE.SPCLIENTINFO)

        val userProfileViewModel = HeightProfileViewModel()
        userProfileViewModel.requestProfileSettings(email)*/

        SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf("InteractivePopUpShowed" to false,
            "SafetyPopUpShowed" to false,
            "AutomaticControls" to false,
            "LegacyControls" to false,
            "IsNotifiedForHeightAdjustments" to false,
            "DeviceDisconnectedByUser" to false,
            "AppSaveProfile" to ""), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)


    }
}