package com.smartpods.android.pulseecho.Interfaces
import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlin.properties.Delegates

data class CoreObject(private val data: ByteArray) : PulseCore {
    var coreDataBytes = data.map { it.toUByte() }
    var coreData : List<UByte> by Delegates.notNull()
    override var Length: Int = 0
    override var Command: Int = 0
    override var ReportedVerPosHB: UInt = 0u
    override var ReportedVerPosLB: UInt = 0u
    override var MainTimerCycleSecondsHB: UInt = 0u
    override var MainTimerCycleSecondsLB: UInt = 0u
    override var MovesreportedVertPos: Int = 0
    override var CondensedStatusOne: Int = 0
    override var CondensedStatusTwo: Int = 0
    override var CondensedEnableOne: Int = 0
    override var CondensedEnableTwo: Int = 0
    override var WifiSyncStatus: UInt = 0u
    override var PendingMoveAndSlider: UInt = 0u
    override var SliderComboTwo: UInt  = 0u
    override var SliderComboThree: UInt = 0u
    override var CondensedStatusThree: Int = 0
    override var CRCHighByte: UInt = 0u
    override var CRCLowByte: UInt = 0u
    /** END BLE STRINGS **/

    override var ReportedVertPos: Int = 0
    override var MainTimerCycleSeconds: Int = 0
    override var TimesreportedVertPos: Int = 0
    override var NextMove: Int = 0

    override var WifiStatus: Int = 0
    override var AdapterError: Int = 0

    override var PendingMove: Int = 0
    override var LEDSlider: Int = 0

    override var SitPresence: Int = 0
    override var StandPresence: Int = 0

    override var AwaySlider: Int = 0
    override var SafetySlider: Int = 0

    override var RunSwitch: Boolean = false
    override var SafetyStatus: Boolean = false
    override var AwayStatus: Boolean = false
    override var Movingdownstatus: Boolean = false
    override var Movingupstatus: Boolean = false
    override var HeightSensorStatus: Boolean = false
    override var AlternateCalibrationMode: Boolean = false
    override var AlternateAITBMode: Boolean = false

    override var NewInfoData: Boolean = false
    override var NewProfileData:  Boolean = false

    override var EnableTwoStageCalibration: Boolean = false
    override var EnableHeatSenseFlipSitting: Boolean = false
    override var EnableHeatSenseFlipStanding: Boolean = false
    override var EnableMotionDetection: Boolean = false
    override var EnableTiMotionBus: Boolean = false
    override var EnableSafety: Boolean = false
    override var UserAuthenticated: Boolean = false
    override var UseInteractiveMode: Boolean = false

    override var AppRxFailFlag: Boolean = false
    override var WifiTxpushFailure: Boolean = false
    override var WeAreSitting: Boolean = false
    override var ObstructionStatus: Boolean = false
    override var CommissioningFlag: Boolean = false
    override var HeartBeatOut: Boolean = false

    override var LastWifiPushFailed: Boolean = false
    override var LastCommandFailed: Boolean = false
    override var WifiPushIncoming: Boolean = false
    override var DeskAtSittingPosition: Boolean = false

    override var ObstructionDetection: Boolean = false
    override var WifiSystem: Boolean = false

    override var AutoPresenceDetection: Boolean = false
    override var NeedPresenceCapture: Boolean = false
    override var StandHeightAdjusted: Boolean = false
    override var SitHeightAdjusted: Boolean = false

    override var DeskCurrentlyBooked: Boolean = false
    override var DeskUpcomingBooking: Boolean = false
    override var DeskEnabledStatus: Boolean = false

    init {
        this.coreData = coreDataBytes
        this.coreData[0].also { this.Length = it.toInt() }
        this.coreData[1].also { this.Command = it.toInt() }
        this.coreData[2].also { this.ReportedVerPosHB = it.toUInt() }
        this.coreData[3].also { this.ReportedVerPosLB = it.toUInt()  }
        this.coreData[4].also { this.MainTimerCycleSecondsHB = it.toUInt() }
        this.coreData[5].also { this.MainTimerCycleSecondsLB = it.toUInt() }
        this.coreData[6].also { this.MovesreportedVertPos = it.toInt() }
        this.coreData[7].also { this.CondensedStatusOne = it.toInt() }
        this.coreData[8].also { this.CondensedStatusTwo = it.toInt() }
        this.coreData[9].also { this.CondensedEnableOne = it.toInt() }
        this.coreData[10].also { this.CondensedEnableTwo = it.toInt() }
        this.coreData[11].also { this.WifiSyncStatus = it.toUInt() }
        this.coreData[12].also { this.PendingMoveAndSlider = it.toUInt() }
        this.coreData[13].also { this.SliderComboTwo = it.toUInt() }
        this.coreData[14].also { this.SliderComboThree = it.toUInt() }
        this.coreData[15].also { this.CondensedStatusThree = it.toInt() }

        this.coreData[18].also { this.CRCHighByte = it.toUInt() }
        this.coreData[19].also { this.CRCLowByte = it.toUInt() }

        this.ReportedVertPos = Utilities.getCombineByte(this.ReportedVerPosHB.toUByte(), this.ReportedVerPosLB.toUByte())
        this.MainTimerCycleSeconds = Utilities.getCombineByte(this.MainTimerCycleSecondsHB.toUByte(), this.MainTimerCycleSecondsLB.toUByte())

        val moveCode = (this.MovesreportedVertPos and 0x0F)
        val moveType = this.MovesreportedVertPos.shr(4)

        this.TimesreportedVertPos = Utilities.getTimeCodes(moveCode)
        this.NextMove = moveType

        this.PendingMove = this.PendingMoveAndSlider.toInt().shr(4)
        this.LEDSlider = (this.PendingMoveAndSlider.toInt() and 0x0F)
        this.SitPresence = this.SliderComboTwo.toInt().shr(4)
        this.StandPresence = (this.SliderComboTwo.toInt() and 0x0F)
        this.AwaySlider = this.SliderComboThree.toInt().shr(4)
        this.SafetySlider = (this.SliderComboThree.toInt() and 0x0F)

        this.RunSwitch = Utilities.convertBitField(this.CondensedStatusOne,0)
        this.SafetyStatus =  Utilities.convertBitField(this.CondensedStatusOne, 1)
        this.AwayStatus =  Utilities.convertBitField(this.CondensedStatusOne, 2)
        this.Movingupstatus =  Utilities.convertBitField(this.CondensedStatusOne, 3)
        this.Movingdownstatus =  Utilities.convertBitField(this.CondensedStatusOne, 4)
        this.HeightSensorStatus =  Utilities.convertBitField(this.CondensedStatusOne, 5)
        this.AlternateCalibrationMode =  Utilities.convertBitField(this.CondensedStatusOne, 6)
        this.AlternateAITBMode =  Utilities.convertBitField(this.CondensedStatusOne, 7)

        this.DeskEnabledStatus = Utilities.convertBitField(this.CondensedStatusThree, 0)
        this.DeskCurrentlyBooked = Utilities.convertBitField(this.CondensedStatusThree, 1)
        this.DeskUpcomingBooking = Utilities.convertBitField(this.CondensedStatusThree,  2)

        this.StandHeightAdjusted = Utilities.convertBitField(this.CondensedStatusThree, 3)
        this.AutoPresenceDetection = Utilities.convertBitField(this.CondensedStatusThree, 4)
        this.NeedPresenceCapture = Utilities.convertBitField(this.CondensedStatusThree,  5)
        this.NewInfoData = Utilities.convertBitField(this.CondensedStatusThree, 6)
        this.NewProfileData = Utilities.convertBitField(this.CondensedStatusThree, 7)

        this.EnableTwoStageCalibration = Utilities.convertBitField(this.CondensedEnableOne, 0)
        this.EnableHeatSenseFlipSitting = Utilities.convertBitField(this.CondensedEnableOne,  1)
        this.EnableHeatSenseFlipStanding = Utilities.convertBitField(this.CondensedEnableOne,  2)
        this.EnableMotionDetection = Utilities.convertBitField(this.CondensedEnableOne,  3)
        this.EnableTiMotionBus = Utilities.convertBitField(this.CondensedEnableOne,  4)
        this.EnableSafety = Utilities.convertBitField(this.CondensedEnableOne,  5)
        this.UserAuthenticated = Utilities.convertBitField(this.CondensedEnableOne,  6)
        this.UseInteractiveMode = Utilities.convertBitField(this.CondensedEnableOne,  7)

        this.SitHeightAdjusted = Utilities.convertBitField(this.CondensedStatusTwo,  0)
        this.LastWifiPushFailed = Utilities.convertBitField(this.CondensedStatusTwo,  1)
        this.LastCommandFailed = Utilities.convertBitField(this.CondensedStatusTwo,  2)
        this.WifiPushIncoming = Utilities.convertBitField(this.CondensedStatusTwo,  3)
        this.DeskAtSittingPosition = Utilities.convertBitField(this.CondensedStatusTwo,  4)
        this.ObstructionStatus = Utilities.convertBitField(this.CondensedStatusTwo,   5)
        this.CommissioningFlag = Utilities.convertBitField(this.CondensedStatusTwo,  6)
        this.HeartBeatOut = Utilities.convertBitField(this.CondensedStatusTwo,   7)

        Utilities.isCalibrationOn = AlternateCalibrationMode
    }
}