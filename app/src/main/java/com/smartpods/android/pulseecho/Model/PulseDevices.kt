package com.smartpods.android.pulseecho.Model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PulseDevices: RealmObject() {

    @PrimaryKey var Email: String = ""
    var BLEIdentifier: String = ""
    var Serial: String = ""
    var State: Int = 0
    var PeripheralName: String = ""
    var UserProfile: String = ""
    var DisconnectedByUser: Boolean = false

    fun initWithHash(data: HashMap<String, Any>, new: Boolean) {

        if (data.containsKey("Serial")) {
            Serial = data["Serial"] as String
        }

        if (data.containsKey("Identifier")) {
            BLEIdentifier = data["Identifier"] as String
        }

        if (data.containsKey("State")) {
            State = data["State"] as Int
        }

        if (data.containsKey("PeripheralName")) {
            PeripheralName = data["PeripheralName"] as String
        }

        if (data.containsKey("UserProfile")) {
            UserProfile = data["UserProfile"] as String
        }

        if (data.containsKey("DisconnectedByUser")) {
            DisconnectedByUser = data["DisconnectedByUser"] as Boolean
        }
    }
}

