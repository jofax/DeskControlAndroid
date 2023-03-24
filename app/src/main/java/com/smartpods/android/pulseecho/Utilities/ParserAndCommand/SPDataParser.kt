package com.smartpods.android.pulseecho.Utilities.ParserAndCommand

import android.bluetooth.BluetoothGatt
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Interfaces.*
import com.smartpods.android.pulseecho.Model.PulseDataPush
import com.smartpods.android.pulseecho.Utilities.*
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleEventListener
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.ViewModel.DeskInfoViewModel
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.ref.WeakReference


class DataEventListener {
    var onCoreDataEventReceived: ((PulseCore) -> Unit)? = null
    var onAESKeyDataEventReceived: ((PulseAESKey) -> Unit)? = null
    var onAESIVDataEventReceived: ((PulseAESIV) -> Unit)? = null
    var onIdentifierDataEventReceived: ((PulseIdentifier) -> Unit)? = null
    var onReportDataEventReceived: ((PulseReport) -> Unit)? = null
    var onServerDataEventReceived: ((PulseServerData) -> Unit)? = null
    var onVerticalProfileDataEventReceived: ((ProfileObject) -> Unit)? = null
    var onDesktopAppHasPriority: ((Boolean) -> Unit)? = null
    var onResumeNormalBLEResponse: ((Boolean) -> Unit)? = null
    var onNewBlePairAttemptResponse: ((Boolean) -> Unit)? = null
}

object SPDataParser {

    private var listeners: MutableSet<WeakReference<DataEventListener>> = mutableSetOf()
    var desktopApphasPriority: Boolean = false
    val deskViewModel = DeskInfoViewModel()

    fun registerListener(listener: DataEventListener) {
        if (listeners.map { it.get() }.contains(listener)) { return }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
    }

    fun unregisterListener(listener: DataEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<DataEventListener>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
        }
    }

    fun initWithString(data: ByteArray) {

        if (data.count() > 0) else return
        val uBytePackets = data.map { it.toUByte() }

        val packetLength = uBytePackets[0].toInt()
        val packetHeader = uBytePackets[1].toInt()

        //println("packetLength | $packetLength | packetHeader | $packetHeader")

        if (packetLength == 20) else return
        if (Utilities.validCRC16(uBytePackets.toUByteArray().toByteArray())) {

            val rawPacketStr = data.toHex()
            //println("rawPacketStr | $rawPacketStr")
            if (rawPacketStr.contains(Constants.PairingDesktopPriorityResponse) ||
                rawPacketStr.contains(Constants.InvalidateCommandResponse) ||
                rawPacketStr.contains(Constants.DesktopAppPriorityResponse)) {
                Utilities.desktopApphasPriority = true
                Utilities.isDeskCurrentlyBooked = false

                listeners.forEach { it.get()?.onDesktopAppHasPriority?.invoke(true) }

            } else if (rawPacketStr.contains(Constants.NewBlePairAttemptResponse)) {
                Utilities.desktopApphasPriority = false
                Utilities.isDeskCurrentlyBooked = false

                listeners.forEach { it.get()?.onNewBlePairAttemptResponse?.invoke(true) }

            }  else if (rawPacketStr.contains(Constants.ResumeNormalBLEDataResponse)) {
                Utilities.desktopApphasPriority = false
                Utilities.isDeskCurrentlyBooked = false

                listeners.forEach { it.get()?.onResumeNormalBLEResponse?.invoke(true) }

            } else {
                when (packetHeader) {
                    1 -> {
                        val mCore = CoreObject(data)
                        //SPBleManager.sendHeartBeatWithDevice(Utilities.getSmartpodsDevice())
                        //println("core stream data: ${core.HeartBeatOut}")

                        if (mCore.HeartBeatOut == true) {
                            val device = Utilities.getSmartpodsDevice()
                            if (device.isConnected()) {
                                println("CORE OBJECT need heartbeat")
                                SPBleManager.sendHeartBeatWithDevice(Utilities.getSmartpodsDevice())
                            }
                        }
                        //listeners.forEach { it.get()?.onCoreDataEventReceived?.let { it1 -> it1(core) } }

                        //println("RunSwitch: ${mCore.RunSwitch}")
                        //println("UseInteractiveMode: ${(mCore.RunSwitch && mCore.UseInteractiveMode)}")
                        //println("Automatic: ${(mCore.RunSwitch && !mCore.UseInteractiveMode)}")

                        //println("DeskCurrentlyBooked: ${mCore.DeskCurrentlyBooked}")

                        Utilities.isDeskCurrentlyBooked = mCore.DeskCurrentlyBooked
                        Utilities.isDeskHasIncomingBooking = mCore.DeskUpcomingBooking
                        Utilities.isDeskEnabled = mCore.DeskEnabledStatus

                        if (Utilities.isDeskHasIncomingBooking) {
                            if (Utilities.bookingSchedulerCount < Utilities.bookingSchedulerLimit &&
                                Utilities.bookingSchedulerCount != Utilities.bookingSchedulerLimit) {
                                Utilities.bookingSchedulerCount += 1

                                print("booking scheduler timer exist")

                                runBlocking {
                                    deskViewModel.checkDeskBookingInformation()
                                }

                            }
                        } else {
                            Utilities.bookingSchedulerCount = 0
                        }

                        if (!mCore.RunSwitch) {
                            UserPreference.prefs.write("desk_mode", "Manual")
                        }

                        if (mCore.RunSwitch && mCore.UseInteractiveMode) {
                            UserPreference.prefs.write("desk_mode", "Interactive")
                        }

                        if (mCore.RunSwitch && !mCore.UseInteractiveMode) {
                            UserPreference.prefs.write("desk_mode", "Automatic")
                        }

                        listeners.forEach { it.get()?.onCoreDataEventReceived?.invoke(mCore) }
                    }
                    2 -> {
                        val mReport = ReportObject(data)
                        listeners.forEach { it.get()?.onReportDataEventReceived?.invoke(mReport) }
                    }
                    3 -> {
                        val midentifier = IdentifierObject(data)

                        UserPreference.prefs.write("SerialNumber", midentifier.SerialNumber)

                        listeners.forEach { it.get()?.onIdentifierDataEventReceived?.invoke(midentifier) }
                    }
                    4 -> {
                        val mProfile = ProfileObject(data)
                        listeners.forEach { it.get()?.onVerticalProfileDataEventReceived?.invoke(mProfile) }
                    }
                    5 -> {
                        val mServerData = ServerDataObject(data)
                        println("SPDataParser server data: ${mServerData.serverData}")
                        //listeners.forEach { it.get()?.onServerDataEventReceived?.invoke(mServerData) }
                        deskViewModel.pushDeskData(mServerData)
                    }
                    20 -> {
                        val mAesData = AESKeyObject(data)
                        println("mAesData:  ${mAesData.aesKey} | data : $data")
                        Utilities.savePushCredentials(mAesData.aesKey, "")

                    }
                    21 -> {
                        val mAesIVData = AESIVObject(data)
                        println("mAesIVData:  ${mAesIVData.aesIV} | data : $data")
                        Utilities.savePushCredentials("", mAesIVData.aesIV)
                    }
                    34,35 -> {
                        listeners.forEach { it.get()?.onDesktopAppHasPriority?.invoke(true) }
                    }
                    36 -> {
                        listeners.forEach { it.get()?.onResumeNormalBLEResponse?.invoke(true) }
                    }
                    else -> return
                }
            }



        }

    }

}
