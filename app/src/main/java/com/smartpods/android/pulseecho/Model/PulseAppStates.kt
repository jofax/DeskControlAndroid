package com.smartpods.android.pulseecho.Model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PulseAppStates: RealmObject() {
    @PrimaryKey var Email: String = ""
    var InteractivePopUpShowed = false
    var SafetyPopUpShowed = false
    var AutomaticControls = false
    var LegacyControls = false
    var IsNotifiedForHeightAdjustments = false
    var DeviceDisconnectedByUser = false
    var AppSaveProfile = ""

    fun initWithHash(data: HashMap<String, Any>, new: Boolean) {
        if (data.containsKey("InteractivePopUpShowed")) {
            InteractivePopUpShowed = data["InteractivePopUpShowed"] as Boolean
        }

        if (data.containsKey("SafetyPopUpShowed")) {
            SafetyPopUpShowed = data["SafetyPopUpShowed"] as Boolean
        }

        if (data.containsKey("SafetyPopUpShowed")) {
            SafetyPopUpShowed = data["SafetyPopUpShowed"] as Boolean
        }

        if (data.containsKey("AutomaticControls")) {
            AutomaticControls = data["AutomaticControls"] as Boolean
        }

        if (data.containsKey("LegacyControls")) {
            LegacyControls = data["LegacyControls"] as Boolean
        }

        if (data.containsKey("IsNotifiedForHeightAdjustments")) {
            IsNotifiedForHeightAdjustments = data["IsNotifiedForHeightAdjustments"] as Boolean
        }

        if (data.containsKey("DeviceDisconnectedByUser")) {
            DeviceDisconnectedByUser = data["DeviceDisconnectedByUser"] as Boolean
        }

        if (data.containsKey("AppSaveProfile")) {
            AppSaveProfile = data["AppSaveProfile"] as String
        }
    }
}