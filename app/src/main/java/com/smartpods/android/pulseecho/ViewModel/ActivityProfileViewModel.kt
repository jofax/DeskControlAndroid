package com.smartpods.android.pulseecho.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.Utilities.SPRealmHelper
import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlinx.coroutines.runBlocking
import java.io.IOException

class ActivityProfileViewModel : ViewModel() {
    private var requestResponse: MutableLiveData<ProfileObject> = MutableLiveData<ProfileObject>()
    private var hasUserProfile: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    private var profileUpdated: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    lateinit var userProfile: PulseUserProfile

    fun getProfileSettings(fragment: BaseFragment): MutableLiveData<Boolean> {
        runBlocking {
            val email = Utilities.getLoggedEmail()
            if (::userProfile.isInitialized) {
                userProfile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE) as PulseUserProfile
                hasUserProfile.value = true
            } else {
                requestProfileSettings(fragment).observe (fragment.viewLifecycleOwner, {
                    userProfile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE) as PulseUserProfile
                    println("getProfileSettings mTest value: ${userProfile.Email}")
                    hasUserProfile.value = true
                })
            }
        }

        return hasUserProfile

    }

    fun requestProfileSettings(fragment: BaseFragment): MutableLiveData<ProfileObject> {
        val email = Utilities.getLoggedEmail()
        val heightProfileViewModel = HeightProfileViewModel()
        runBlocking {
            try {
                if (email.isNotEmpty()) {
                    fragment.pulseMainActivity.showActivityLoader(true)
                    val hasNetwork =  PulseApp.appContext.let { fragment.pulseMainActivity.isNetworkAvailable(it) }
                    println("ActivityProfileViewModel hasNetwork : $hasNetwork")

                    if (hasNetwork) {
                        //request updated profile settings from cloud
                        heightProfileViewModel.requestProfileSettings(email).observe(fragment.viewLifecycleOwner, Observer {
                            fragment.pulseMainActivity.showActivityLoader(false)
                            if (it.GenericResponse.Success) {
                                if (it.Settings != null) {
                                    println("ActivityProfileViewModel hasNetwork : ${it.Settings}")
                                    requestResponse.value = it
                                }
                            } else {
                                if (it.GenericResponse.Message == "Unauthorized") {
                                    println("redirect to login")
                                    fragment.pulseMainActivity.redirectToLoginPage(email)
                                }
                            }
                        })
                    } else {
                        //fetch locally saved profile settings
                        val mProfile = heightProfileViewModel.getProfileSettings(email)
                        if (mProfile != null) {
                            requestResponse.value = ProfileObject(mProfile.toJSONObject())
                        } else {

                        }
                    }
                } else {}
            } catch (e: IOException) {
                e.message?.let { fragment.pulseMainActivity.fail(it) }
            }
        }

        return requestResponse
    }
    
    fun updateUserProfile(fragment: BaseFragment,
                          profileSettings: Int,
                          standTime1: Int,
                          standTime2: Int): MutableLiveData<Boolean> {

        val email = Utilities.getLoggedEmail()
        val heightProfileViewModel = HeightProfileViewModel()

        var mParams:HashMap<String, Any> = hashMapOf("email" to email,
            "ProfileID" to userProfile.ProfileID,
            "StandingTime1" to standTime1,
            "StandingTime2" to standTime2,
            "ProfileSettingType" to profileSettings,
            "SittingPosition" to userProfile.SittingPosition,
            "StandingPosition" to userProfile.StandingPosition,
            "IsInteractive" to userProfile.IsInteractive)

        //var mParameters: HashMap<String, Any> = hashMapOf("settings" to mParams)
        val mRequestParameters = Utilities.addTokenToParameter(email, mParams)

        println("mRequestParameters : $mRequestParameters")

        fragment.pulseMainActivity.showActivityLoader(true)
        val hasNetwork =  PulseApp.appContext.let { fragment.pulseMainActivity.isNetworkAvailable(it) }
        println("ActivityProfileViewModel hasNetwork : $hasNetwork")

        if (hasNetwork) {
            //request updated profile settings from cloud
            heightProfileViewModel.requestUpdateProfileSettings(email,mRequestParameters).observe(fragment.viewLifecycleOwner, Observer {
                fragment.pulseMainActivity.showActivityLoader(false)
                if (it.GenericResponse.Success) {
                    if (it.Settings != null) {
                        profileUpdated.value = true
                    }
                } else {
                    if (it.GenericResponse.Message == "Unauthorized") {
                        println("redirect to login")
                        fragment.pulseMainActivity.redirectToLoginPage(email)
                    }
                }
            })
        } else {
            //update locally profile settings
            var updatedUserProfile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE) as PulseUserProfile
            updatedUserProfile.StandingTime1 = standTime1
            updatedUserProfile.StandingTime2 = standTime2
            updatedUserProfile.ProfileSettingType = profileSettings
            SPRealmHelper.saveObject(updatedUserProfile, email, REALM_OBJECT_TYPE.PULSEPROFILE)
            fragment.pulseMainActivity.showToastView("Unable to update your profile to the cloud.")
            profileUpdated.value = true
        }



        return profileUpdated
    }
}