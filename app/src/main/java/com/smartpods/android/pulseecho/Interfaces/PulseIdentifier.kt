package com.smartpods.android.pulseecho.Interfaces

import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.byteArrayOfInts
import com.smartpods.android.pulseecho.Utilities.toPositiveInt
import kotlin.properties.Delegates


interface PulseIdentifier {
    /** BLE STRINGS **/
    var Length: Int
    var Command: Int
    var DeskIDMSB: UInt
    var DeskIDByte3: UInt
    var DeskIDByte2: UInt
    var DeskIDLSB: UInt
    var VersionNumberMSB: UInt
    var VersionNumberByte: UInt
    var VersionNumberByte2: UInt
    var VersionNumberLSB: UInt
    var VersionBoardType: UInt
    var CRCHighByte: UInt
    var CRCLowByte: UInt
    var UpdateDownCount2: UInt
    var UpdateDownCount1: UInt
    var UpdateDownCount0: UInt
    var RegistrationID3: UInt
    var RegistrationID2: UInt
    var RegistrationID1: UInt
    var RegistrationID0: UInt

    var SerialNumber: String
    var Version: String
    var UpDownCount: String
    var RegistrationID: String
    var DeskID: Int

    /** END BLE STRINGS **/
}

data class IdentifierObject(private val data: ByteArray) : PulseIdentifier {

    var identifierDataBytes = data.map { it.toUByte() }
    var identifierData : List<UByte> by Delegates.notNull()

    override var Length: Int = 0
    override var Command: Int = 0

    override var DeskIDMSB: UInt = 0u
    override var DeskIDByte3: UInt = 0u
    override var DeskIDByte2: UInt = 0u
    override var DeskIDLSB: UInt = 0u

    override var VersionNumberMSB: UInt = 0u
    override var VersionNumberByte: UInt = 0u
    override var VersionNumberByte2: UInt = 0u
    override var VersionNumberLSB: UInt = 0u
    override var VersionBoardType: UInt = 0u

    override var UpdateDownCount2: UInt = 0u
    override var UpdateDownCount1: UInt = 0u
    override var UpdateDownCount0: UInt = 0u

    override var RegistrationID3: UInt = 0u
    override var RegistrationID2: UInt = 0u
    override var RegistrationID1: UInt = 0u
    override var RegistrationID0: UInt = 0u

    override var SerialNumber: String = ""
    override var Version: String = ""
    override var UpDownCount: String = ""
    override var RegistrationID: String = ""
    override var DeskID: Int = 0

    override var CRCHighByte: UInt = 0u
    override var CRCLowByte: UInt = 0u

    init {
        this.identifierData = identifierDataBytes
        this.identifierData[0].also { this.Length = it.toInt() }
        this.identifierData[1].also { this.Command = it.toInt() }

        this.identifierData[2].also { this.DeskIDMSB = it.toUInt() }
        this.identifierData[3].also { this.DeskIDByte3 = it.toUInt()  }
        this.identifierData[4].also { this.DeskIDByte2 = it.toUInt()  }
        this.identifierData[5].also { this.DeskIDLSB = it.toUInt()  }

        this.identifierData[6].also { this.VersionNumberMSB = it.toUInt() }
        this.identifierData[7].also { this.VersionNumberByte = it.toUInt()  }
        this.identifierData[8].also { this.VersionNumberByte2 = it.toUInt()  }
        this.identifierData[9].also { this.VersionNumberLSB = it.toUInt()  }
        this.identifierData[10].also { this.VersionBoardType = it.toUInt()  }

        this.identifierData[11].also { this.UpdateDownCount2 = it.toUInt() }
        this.identifierData[12].also { this.UpdateDownCount1 = it.toUInt()  }
        this.identifierData[13].also { this.UpdateDownCount0 = it.toUInt()  }

        this.identifierData[14].also { this.RegistrationID3 = it.toUInt()  }
        this.identifierData[15].also { this.RegistrationID2 = it.toUInt()  }
        this.identifierData[16].also { this.RegistrationID1 = it.toUInt()  }
        this.identifierData[17].also { this.RegistrationID0 = it.toUInt()  }

        this.identifierData[18].also { this.CRCHighByte = it.toUInt()  }
        this.identifierData[19].also { this.CRCLowByte = it.toUInt()  }

        val _versionNo = listOf(this.VersionNumberMSB, this.VersionNumberByte, this.VersionNumberByte2, this.VersionNumberLSB, this.VersionBoardType)

        Utilities.DeskBytes = listOf(this.DeskIDMSB.toUByte(),
            this.DeskIDByte3.toUByte(),
            this.DeskIDByte2.toUByte(),
            this.DeskIDLSB.toUByte())

        val serialArr: ByteArray = byteArrayOf(this.DeskIDMSB.toByte(),
                this.DeskIDByte3.toByte(),
                this.DeskIDByte2.toByte(),
                this.DeskIDLSB.toByte())

        val versionArr: ByteArray = byteArrayOf(this.VersionNumberMSB.toByte(),
                this.VersionNumberByte.toByte(),
                this.VersionNumberByte2.toByte(),
                this.VersionNumberLSB.toByte(),
                this.VersionBoardType.toByte())



        val upDownCountArr: ByteArray = byteArrayOf(this.UpdateDownCount2.toByte(),
                this.UpdateDownCount1.toByte(),
                this.UpdateDownCount0.toByte())

        val registrationIdArr: ByteArray = byteArrayOf(this.RegistrationID3.toByte(),
                this.RegistrationID2.toByte(),
                this.RegistrationID1.toByte(),
                this.RegistrationID0.toByte())


        println("serialArr: $serialArr")
        println("versionArr: ${Utilities.calculateCrcWithBytes((_versionNo.map { it.toByte()  }).toByteArray(), true)}")
        println("upDownCountArr: $upDownCountArr")
        println("registrationIdArr: $registrationIdArr")

        this.SerialNumber = "${Utilities.convertByteArrayToInt(serialArr)}"
        this.Version = "${Utilities.convertByteArrayToInt(versionArr)}"
        //this.UpDownCount = "${Utilities.convertByteArrayToInt(upDownCountArr)}"
        this.RegistrationID = "${Utilities.convertByteArrayToInt(registrationIdArr)}"

    }
}
