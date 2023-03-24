package com.smartpods.android.pulseecho.Model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class PulseDataPush: RealmObject() {
    @PrimaryKey var id: String = ""
    var Serial: String = ""
    var Email: String = ""
    var AESKey: String = ""
    var AESIV: String = ""

    fun initWithHash(data: HashMap<String, Any>) {
        if (data.containsKey("Serial")) {
            Serial = data["Serial"] as String
        }

        if (data.containsKey("Email")) {
            Email = data["Email"] as String
        }

        if (data.containsKey("AESKey")) {
            AESKey = data["AESKey"] as String
        }

        if (data.containsKey("AESIV")) {
            AESIV = data["AESIV"] as String
        }
    }
}