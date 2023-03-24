package com.smartpods.android.pulseecho.Interfaces

interface PulseCore {
    var Length: Int
    var Command: Int
    var ReportedVerPosHB: UInt
    var ReportedVerPosLB: UInt
    var MainTimerCycleSecondsHB: UInt
    var MainTimerCycleSecondsLB: UInt
    var MovesreportedVertPos: Int
    var CondensedStatusOne: Int
    var CondensedStatusTwo: Int
    var CondensedEnableOne: Int
    var CondensedEnableTwo: Int
    var WifiSyncStatus: UInt
    var PendingMoveAndSlider: UInt
    var SliderComboTwo: UInt
    var SliderComboThree: UInt
    var CondensedStatusThree: Int
    var CRCHighByte: UInt
    var CRCLowByte: UInt
    /** END BLE STRINGS **/

    var ReportedVertPos: Int
    var MainTimerCycleSeconds: Int
    var TimesreportedVertPos: Int
    var NextMove: Int

    var WifiStatus: Int
    var AdapterError: Int

    var PendingMove: Int
    var LEDSlider: Int

    var SitPresence: Int
    var StandPresence: Int

    var AwaySlider: Int
    var SafetySlider: Int

    var RunSwitch: Boolean
    var SafetyStatus: Boolean
    var AwayStatus: Boolean
    var Movingdownstatus: Boolean
    var Movingupstatus: Boolean
    var HeightSensorStatus: Boolean
    var AlternateCalibrationMode: Boolean
    var AlternateAITBMode: Boolean

    var NewInfoData: Boolean
    var NewProfileData:  Boolean

    var EnableTwoStageCalibration: Boolean
    var EnableHeatSenseFlipSitting: Boolean
    var EnableHeatSenseFlipStanding: Boolean
    var EnableMotionDetection: Boolean
    var EnableTiMotionBus: Boolean
    var EnableSafety: Boolean
    var UserAuthenticated: Boolean
    var UseInteractiveMode: Boolean

    var AppRxFailFlag: Boolean
    var WifiTxpushFailure: Boolean
    var WeAreSitting: Boolean
    var ObstructionStatus: Boolean
    var CommissioningFlag: Boolean
    var HeartBeatOut: Boolean

    var LastWifiPushFailed: Boolean
    var LastCommandFailed: Boolean
    var WifiPushIncoming: Boolean
    var DeskAtSittingPosition: Boolean

    var ObstructionDetection: Boolean
    var WifiSystem: Boolean

    var AutoPresenceDetection: Boolean
    var NeedPresenceCapture: Boolean
    var StandHeightAdjusted: Boolean
    var SitHeightAdjusted: Boolean

    var DeskCurrentlyBooked: Boolean
    var DeskUpcomingBooking: Boolean
    var DeskEnabledStatus: Boolean
}
