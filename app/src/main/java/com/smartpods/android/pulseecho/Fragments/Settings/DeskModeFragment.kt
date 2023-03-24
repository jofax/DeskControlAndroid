package com.smartpods.android.pulseecho.Fragments.Settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Constants.DESK_MODE
import com.smartpods.android.pulseecho.Fragments.SettingsFragment
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.SPAppEventHandler
import com.smartpods.android.pulseecho.Utilities.StopWatch
import com.smartpods.android.pulseecho.Utilities.UserPreference
import com.smartpods.android.pulseecho.Utilities.guard
import com.smartpods.android.pulseecho.ViewModel.DeskModeViewModel
import kotlinx.android.synthetic.main.activity_profile_fragment.*
import kotlinx.android.synthetic.main.desk_mode_fragment.*
import kotlinx.android.synthetic.main.desk_mode_fragment.btnDeskAutomatic
import kotlinx.android.synthetic.main.height_settings_fragment.*
import kotlinx.android.synthetic.main.home_heart_fragment.*
import org.jetbrains.anko.support.v4.toast

class DeskModeFragment : BaseFragment() {

    companion object {
        fun newInstance() = DeskModeFragment()
    }

    private lateinit var viewModel: DeskModeViewModel
    var currentDeskMode = DESK_MODE.NONE
    var runSwitchStatus: Boolean = false
    var semiAutomatic: Boolean = false
    var mCurrentDeskMode = currentDeskMode

    private val spDataParseEventListener by lazy {

        DataEventListener().apply {
            onCoreDataEventReceived = {


                semiAutomatic = it.UseInteractiveMode
                runSwitchStatus = it.RunSwitch

                //print("runswitch: ${it.RunSwitch}")

                if (it.RunSwitch == false) {
                    mCurrentDeskMode = DESK_MODE.MANUAL
                }

                if (it.RunSwitch && it.UseInteractiveMode) {
                    mCurrentDeskMode = DESK_MODE.INTERACTIVE
                }

                if (it.RunSwitch && !it.UseInteractiveMode) {
                    mCurrentDeskMode = DESK_MODE.AUTOMATIC
                }

            }

            onVerticalProfileDataEventReceived = {
                println("onVerticalProfileDataEventReceived : ${it.movements}")

            }

            onDesktopAppHasPriority = {
               if (it) {
                   pulseMainActivity.showToastView(getString(R.string.desktop_has_priority))
               }
            }

            onResumeNormalBLEResponse = {

            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.desk_mode_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DeskModeViewModel::class.java)
        // TODO: Use the ViewModel
        viewActions()
    }

    override fun onResume() {
        println("DeskModeFragment onResume")

        getCurrentDeskMode()

        super.onResume()
    }

    private fun viewActions() {
        btnDeskAutomatic.setOnClickListener {
            resetSelectedMode()
            currentDeskMode = DESK_MODE.AUTOMATIC
            deskModeTitleAndDescription(DESK_MODE.AUTOMATIC)
        }

        btnDeskInteractive.setOnClickListener{
            resetSelectedMode()
            currentDeskMode = DESK_MODE.INTERACTIVE
            deskModeTitleAndDescription(DESK_MODE.INTERACTIVE)
        }

        btnDeskManual.setOnClickListener{
            resetSelectedMode()
            currentDeskMode = DESK_MODE.MANUAL
            deskModeTitleAndDescription(DESK_MODE.MANUAL)

        }

        btnSaveDeskMode.setOnClickListener {

            println("check if device is initialized: ${pulseMainActivity.deviceIsInitialized()}")

            if (pulseMainActivity.deviceIsInitialized()) {
                pushDeskModeCommand(currentDeskMode)
            } else {
                toast(R.string.device_not_connected)
            }
        }
    }

    fun getCurrentDeskMode() {
        resetSelectedMode()
        val desk_mode  = UserPreference.prefs.read("desk_mode", "Manual")
        when(desk_mode) {
            DESK_MODE.MANUAL.rawValue -> {
                deskModeTitleAndDescription(DESK_MODE.MANUAL)
            }

            DESK_MODE.AUTOMATIC.rawValue -> {
                deskModeTitleAndDescription(DESK_MODE.AUTOMATIC)
            }

            DESK_MODE.INTERACTIVE.rawValue -> {
                deskModeTitleAndDescription(DESK_MODE.INTERACTIVE)
            }
        }
    }

    fun deskModeTitleAndDescription(mode: DESK_MODE) {
        when(mode) {
            DESK_MODE.AUTOMATIC -> {
                deskModeTitle.text = getString(R.string.desk_mode_interactive_title)
                deskModeDescription.text = getString(R.string.desk_mode_interactive_desc)
                btnDeskAutomatic.setBackgroundResource(R.drawable.sp_rounded_button_selected)
            }

            DESK_MODE.INTERACTIVE -> {
                deskModeTitle.text = getString(R.string.desk_mode_auto_title)
                deskModeDescription.text = getString(R.string.desk_mode_auto_desc)
                btnDeskInteractive.setBackgroundResource(R.drawable.sp_rounded_button_selected)
            }

            DESK_MODE.MANUAL -> {
                deskModeTitle.text = getString(R.string.desk_mode_manual_title)
                deskModeDescription.text = getString(R.string.desk_mode_manual_desc)
                btnDeskManual.setBackgroundResource(R.drawable.sp_rounded_button_selected)
            }
        }
    }

    fun resetSelectedMode() {
        currentDeskMode = DESK_MODE.NONE
        btnDeskAutomatic.setBackgroundResource(R.drawable.sp_rounded_button)
        btnDeskManual.setBackgroundResource(R.drawable.sp_rounded_button)
        btnDeskInteractive.setBackgroundResource(R.drawable.sp_rounded_button)
    }

    fun pushDeskModeCommand(mode: DESK_MODE) {
        when(mode) {
            DESK_MODE.MANUAL -> {

                if (this.pulseMainActivity.SPBleDevice.isConnected()) {

                    val manualMode = command.GetDeskTurnOff()
                    this.pulseMainActivity.sendCommand(manualMode, "GetDeskTurnOff")

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            SPAppEventHandler.showTabSelected(0)
                        },
                        1000 // value in milliseconds
                    )
                }
            }

            DESK_MODE.AUTOMATIC -> {

                if (this.pulseMainActivity.SPBleDevice.isConnected()) {

                    val switchOn = command.GetDeskTurnOn()
                    val semiAuto = command.GetDisableSemiAutomaticMode()

                    if (!runSwitchStatus) {
                        this.pulseMainActivity.sendCommand(switchOn, "GetDeskTurnOn")
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                this.pulseMainActivity.sendCommand(semiAuto, "GetDisableSemiAutomaticMode")
                                SPAppEventHandler.showTabSelected(0)
                            },
                            1000 // value in milliseconds
                        )
                        runSwitchStatus = true
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                this.pulseMainActivity.sendCommand(semiAuto, "GetDisableSemiAutomaticMode")
                                SPAppEventHandler.showTabSelected(0)
                            },
                            1000 // value in milliseconds
                        )
                    }

                }
            }

            DESK_MODE.INTERACTIVE -> {

                if (this.pulseMainActivity.SPBleDevice.isConnected()) {

                    val switchOn = command.GetDeskTurnOn()
                    val semiAuto = command.GetEnableSemiAutomaticMode()

                    if (runSwitchStatus == false) {
                        this.pulseMainActivity.sendCommand(switchOn, "GetDeskTurnOn")

                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                this.pulseMainActivity.sendCommand(semiAuto, "GetEnableSemiAutomaticMode")
                                SPAppEventHandler.showTabSelected(0)
                            },
                            1000 // value in milliseconds
                        )
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                this.pulseMainActivity.sendCommand(semiAuto, "GetEnableSemiAutomaticMode")
                                SPAppEventHandler.showTabSelected(0)
                            },
                            1000 // value in milliseconds
                        )
                    }


                }
            }
        }
    }

}