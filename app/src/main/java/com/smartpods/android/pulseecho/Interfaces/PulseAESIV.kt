package com.smartpods.android.pulseecho.Interfaces
import android.util.Base64
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.byteArrayOfInts
import com.smartpods.android.pulseecho.Utilities.toHex
import kotlin.properties.Delegates

interface PulseAESIV {
    var Length: Int
    var Command: Int
    var PayloadAESIV0: UInt
    var PayloadAESIV1: UInt
    var PayloadAESIV2: UInt
    var PayloadAESIV3: UInt
    var PayloadAESIV4: UInt
    var PayloadAESIV5: UInt
    var PayloadAESIV6: UInt
    var PayloadAESIV7: UInt
    var PayloadAESIV8: UInt
    var PayloadAESIV9: UInt
    var PayloadAESIV10: UInt
    var PayloadAESIV11: UInt
    var PayloadAESIV12: UInt
    var PayloadAESIV13: UInt
    var PayloadAESIV14: UInt
    var PayloadAESIV15: UInt
    var CRCHighByte: UInt
    var CRCLowByte: UInt
    var aesIV: String
}

data class AESIVObject(private val data: ByteArray) : PulseAESIV {

    var aesIVDataBytes = data.map { it.toUByte() }
    var aesIVData : List<UByte> by Delegates.notNull()

    override var Length: Int = 0
    override var Command: Int  = 0
    override var PayloadAESIV0: UInt = 0u
    override var PayloadAESIV1: UInt = 0u
    override var PayloadAESIV2: UInt = 0u
    override var PayloadAESIV3: UInt = 0u
    override var PayloadAESIV4: UInt = 0u
    override var PayloadAESIV5: UInt = 0u
    override var PayloadAESIV6: UInt = 0u
    override var PayloadAESIV7: UInt = 0u
    override var PayloadAESIV8: UInt = 0u
    override var PayloadAESIV9: UInt = 0u
    override var PayloadAESIV10: UInt = 0u
    override var PayloadAESIV11: UInt = 0u
    override var PayloadAESIV12: UInt = 0u
    override var PayloadAESIV13: UInt = 0u
    override var PayloadAESIV14: UInt = 0u
    override var PayloadAESIV15: UInt = 0u
    override var CRCHighByte: UInt = 0u
    override var CRCLowByte: UInt = 0u
    override var aesIV: String = ""

    init {
        this.aesIVData = this.aesIVDataBytes
        this.aesIVData[0].also { this.Length = it.toInt() }
        this.aesIVData[1].also { this.Command = it.toInt() }
        this.aesIVData[2].also { this.PayloadAESIV0 = it.toUInt() }
        this.aesIVData[3].also { this.PayloadAESIV1 = it.toUInt() }
        this.aesIVData[4].also { this.PayloadAESIV2 = it.toUInt() }
        this.aesIVData[5].also { this.PayloadAESIV3 = it.toUInt() }
        this.aesIVData[6].also { this.PayloadAESIV4 = it.toUInt() }
        this.aesIVData[7].also { this.PayloadAESIV5 = it.toUInt() }
        this.aesIVData[8].also { this.PayloadAESIV6 = it.toUInt() }
        this.aesIVData[9].also { this.PayloadAESIV7 = it.toUInt() }
        this.aesIVData[10].also { this.PayloadAESIV8 = it.toUInt() }
        this.aesIVData[11].also { this.PayloadAESIV9 = it.toUInt() }
        this.aesIVData[12].also { this.PayloadAESIV10 = it.toUInt() }
        this.aesIVData[13].also { this.PayloadAESIV11 = it.toUInt() }
        this.aesIVData[14].also { this.PayloadAESIV12 = it.toUInt() }
        this.aesIVData[15].also { this.PayloadAESIV13 = it.toUInt() }
        this.aesIVData[16].also { this.PayloadAESIV14 = it.toUInt() }
        this.aesIVData[17].also { this.PayloadAESIV15 = it.toUInt() }
        this.aesIVData[18].also { this.CRCHighByte = it.toUInt() }
        this.aesIVData[19].also { this.CRCLowByte = it.toUInt() }

        val mAesIV: ByteArray = byteArrayOfInts(this.PayloadAESIV0.toInt(),
            this.PayloadAESIV1.toInt(),
            this.PayloadAESIV2.toInt(),
            this.PayloadAESIV3.toInt(),
            this.PayloadAESIV4.toInt(),
            this.PayloadAESIV5.toInt(),
            this.PayloadAESIV6.toInt(),
            this.PayloadAESIV7.toInt(),
            this.PayloadAESIV8.toInt(),
            this.PayloadAESIV9.toInt(),
            this.PayloadAESIV10.toInt(),
            this.PayloadAESIV11.toInt(),
            this.PayloadAESIV12.toInt(),
            this.PayloadAESIV13.toInt(),
            this.PayloadAESIV14.toInt(),
            this.PayloadAESIV15.toInt())

        //141586a8537b6bdb669f204da8ea93da12f2c0af

        //val mValue = Utilities.convertByteArrayToInt(mAesIV)

        //val mValue = String(mAesIV, charset("UTF-8"))

        //println("PulseAESIV : ${Base64.encodeToString(mValue.toByteArray(),0,mValue.length,Base64.DEFAULT,)}")

        //this.aesIV = "${Utilities.convertByteArrayToString(mAesIV)}"


        //val mValue = String(mAesIV, charset("UTF-8"))
        //this.aesIV = "${Utilities.encodeToBase64(mValue)}"

        this.aesIV = mAesIV.toHex()
    }
}