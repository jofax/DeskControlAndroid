package com.smartpods.android.pulseecho.Utilities.ParserAndCommand

import com.smartpods.android.pulseecho.Constants.CommandType
import com.smartpods.android.pulseecho.Constants.ProfileSettingsType
import com.smartpods.android.pulseecho.Model.DeskBooking
import com.smartpods.android.pulseecho.Model.ProfileObject
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.byteArrayOfInts
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.toByteArray
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.HashMap

object  SPRequestParameters {
    val BLEHearbeatWithName: ByteArray = byteArrayOfInts(0x13, 0x11, 0x62, 0x06, 0x73, 0x6D, 0x61, 0x72, 0x74, 0x70, 0x6F, 0x64, 0x73, 0x32, 0x78)
    val BLEHeartbeatForeground: ByteArray = byteArrayOfInts(0x07, 0x11, 0x62, 0x00, 0x01, 0xFE, 0x4F)
    val BLEHeartbeatBackground: ByteArray = byteArrayOfInts(0x07, 0x11, 0x62, 0x00, 0x00, 0x44, 0xE2)
    val Report: ByteArray = byteArrayOfInts(0x06, 0x11, 0x62, 0x02, 0xB5, 0x50)
    val Profile: ByteArray = byteArrayOfInts(0x06, 0x11, 0x62, 0x03, 0x0F, 0xFD)
    val Information: ByteArray = byteArrayOfInts(0x06, 0x11, 0x62, 0x04, 0x5F, 0xE4)
    val All: ByteArray = byteArrayOfInts(0x06, 0x11, 0x62, 0x05, 0xE5, 0x49)
    val PacketByteArray = byteArrayOfInts(0x08, 0x10, 0x70, 0x69, 0x6E, 0x67, 0x74, 0xE3, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)

    //Automatic detection
    val LegacyDetection: ByteArray =  byteArrayOfInts(0x06, 0x11, 0x6E, 0x00, 0xAE, 0x4A)  //Revert to legacy presence detection
    val AutomaticDetection: ByteArray =  byteArrayOfInts(0x06, 0x11, 0x6E, 0x01, 0x14, 0xE7)  //Enable automated presence detection
    val CaptureAutomaticDetection: ByteArray = byteArrayOfInts(0x06, 0x11, 0x6E, 0x02, 0x61, 0xBD)  //Submit capture request in automated presence detection

    //AES KEY & IV
    val GetAESKey: ByteArray = byteArrayOfInts(0x06, 0x11, 0x62, 0x06, 0x90, 0x13)

    //TEST REgistration ID
    val testRegistrationID:ByteArray = byteArrayOfInts(0x08, 0x59, 0x0, 0x0, 0x0, 0x02, 0x7F, 0xAE)

    init {
        print("SPRequestParameters initialized")
    }
}

/**
 * kotlin.UByte: an unsigned 8-bit integer, ranges from 0 to 255
kotlin.UShort: an unsigned 16-bit integer, ranges from 0 to 65535
kotlin.UInt: an unsigned 32-bit integer, ranges from 0 to 2^32 - 1
kotlin.ULong: an unsigned 64-bit integer, ranges from 0 to 2^64 - 1
 *
 *
 * **/

interface CommandBuilder {
    fun GetSetHeightOffset(value: Double) : ByteArray
    fun GetEnableMotionCommand() : ByteArray
    fun GetDisableMotionCommand() : ByteArray
    fun GetStopCommand() : ByteArray
    fun GetMoveUpCommand() : ByteArray
    fun GetMoveDownCommand() : ByteArray
    fun GetMoveSittingCommand() : ByteArray
    fun GetMoveStandingCommand() : ByteArray
    fun GetSetDownCommand(value: Double) : ByteArray
    fun GetPresenceInverted() : ByteArray
    fun GetPresenceNoInverted() : ByteArray
    fun GetPresenceStandInverted() : ByteArray
    fun GetPresenceStandNoInverted() : ByteArray
    fun GetEnableSemiAutomaticMode() : ByteArray
    fun GetDisableSemiAutomaticMode() : ByteArray
    fun GetResetCommissionedOn() : ByteArray
    fun GetResetCommissionedOff() : ByteArray
    fun GetSerialNumber(value: String) : ByteArray
    fun GetAESKey(value: Double) : ByteArray
    fun GetCommitAESKey() : ByteArray
    fun GetRevertAESKey() : ByteArray
    fun GetAcknowkedgePendingMovement() : ByteArray
    fun GetAknowledgeSafetyCommand() : ByteArray
    fun GetSetCrushThreshold(value: Int) : ByteArray
    fun GetDeskTurnOn() : ByteArray
    fun GetDeskTurnOff() : ByteArray
    fun GetEnableSafetyCommand() : ByteArray
    fun GetDisableSafetyCommand() : ByteArray
    fun GetSetIndicatorLight(value: Int) : ByteArray
    fun GetSetTopCommand(value: Double) : ByteArray
    fun GetUserAuthenticatedOnCommand() : ByteArray
    fun GetUserAuthenticatedOffCommand() : ByteArray
    fun GetSetPNDThreshold(value: Int) : ByteArray
    fun GetSetPNDStandThreshold(value: Int) : ByteArray
    fun GetSetAwayAdjust(value: Int) : ByteArray
    fun GetCommitProfile() : ByteArray
    fun GetGridEyeOn() : ByteArray
    fun GetGridEyeOff() : ByteArray
    fun GetRowSelector(value: Double) : ByteArray
    fun GetColumnSelector(value: Double) : ByteArray
    fun GetResumeOutputCommand() : ByteArray
    fun GetStopOutputCommand() : ByteArray
    fun GetClearWatchdogAlarm() : ByteArray
    fun BLEHeartBeat() : ByteArray
    fun EnableHeartBeat() : ByteArray
    fun DisableHeartBeat() : ByteArray
    fun GenerateVerticalProfile(movements: List<HashMap<String, Int>>) : ByteArray
    fun CreateVerticalProfile(settings: ProfileObject) : ByteArray
    fun BLEHeartBeatIn(value: String) : ByteArray
    fun DeskBookingInfo(data: DeskBooking): ByteArray
}

interface CommandAdapterProtocol {
    fun buildPulseCommand(command: String, type: CommandType) : ByteArray
    fun buildPulseVerticalProfile(newMovements: List<HashMap<String, Int>>) : ByteArray
    fun buildPulseUserProfile(profile: ProfileObject) : ByteArray
    fun buildBookingCommand(booking: DeskBooking) : ByteArray
}

class PulseCommand: CommandAdapterProtocol {
    override fun buildPulseCommand(command: String, type: CommandType): ByteArray {
        val packetNumber = 1
        val packetTotal = 1
        val header = command[0].toString().toByteArray(Charsets.UTF_8)[0].toByte()
        println("pulse command header: $header")
        var payload: ByteArray = byteArrayOf()

        val fourbits = ((packetNumber.toString().padStart(4, '0')) + (packetTotal.toString().padStart(4, '0'))).toBigInteger(2)
        println("pulse command fourbits: $fourbits")

        if (command.count() > 1) {
            val _command = command.substring(1, command.count())
            when(type) {
                CommandType.ASCIIType -> {
                    payload = _command.toByteArray(Charsets.UTF_8)
                }
                CommandType.INTType -> {
                    payload = _command.toByteArray(Charsets.UTF_8)
                }
                CommandType.SHORTType -> {
                    val _commandByte = _command.toByteArray(Charsets.UTF_8)
                    for (item in _commandByte) {
                        println("byte item: $item")
                        payload += item.toByte()
                    }
                }
                CommandType.BYTEType -> {
                    payload += _command.toByte()
                }
                CommandType.BYTEHEXType -> {
                    payload += _command.toByte()
                }
                CommandType.BYTESHORTType -> {
                    val _commandByte = Utilities.asByteArray(_command.toShort()).toUByteArray().toByteArray()
                    for (item in _commandByte) {
                        println("byte item: $item")
                        payload += item.toByte()
                    }
                }
            }
        }

        var packet: ByteArray = byteArrayOf(fourbits.toByte().toByte(), header)
        packet += (payload)

        val packetLength = 3 + packet.count()

        var initialPacket: ByteArray  = byteArrayOf(packetLength.toByte())
        initialPacket += packet

        var finalPacketBytes: List<UByte> = listOf()
        finalPacketBytes += initialPacket.map { it.toUByte() }

        val crc16 = Utilities.calculatCrc16FromArray(initialPacket.toUByteArray(), 0xFFFF.toUShort())
        val byteConverted = Utilities.asByteArray(crc16.toShort())
        finalPacketBytes += byteConverted

        var byteArrayPackets: ByteArray = finalPacketBytes.map { it.toByte() }.toByteArray()

        println("pulse command packet structure: ${byteArrayPackets.toString(Charsets.UTF_8)}")

        return byteArrayPackets
    }

    override fun buildPulseVerticalProfile(newMovements: List<HashMap<String, Int>>): ByteArray {
        val packetNumber = 1
        val packetTotal = 1

        println("buildPulseVerticalProfile newMovements: $newMovements")

        val header = "P".toByteArray(Charsets.UTF_8)[0]

        var payload: ByteArray = byteArrayOfInts()
        val fourbits = ((packetNumber.toString().padStart(4, '0')) + (packetTotal.toString().padStart(4, '0'))).toBigInteger(2)

        for(movement in newMovements) {

            val moveValue = movement["raw"]
            val moveType = movement["key"]?.toBigInteger()

            println("moveValue : $moveValue")
            println("moveType : $moveType")

            if (moveType != null) {
                payload += moveType.toByte()
            }

            if (moveValue == 3 || moveValue == 0) {
                payload += 0.toByte()
                payload += 3.toByte()
            } else {
                //println("move data to bytearray : ${moveValue?.let { byteArrayOf(it.toByte()) }}")
                val movementData = moveValue?.toByteArray()?.filter { (it.toInt() != 0)}
                if (movementData != null) {
                    println("movementData : $movementData")
                    payload += movementData
                }


            }

        }

        var packet: ByteArray = byteArrayOf(fourbits.toByte().toByte(), header)
        packet += (payload)

        val packetLength = 3 + packet.count()

        var initialPacket: ByteArray  = byteArrayOf(packetLength.toByte())
        initialPacket += packet

        println("buildPulseVerticalProfile initialPacket : ${ String(initialPacket)}")

        var finalPacketBytes: List<UByte> = listOf()
        finalPacketBytes += initialPacket.map { it.toUByte() }

        val crc16 = Utilities.calculatCrc16FromArray(initialPacket.toUByteArray(), 0xFFFF.toUShort())
        val byteConverted = Utilities.asByteArray(crc16.toShort())
        finalPacketBytes += byteConverted

        var byteArrayPackets: ByteArray = finalPacketBytes.map { it.toByte() }.toByteArray()

        var packetsToInteArray = byteArrayPackets.map { it.toInt() }

        println("buildPulseVerticalProfile packetsToInteArray: $packetsToInteArray")
        println("buildPulseVerticalProfile packet: ${ String(byteArrayPackets)}")

        return byteArrayPackets
    }

    override fun buildPulseUserProfile(profile: ProfileObject) : ByteArray {
        val packetNumber = 1
        val packetTotal = 1
        println("buildPulseUserProfile profile: $profile")
        val header = "P".toByteArray(Charsets.UTF_8)[0]
        var payload: ByteArray = byteArrayOfInts()
        val fourbits = ((packetNumber.toString().padStart(4, '0')) + (packetTotal.toString().padStart(4, '0'))).toBigInteger(2)

        val selectedProfileSettingsType = ProfileSettingsType(profile.ProfileSettingType)
        println("viewModel.selectedProfileSettingsType : $selectedProfileSettingsType")

        when(selectedProfileSettingsType) {
            ProfileSettingsType.ACTIVE, ProfileSettingsType.MODERATELYACTIVE -> {
                payload += 7.toByte()
                payload += 0.toByte()
                payload += 3.toByte()

                val timeDiff = (60 - profile.StandingTime1) * 60
                payload += 4.toByte()
                val movementData = timeDiff?.toByteArray()?.filter { (it.toInt() != 0)}
                if (movementData != null) {
                    println("ProfileSettingsType.ACTIVE / MODERATELYACTIVE movementData : $movementData")
                    payload += movementData
                }
            }

            ProfileSettingsType.VERYACTIVE -> {
                payload += 7.toByte()
                payload += 0.toByte()
                payload += 3.toByte()

                val timeDiff1 = (30 - profile.StandingTime1) * 60
                payload += 4.toByte()
                val movementData1 = timeDiff1?.toByteArray()?.filter { (it.toInt() != 0)}
                if (movementData1 != null) {
                    println("ProfileSettingsType.VERYACTIVE movementData1 : $movementData1")
                    payload += movementData1
                }

                payload += 7.toByte()
                val movementData2 = 1800.toByteArray()?.filter { (it.toInt() != 0)}
                if (movementData2 != null) {
                    println("ProfileSettingsType.VERYACTIVE movementData2 : $movementData2")
                    payload += movementData2
                }

                val timeDiff2 = (60 - profile.StandingTime2) * 60
                payload += 4.toByte()
                val movementData3 = timeDiff2?.toByteArray()?.filter { (it.toInt() != 0)}
                if (movementData3 != null) {
                    println("ProfileSettingsType.VERYACTIVE movementData3 : $movementData3")
                    payload += movementData3
                }
            }

            ProfileSettingsType.CUSTOM -> {
                if (profile.StandingTime2 != 0) {
                    // custom 30 / 30
                    payload += 7.toByte()
                    payload += 0.toByte()
                    payload += 3.toByte()

                    val timeDiff1 = (30 - profile.StandingTime1) * 60
                    payload += 4.toByte()
                    val movementData1 = timeDiff1?.toByteArray()?.filter { (it.toInt() != 0)}
                    if (movementData1 != null) {
                        println("ProfileSettingsType.VERYACTIVE movementData1 : $movementData1")
                        payload += movementData1
                    }

                    payload += 7.toByte()
                    val movementData2 = 1800.toByteArray()?.filter { (it.toInt() != 0)}
                    if (movementData2 != null) {
                        println("ProfileSettingsType.VERYACTIVE movementData2 : $movementData2")
                        payload += movementData2
                    }

                    val timeDiff2 = (60 - profile.StandingTime2) * 60
                    payload += 4.toByte()
                    val movementData3 = timeDiff2?.toByteArray()?.filter { (it.toInt() != 0)}
                    if (movementData3 != null) {
                        println("ProfileSettingsType.VERYACTIVE movementData3 : $movementData3")
                        payload += movementData3
                    }
                } else {
                    // custom 60
                    payload += 7.toByte()
                    payload += 0.toByte()
                    payload += 3.toByte()

                    val timeDiff = (60 - profile.StandingTime1) * 60
                    payload += 4.toByte()
                    val movementData = timeDiff?.toByteArray()?.filter { (it.toInt() != 0)}
                    if (movementData != null) {
                        println("ProfileSettingsType.ACTIVE / MODERATELYACTIVE movementData : $movementData")
                        payload += movementData
                    }
                }
            }
        }

        var packet: ByteArray = byteArrayOf(fourbits.toByte(), header)
        packet += (payload)

        val packetLength = 3 + packet.count()

        var initialPacket: ByteArray  = byteArrayOf(packetLength.toByte())
        initialPacket += packet

        println("buildPulseUserProfile initialPacket : ${ String(initialPacket)}")

        var finalPacketBytes: List<UByte> = listOf()
        finalPacketBytes += initialPacket.map { it.toUByte() }

        val crc16 = Utilities.calculatCrc16FromArray(initialPacket.toUByteArray(), 0xFFFF.toUShort())
        val byteConverted = Utilities.asByteArray(crc16.toShort())
        finalPacketBytes += byteConverted

        var byteArrayPackets: ByteArray = finalPacketBytes.map { it.toByte() }.toByteArray()

        var packetsToInteArray = byteArrayPackets.map { it.toInt() }

        println("buildPulseUserProfile packetsToInteArray: $packetsToInteArray")
        println("buildPulseUserProfile packet: ${ String(byteArrayPackets)}")

        return byteArrayPackets

    }

    override fun buildBookingCommand(booking: DeskBooking): ByteArray {
        println("buildBookingCommand profile: $booking")
        var finalPacketBytes: List<UByte> = listOf()

        val _isEnabled = if (booking.IsEnabled) 1 else 0
        val _isLoggedin = if (booking.IsLoggedIn) 1 else 0
        val _IsHotelingStateEnabled = if (booking.IsHotelingStateEnabled) 1 else 0
        val _bookingId = if (booking.BookingId != 0) 1 else 0

        finalPacketBytes += 10.toUByte()
        finalPacketBytes += 17.toUByte()
        finalPacketBytes += 96.toUByte()

        finalPacketBytes += _isEnabled.toUByte()
        finalPacketBytes += _bookingId.toUByte()
        finalPacketBytes += _isLoggedin.toUByte()

        if (booking.BookingId != 0) {
            val mBooking = Utilities.getBookingTime(booking.BookingDate,
                booking.Periods,
                booking.TzOffset)
            val startTime = mBooking["BookFrom"]
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val utcTime = LocalDateTime.parse(booking.UtcDateTime, formatter)
            val timeOfBooking = utcTime.plusMinutes(15L)
            val totalSeconds =  timeOfBooking.second.minus(utcTime.second)

            val byteConverted = Utilities.asByteArray(totalSeconds.toShort())
            finalPacketBytes += byteConverted

        } else {
            finalPacketBytes += 0.toUByte()
            finalPacketBytes += 0.toUByte()
        }

        finalPacketBytes += _IsHotelingStateEnabled.toUByte()

        val crcPacket = Utilities.calculateCrcWithBytes(finalPacketBytes.map { it.toByte() }.toByteArray(), true).toByteArray().map { it.toUByte()}
        finalPacketBytes += crcPacket

        return finalPacketBytes.map { it.toByte() }.toByteArray()
    }

}

class PulseCommands: CommandBuilder {

    private val pulseCommand = PulseCommand()

    override fun GetSetHeightOffset(value: Double): ByteArray {
        val mValue = "%.0f".format(value)
        val mCommand = "a$mValue"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.SHORTType)
    }

    override fun GetEnableMotionCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("B1", CommandType.SHORTType)
    }

    override fun GetDisableMotionCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("B0", CommandType.SHORTType)
    }

    override fun GetStopCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("C1", CommandType.SHORTType)
    }

    override fun GetMoveUpCommand(): ByteArray  {
        return pulseCommand.buildPulseCommand("C6", CommandType.SHORTType)
    }

    override fun GetMoveDownCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("C5", CommandType.SHORTType)
    }

    override fun GetMoveSittingCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("Cd", CommandType.SHORTType)
    }

    override fun GetMoveStandingCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("Cu", CommandType.SHORTType)
    }

    override fun GetSetDownCommand(value: Double): ByteArray {
        val mValue = "%.0f".format(value)
        val mCommand = "D$mValue"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTESHORTType)
    }

    override fun GetPresenceInverted(): ByteArray {
        return pulseCommand.buildPulseCommand("E1", CommandType.SHORTType)
    }

    override fun GetPresenceNoInverted(): ByteArray {
        return pulseCommand.buildPulseCommand("E0", CommandType.SHORTType)
    }

    override fun GetPresenceStandInverted(): ByteArray {
        return pulseCommand.buildPulseCommand("F1", CommandType.SHORTType)
    }

    override fun GetPresenceStandNoInverted(): ByteArray {
        return pulseCommand.buildPulseCommand("F0", CommandType.SHORTType)
    }

    override fun GetEnableSemiAutomaticMode(): ByteArray {
        return pulseCommand.buildPulseCommand("G1", CommandType.BYTEType)
    }

    override fun GetDisableSemiAutomaticMode(): ByteArray {
        return pulseCommand.buildPulseCommand("G0", CommandType.BYTEType)
    }

    override fun GetResetCommissionedOn(): ByteArray {
        return pulseCommand.buildPulseCommand("H1", CommandType.SHORTType)
    }

    override fun GetResetCommissionedOff(): ByteArray {
        return pulseCommand.buildPulseCommand("H0", CommandType.SHORTType)
    }

    override fun GetSerialNumber(value: String): ByteArray {
        val mCommand = "I$value"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.INTType)
    }

    override fun GetAESKey(value: Double): ByteArray {
        val mValue = "%.0f".format(value)
        val mCommand = "k$mValue"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTEType)
    }

    override fun GetCommitAESKey(): ByteArray {
        return pulseCommand.buildPulseCommand("K1", CommandType.SHORTType)
    }

    override fun GetRevertAESKey(): ByteArray {
        return pulseCommand.buildPulseCommand("K0", CommandType.SHORTType)
    }

    override fun GetAcknowkedgePendingMovement(): ByteArray {
        return pulseCommand.buildPulseCommand("l", CommandType.BYTEType)
    }

    override fun GetAknowledgeSafetyCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("L", CommandType.BYTEType)
    }

    override fun GetSetCrushThreshold(value: Int): ByteArray {
        val mCommand = "m$value"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTEType)
    }

    override fun GetDeskTurnOn(): ByteArray {
        return pulseCommand.buildPulseCommand("o1", CommandType.BYTEType)
    }

    override fun GetDeskTurnOff(): ByteArray {
        return pulseCommand.buildPulseCommand("o0", CommandType.BYTEType)
    }

    override fun GetEnableSafetyCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("S1", CommandType.SHORTType)
    }

    override fun GetDisableSafetyCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("S0", CommandType.SHORTType)
    }

    override fun GetSetIndicatorLight(value: Int): ByteArray {
        val mCommand = "t$value"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTEType)
    }

    override fun GetSetTopCommand(value: Double): ByteArray {
        val mValue = "%.0f".format(value)
        val mCommand = "U$mValue"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTESHORTType)
    }

    override fun GetUserAuthenticatedOnCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("W1", CommandType.SHORTType)
    }

    override fun GetUserAuthenticatedOffCommand(): ByteArray {
        return pulseCommand.buildPulseCommand("W0", CommandType.SHORTType)
    }

    override fun GetSetPNDThreshold(value: Int): ByteArray {
        val mCommand = "x$value"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTEType)
    }

    override fun GetSetPNDStandThreshold(value: Int): ByteArray {
        val mCommand = "v$value"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTEType)
    }

    override fun GetSetAwayAdjust(value: Int): ByteArray {
        val mCommand = "T$value"
        return pulseCommand.buildPulseCommand(mCommand, CommandType.BYTEType)
    }

    override fun GetCommitProfile(): ByteArray {
        return pulseCommand.buildPulseCommand("X", CommandType.BYTEType)
    }

    override fun GetGridEyeOn(): ByteArray {
        return pulseCommand.buildPulseCommand("V1", CommandType.BYTEType)
    }

    override fun GetGridEyeOff(): ByteArray {
        return pulseCommand.buildPulseCommand("V0", CommandType.BYTEType)
    }

    override fun GetRowSelector(value: Double): ByteArray {
        TODO("Not yet implemented")
    }

    override fun GetColumnSelector(value: Double): ByteArray {
        TODO("Not yet implemented")
    }

    override fun GetResumeOutputCommand(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun GetStopOutputCommand(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun GetClearWatchdogAlarm(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun BLEHeartBeat(): ByteArray {
        return pulseCommand.buildPulseCommand("b0", CommandType.SHORTType)
    }

    override fun EnableHeartBeat(): ByteArray {
        return pulseCommand.buildPulseCommand("z0", CommandType.SHORTType)
    }

    override fun DisableHeartBeat(): ByteArray {
        return pulseCommand.buildPulseCommand("z1", CommandType.SHORTType)
    }

    override fun GenerateVerticalProfile(movements: List<HashMap<String, Int>>): ByteArray {
        return pulseCommand.buildPulseVerticalProfile(movements)
    }

    override fun CreateVerticalProfile(settings: ProfileObject): ByteArray {
        return pulseCommand.buildPulseUserProfile(settings)
    }

    override fun BLEHeartBeatIn(value: String): ByteArray {
        TODO("Not yet implemented")
    }

    override fun DeskBookingInfo(data: DeskBooking): ByteArray {
        return pulseCommand.buildBookingCommand(data)
    }
}

private fun bigIntToByteArray(i: Int): ByteArray? {
    val bigInt: BigInteger = BigInteger.valueOf(i.toLong())
    return bigInt.toByteArray()
}

