package com.smartpods.android.pulseecho.Interfaces

import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlin.properties.Delegates

interface PulseServerData {
    var Length: Int
    var Command: Int
    var PushFrequency:  UByte
    var SitTimeStorageHB: UByte
    var SitTimeStorageLB: UByte
    var StandTimeStorageHB: UByte
    var StandTimeStorageLB: UByte
    var AwayTimeStorageHB: UByte
    var AwayTimeStorageLB: UByte
    var AutomaticTimeStorageHB: UByte
    var AutomaticTimeStorageLB: UByte
    var InteractiveTimeStorageHB: UByte
    var InteractiveTimeStorageLB: UByte
    var ManualTimeStorageHB: UByte
    var ManualTimeStorageLB: UByte
    var SafetyTriggerTallyStorage: UByte
    var UpDownTallyStorage: UByte
    var CurrentDeskAvailability: UByte
    var CRCHighByte: UByte
    var CRCLowByte: UByte

}

data class ServerDataObject(private val data: ByteArray) : PulseServerData {
    var serverDataBytes = data.map { it.toUByte() }
    var serverData : List<UByte> by Delegates.notNull()

    override var Length: Int = 0
    override var Command: Int  = 0
    override var PushFrequency:  UByte  = 0u
    override var SitTimeStorageHB: UByte  = 0u
    override var SitTimeStorageLB: UByte  = 0u
    override var StandTimeStorageHB: UByte  = 0u
    override var StandTimeStorageLB: UByte  = 0u
    override var AwayTimeStorageHB: UByte  = 0u
    override var AwayTimeStorageLB: UByte  = 0u
    override var AutomaticTimeStorageHB: UByte  = 0u
    override var AutomaticTimeStorageLB: UByte  = 0u
    override var InteractiveTimeStorageHB: UByte  = 0u
    override var InteractiveTimeStorageLB: UByte = 0u
    override var ManualTimeStorageHB: UByte = 0u
    override var ManualTimeStorageLB: UByte = 0u
    override var SafetyTriggerTallyStorage: UByte = 0u
    override var UpDownTallyStorage: UByte = 0u
    override var CurrentDeskAvailability: UByte = 0u
    override var CRCHighByte: UByte = 0u
    override var CRCLowByte: UByte = 0u

    var SitTimeStorage: Int = 0
    var StandTimeStorage: Int = 0
    var AwayTimeStorage: Int = 0
    var AutomaticTimeStorage: Int = 0
    var InteractiveTimeStorage: Int = 0
    var ManualTimeStorage: Int = 0

    init {
        this.serverData = this.serverDataBytes

        this.serverData[0].also { this.Length = it.toInt() }
        this.serverData[1].also { this.Command = it.toInt() }

        this.serverData[2].also { this.PushFrequency = it }
        this.serverData[3].also { this.SitTimeStorageHB = it }
        this.serverData[4].also { this.SitTimeStorageLB = it }
        this.serverData[5].also { this.StandTimeStorageHB = it }
        this.serverData[6].also { this.StandTimeStorageLB = it}
        this.serverData[7].also { this.AwayTimeStorageHB = it }
        this.serverData[8].also { this.AwayTimeStorageLB = it}
        this.serverData[9].also { this.AutomaticTimeStorageHB = it }
        this.serverData[10].also { this.AutomaticTimeStorageLB = it}

        this.serverData[11].also { this.InteractiveTimeStorageHB = it }
        this.serverData[12].also { this.InteractiveTimeStorageLB = it }
        this.serverData[13].also { this.ManualTimeStorageHB = it }
        this.serverData[14].also { this.ManualTimeStorageLB = it }
        this.serverData[15].also { this.SafetyTriggerTallyStorage = it }
        this.serverData[16].also { this.UpDownTallyStorage = it }
        this.serverData[17].also { this.CurrentDeskAvailability = it }
        this.serverData[18].also { this.CRCHighByte = it }
        this.serverData[19].also { this.CRCLowByte = it }

        SitTimeStorage = Utilities.getCombineByte(this.SitTimeStorageHB, this.SitTimeStorageLB)
        StandTimeStorage = Utilities.getCombineByte(this.StandTimeStorageHB, this.StandTimeStorageLB)
        AwayTimeStorage = Utilities.getCombineByte(this.AwayTimeStorageHB, this.AwayTimeStorageLB)
        AutomaticTimeStorage = Utilities.getCombineByte(this.AutomaticTimeStorageHB, this.AutomaticTimeStorageLB)
        InteractiveTimeStorage = Utilities.getCombineByte(this.InteractiveTimeStorageHB, this.InteractiveTimeStorageLB)
        ManualTimeStorage = Utilities.getCombineByte(this.ManualTimeStorageHB, this.ManualTimeStorageLB)

    }
}