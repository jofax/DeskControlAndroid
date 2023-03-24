package com.smartpods.android.pulseecho.Interfaces

import android.util.Base64
import com.smartpods.android.pulseecho.Utilities.BLE.toHexString
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.byteArrayOfInts
import com.smartpods.android.pulseecho.Utilities.toHex
import kotlin.properties.Delegates

interface PulseAESKey {
    var Length: Int
    var Command: Int
    var PayloadAESKey0: UInt
    var PayloadAESKey1: UInt
    var PayloadAESKey2: UInt
    var PayloadAESKey3: UInt
    var PayloadAESKey4: UInt
    var PayloadAESKey5: UInt
    var PayloadAESKey6: UInt
    var PayloadAESKey7: UInt
    var PayloadAESKey8: UInt
    var PayloadAESKey9: UInt
    var PayloadAESKey10: UInt
    var PayloadAESKey11: UInt
    var PayloadAESKey12: UInt
    var PayloadAESKey13: UInt
    var PayloadAESKey14: UInt
    var PayloadAESKey15: UInt
    var CRCHighByte: UInt
    var CRCLowByte: UInt
    var aesKey: String
}

data class AESKeyObject(private val data: ByteArray) : PulseAESKey {
    var aesKeyDataBytes = data.map { it.toUByte() }
    var aesKeyData : List<UByte> by Delegates.notNull()

    override var Length: Int = 0
    override var Command: Int  = 0
    override var PayloadAESKey0: UInt = 0u
    override var PayloadAESKey1: UInt = 0u
    override var PayloadAESKey2: UInt = 0u
    override var PayloadAESKey3: UInt = 0u
    override var PayloadAESKey4: UInt = 0u
    override var PayloadAESKey5: UInt = 0u
    override var PayloadAESKey6: UInt = 0u
    override var PayloadAESKey7: UInt = 0u
    override var PayloadAESKey8: UInt = 0u
    override var PayloadAESKey9: UInt = 0u
    override var PayloadAESKey10: UInt = 0u
    override var PayloadAESKey11: UInt = 0u
    override var PayloadAESKey12: UInt = 0u
    override var PayloadAESKey13: UInt = 0u
    override var PayloadAESKey14: UInt = 0u
    override var PayloadAESKey15: UInt = 0u
    override var CRCHighByte: UInt = 0u
    override var CRCLowByte: UInt = 0u
    override var aesKey = ""

    init {
        this.aesKeyData = this.aesKeyDataBytes
        this.aesKeyData[0].also { this.Length = it.toInt() }
        this.aesKeyData[1].also { this.Command = it.toInt() }
        this.aesKeyData[2].also { this.PayloadAESKey0 = it.toUInt() }
        this.aesKeyData[3].also { this.PayloadAESKey1 = it.toUInt() }
        this.aesKeyData[4].also { this.PayloadAESKey2 = it.toUInt() }
        this.aesKeyData[5].also { this.PayloadAESKey3 = it.toUInt() }
        this.aesKeyData[6].also { this.PayloadAESKey4 = it.toUInt() }
        this.aesKeyData[7].also { this.PayloadAESKey5 = it.toUInt() }
        this.aesKeyData[8].also { this.PayloadAESKey6 = it.toUInt() }
        this.aesKeyData[9].also { this.PayloadAESKey7 = it.toUInt() }
        this.aesKeyData[10].also { this.PayloadAESKey8 = it.toUInt() }
        this.aesKeyData[11].also { this.PayloadAESKey9 = it.toUInt() }
        this.aesKeyData[12].also { this.PayloadAESKey10 = it.toUInt() }
        this.aesKeyData[13].also { this.PayloadAESKey11 = it.toUInt() }
        this.aesKeyData[14].also { this.PayloadAESKey12 = it.toUInt() }
        this.aesKeyData[15].also { this.PayloadAESKey13 = it.toUInt() }
        this.aesKeyData[16].also { this.PayloadAESKey14 = it.toUInt() }
        this.aesKeyData[17].also { this.PayloadAESKey15 = it.toUInt() }
        this.aesKeyData[18].also { this.CRCHighByte = it.toUInt() }
        this.aesKeyData[19].also { this.CRCLowByte = it.toUInt() }

        val mAesKey: ByteArray = byteArrayOfInts(this.PayloadAESKey0.toInt(),
            this.PayloadAESKey1.toInt(),
            this.PayloadAESKey2.toInt(),
            this.PayloadAESKey3.toInt(),
            this.PayloadAESKey4.toInt(),
            this.PayloadAESKey5.toInt(),
            this.PayloadAESKey6.toInt(),
            this.PayloadAESKey7.toInt(),
            this.PayloadAESKey8.toInt(),
            this.PayloadAESKey9.toInt(),
            this.PayloadAESKey10.toInt(),
            this.PayloadAESKey11.toInt(),
            this.PayloadAESKey12.toInt(),
            this.PayloadAESKey13.toInt(),
            this.PayloadAESKey14.toInt(),
            this.PayloadAESKey15.toInt())

        //1414ce4b083e5dd0e2ab0b985f6c28f95071c27c
        //    ce4b083e5dd0e2ab0b985f6c28f95071


        //val mValue = Utilities.convertByteArrayToInt(mAesKey)
        //val mValue = String(mAesKey, charset("UTF-8"))

        //println("PulseAESKey : ${Base64.encodeToString(mAesKey,Base64.DEFAULT)}")
        //println("PulseAESKey base64 : ${Utilities.encodeToBase64(mValue)}")
        //this.aesKey = "${Utilities.encodeToBase64(mValue)}"

        this.aesKey = mAesKey.toHex()
    }
}