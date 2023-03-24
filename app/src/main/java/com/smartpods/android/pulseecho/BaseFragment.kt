package com.smartpods.android.pulseecho
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.smartpods.android.pulseecho.Model.GenericResponseFromJSON
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleEventListener
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.PulseCommands
import com.smartpods.android.pulseecho.Utilities.hide
import com.smartpods.android.pulseecho.Utilities.show
import info.androidhive.fontawesome.FontDrawable
import kotlinx.android.synthetic.main.sp_with_menu_action_bar.*

abstract class BaseFragment: Fragment()  {

    var deviceConnected: Boolean = false
    var command = PulseCommands()
    lateinit var pulseMainActivity: BaseActivity

    private val connectionEventListener by lazy {
        SPBleEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                //println("HomeFragment onConnectionSetupComplete bond state: " + gatt.device.bondState)

            }
            onDisconnect = {
                //println("HomeFragment onDisconnect: " + it)
            }

            onCharacteristicWrite = { device, gattChar ->
                //println("HomeFragment onCharacteristicWrite: " + device)
                //println("HomeFragment onCharacteristicWrite: " + gattChar.value.toHexString())
            }

            onCharacteristicRead = { device, desc ->
                //println("HomeFragment onCharacteristicRead: " + device)
                //println("HomeFragment onCharacteristicRead: " + desc)
            }

            onCharacteristicChanged = { device, gattChar ->
                //println("HomeFragment onCharacteristicChanged: " + device)
                //println("HomeFragment onCharacteristicChanged: " + gattChar.value.toHexString())
            }

            onBondComplete =  { gatt, obj ->
                //println("HomeFragment onBondComplete: " + gatt)
                //println("HomeFragment onBondComplete: " + obj)
            }

            onPairedComplete =  { gatt ->
                //println("HomeFragment onPairedComplete: " + gatt)
            }

            onDeviceConnectivityError =  { device, message ->

                //println("HomeFragment onDeviceConnectivityError: Device - {$device} Message - {$message}")
            }

            onDeviceDataError =  { device, message ->
                //println("HomeFragment onDeviceDataError: Device - {$device} Message - {$message}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.base_fragment, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        SPBleManager.registerListener(connectionEventListener)
        pulseMainActivity = activity as BaseActivity
    }

    fun hasBackButton(show: Boolean) {
        if (show) {
            btnBle.hide()
            btnCloud.hide()
        } else {
            btnBle.show()
            btnCloud.show()
        }
    }

    fun fragmentNavTitle(title: String, userLogged: Boolean, hasBackButton: Boolean) {
        if (pulseMainActivity != null) {
            val bleIcon = FontDrawable(appContext, R.string.fa_bluetooth_b, false, true)
            val cloudIcon = FontDrawable(appContext, R.string.fa_cloud_solid, true, false)
            val elipseIcon = FontDrawable(appContext, R.string.fa_ellipsis_v_solid, true, false)
            elipseIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_green))

            if (userLogged) {
                cloudIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_green))
            } else {
                cloudIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_gray))
            }

            if (pulseMainActivity.userScreenTitleText != null) {
                pulseMainActivity.userScreenTitleText.text = title
            }

            if (pulseMainActivity.btnCloud != null) {
                pulseMainActivity.btnCloud.setImageDrawable(cloudIcon)
            }

            if (pulseMainActivity.btnLogout != null) {
                pulseMainActivity.btnLogout.setImageDrawable(elipseIcon)
            }

            if (hasBackButton) {
                pulseMainActivity.btnCloud.hide()
            } else {
                pulseMainActivity.btnCloud.show()
            }

            if (pulseMainActivity.deviceConnected) {
                bleIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_green))
            } else {
                bleIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_gray))
            }

//            if (hasBackButton) {
//                if (pulseMainActivity::btnBluetooth != null) {
//
//                    val backIcon = FontDrawable(appContext, R.string.fa_angle_left_solid, true, false)
//                    backIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_blue))
//                    pulseMainActivity. btnBluetooth.setImageDrawable(backIcon)
//                }
//            } else {
//                if (pulseMainActivity::btnBluetooth != null) {
//                    pulseMainActivity.btnBluetooth.setImageDrawable(bleIcon)
//                }
//            }
        }

    }

    open fun fetchData() {}

    fun errorResponse(it: GenericResponseFromJSON, email: String) {
        if (it.Message == "Unauthorized") {
            println("redirect to login")
            pulseMainActivity.redirectToLoginPage(email)
        }
    }

}