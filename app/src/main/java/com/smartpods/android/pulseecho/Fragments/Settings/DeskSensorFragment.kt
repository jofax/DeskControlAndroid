package com.smartpods.android.pulseecho.Fragments.Settings

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.slider.Slider
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Constants.INVERT_TYPE
import com.smartpods.android.pulseecho.Interfaces.CoreObject
import com.smartpods.android.pulseecho.Interfaces.PulseCore
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.*
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.ViewModel.DeskSensorViewModel
import kotlinx.android.synthetic.main.desk_mode_fragment.*
import kotlinx.android.synthetic.main.desk_sensor_fragment.*
import kotlinx.android.synthetic.main.home_heart_fragment.*

class DeskSensorFragment : BaseFragment() {

    companion object {
        fun newInstance() = DeskSensorFragment()
    }

    private lateinit var viewModel: DeskSensorViewModel
    var isAutomatic: Boolean = false
    var isLegacy: Boolean = false

    var lightValue: Int? = null
    var presenceValue: Int? = null
    var safetyValue: Int? = null
    var awayAdjustValue: Int? = null
    var appSigmaStandingThreshold: Int? = null
    var appSigmaSittingThreshold: Int? = null
    var enableHeatSensingFlipStanding: Boolean? = null
    var enableHeatSensingFlipSitting: Boolean? = null

    var isAutomaticPresence: Boolean? = null

    var sigmaStandingThreshold: Int? = null
    var safetyAlertShown: Boolean = false

    lateinit var coreOneObject: PulseCore

    private val spDataParseEventListener by lazy {

        DataEventListener().apply {
            onCoreDataEventReceived = {
                //println("DeskSensorFragment : ${it}")

                coreOneObject = it

                if (lightValue == null ||
                    appSigmaSittingThreshold == null ||
                    safetyValue == null ||
                    awayAdjustValue == null ||
                    appSigmaStandingThreshold == null) {

                    lightValue = it.LEDSlider
                    appSigmaSittingThreshold = it.SitPresence
                    safetyValue = it.SafetySlider
                    awayAdjustValue = it.AwaySlider
                    appSigmaStandingThreshold = it.StandPresence

                    updateControlValues()

                } else {
                    if (it.LEDSlider != lightValue ||
                        it.SitPresence != appSigmaSittingThreshold ||
                        it.SafetySlider != safetyValue ||
                        it.AwaySlider != awayAdjustValue ||
                        it.StandPresence != appSigmaStandingThreshold) {

                        lightValue = it.LEDSlider
                        appSigmaSittingThreshold = it.SitPresence
                        safetyValue = it.SafetySlider
                        awayAdjustValue = it.AwaySlider
                        appSigmaStandingThreshold = it.StandPresence

                        updateControlValues()

                    }

                }

                if (enableHeatSensingFlipStanding == null || enableHeatSensingFlipSitting == null) {
                        enableHeatSensingFlipStanding = it.EnableHeatSenseFlipStanding
                        enableHeatSensingFlipSitting = it.EnableHeatSenseFlipSitting
                } else {
                    if (it.EnableHeatSenseFlipStanding != enableHeatSensingFlipStanding ||
                        it.EnableHeatSenseFlipSitting != enableHeatSensingFlipSitting) {
                        enableHeatSensingFlipStanding = it.EnableHeatSenseFlipStanding
                        enableHeatSensingFlipSitting = it.EnableHeatSenseFlipSitting
                    }
                }



                if (isAutomaticPresence == null){

                    isAutomaticPresence = it.AutoPresenceDetection

                    activity?.runOnUiThread {
                        if (it.AutoPresenceDetection == true) {
                            presenceIndicator( true,  false,  false)
                        } else {
                            presenceIndicator(false,  true,  false)
                        }
                    }



                } else {
                    if (it.AutoPresenceDetection != isAutomaticPresence) {
                        isAutomaticPresence = it.AutoPresenceDetection

                        activity?.runOnUiThread {
                            if (it.AutoPresenceDetection == true) {
                                presenceIndicator( true,  false,  false)
                            } else {
                                presenceIndicator(false,  true, false)
                            }
                        }

                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.desk_sensor_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DeskSensorViewModel::class.java)
        // TODO: Use the ViewModel
        SPDataParser.registerListener(spDataParseEventListener)
        initilizeUI()
        getBoxInformation()
    }

    fun getBoxInformation() {
        val commandPacket = SPRequestParameters.GetAESKey
        pulseMainActivity.sendCommand(commandPacket, "SPRequestParameters.GetAESKey")
    }

    private fun initilizeUI() {
        //light indicator slider
        lightIndicatorSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        lightSensitivity(slider.value.toInt(),true)
                    },
                    1000 // value in milliseconds
                )
            }
        })

        //safety sliders
        safetySlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        safetySensitivity(slider.value.toInt(),true)
                    },
                    1000 // value in milliseconds
                )
            }
        })

        //away slider
        awaySlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        awayStatus(slider.value.toInt(), true)
                    },
                    1000 // value in milliseconds
                )
            }
        })

        //legacy sitting slider
        sittingControlSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        presenceSensitivity(slider.value.toInt(),3)
                    },
                    1000 // value in milliseconds
                )
            }
        })

        //legacy standing slider
        standingControlSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        presenceSensitivity(slider.value.toInt(),4)
                    },
                    1000 // value in milliseconds
                )
            }
        })


        //Legacy detection
        switchEnableLegacy.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                presenceIndicator(false, true,  true)
            } else {
                presenceIndicator( true,  false, true)
            }
        }

        //Legacy Sitting detection
        switchInvertSitting.setOnCheckedChangeListener { buttonView, isChecked ->
            val flipSit = enableHeatSensingFlipSitting
            if (flipSit != null) {
                setPresenceInvert(flipSit, INVERT_TYPE.SIT)
            }
        }

        //Legacy Standing detection
        switchInvertStanding.setOnCheckedChangeListener { buttonView, isChecked ->
            val flipStand =enableHeatSensingFlipStanding
            if (flipStand != null) {
                setPresenceInvert(flipStand, INVERT_TYPE.STAND)
            }
        }


        //Light indicator button actions
        btnDecreaseLight.setOnClickListener{
            if (lightIndicatorSlider.value.toInt() > 0) {
                lightIndicatorSlider.value -= 1
                lightSensitivity(lightIndicatorSlider.value.toInt(),true)
            }
        }

        btnIncreaseLight.setOnClickListener{
            if (lightIndicatorSlider.value.toInt() < 2) {
                lightIndicatorSlider.value += 1
                lightSensitivity(lightIndicatorSlider.value.toInt(),true)
            }
        }

        //Safety button actions
        btnDecreaseSafety.setOnClickListener{
            if (safetySlider.value.toInt() > 0) {
                safetySlider.value -= 1
                safetySensitivity(safetySlider.value.toInt(), true)
            }
        }

        btnIncreaseSafety.setOnClickListener{
            if (safetySlider.value.toInt() < 10) {
                safetySlider.value += 1
                safetySensitivity(safetySlider.value.toInt(), true)
            }
        }

        //Away button actions
        btnDecreaseAway.setOnClickListener{
            if (awaySlider.value > 0){
                awaySlider.value -= 1
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        awayStatus(awaySlider.value.toInt(), true)
                    },
                    500 // value in milliseconds
                )
            }
        }

        btnIncreaseAway.setOnClickListener{
            if (awaySlider.value < 5){
                awaySlider.value += 1
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        awayStatus(awaySlider.value.toInt(), true)
                    },
                    500 // value in milliseconds
                )
            }
        }

        //Sitting legacy button actions
        btnDecreaseSitting.setOnClickListener{
            if (sittingControlSlider.value > 0){
                sittingControlSlider.value -= 1
                presenceSensitivity(sittingControlSlider.value.toInt(),  3)
            }
        }

        btnIncreaseSitting.setOnClickListener{
            if (sittingControlSlider.value < 10){
                sittingControlSlider.value += 1
                presenceSensitivity(sittingControlSlider.value.toInt(),  3)
            }
        }

        //Standing legacy button actions
        btnDecreaseStanding.setOnClickListener{
            if (standingControlSlider.value > 0){
                standingControlSlider.value -= 1
                presenceSensitivity(sittingControlSlider.value.toInt(),  4)
            }
        }

        btnIncreaseStanding.setOnClickListener{
            if (standingControlSlider.value < 10){
                standingControlSlider.value += 1
                presenceSensitivity(sittingControlSlider.value.toInt(),  4)
            }
        }

        btnCapturePrecense.setOnClickListener{
            if (it.tag == 20) {
                presenceIndicator(true, false,true)
            } else {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                            SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                                SPRequestParameters.CaptureAutomaticDetection )
                        }
                    },
                    500 // value in milliseconds
                )
            }
        }
    }

    fun presenceIndicator(automatic: Boolean, legacy: Boolean, withCommand: Boolean) {

        if (switchEnableLegacy != null) {
            switchEnableLegacy.isChecked = legacy
            isLegacy = legacy
            hideShowLegacyView(legacy)

        }

        println("automatic: $automatic | legacy: $legacy | withCommand: $withCommand")

        if (legacy) {

            if (this.btnCapturePrecense != null) {
                btnCapturePrecense.text = getString(R.string.desk_mode_automatic)
                btnCapturePrecense.tag = 20
                btnCapturePrecense.setBackgroundResource(R.drawable.sp_rounded_button)
            }
            sittingDivider.show()
        }

        if (automatic) {

            if (this.btnCapturePrecense != null) {
                btnCapturePrecense.setBackgroundResource(R.drawable.sp_rounded_button_selected)
                btnCapturePrecense.tag = 21
                btnCapturePrecense.text = getString(R.string.desk_controls_capture_title)
            }
            sittingDivider.hide()
        }

        isAutomatic = automatic

        if (withCommand) {
            if (legacy) {
                if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                    SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                        SPRequestParameters.LegacyDetection )
                }
            }

            if (automatic) {
                if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                    SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                        SPRequestParameters.AutomaticDetection )
                }

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                            SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                                SPRequestParameters.CaptureAutomaticDetection )
                        }
                    },
                    1000 // value in milliseconds
                )
            }
        }


    }

    fun updateControlValues() {

        if (coreOneObject != null) {

            lightIndicatorSlider?.value = coreOneObject.LEDSlider.toFloat()
            lightSensitivity(coreOneObject.LEDSlider,  false)

            safetySlider?.value = coreOneObject.SafetySlider.toFloat()
            safetySensitivity(coreOneObject.SafetySlider ,false)

            awaySlider?.value = coreOneObject.AwaySlider.toFloat()
            awayStatus(coreOneObject.AwaySlider, false)

            sittingControlSlider?.value = coreOneObject.SitPresence.toFloat()
            presenceSensitivity(coreOneObject.SitPresence, 3)

            standingControlSlider?.value = coreOneObject.StandPresence.toFloat()
            presenceSensitivity(coreOneObject.StandPresence, 4)
        }
    }

    fun invertedStatusIndicators() {
        val flipStand = enableHeatSensingFlipStanding
        val flipSit = enableHeatSensingFlipSitting

        if (flipSit != null) {
            activity?.runOnUiThread {
                switchInvertSitting.isChecked = flipSit
            }
        }

        if (flipStand != null) {
            activity?.runOnUiThread {
                switchInvertStanding.isChecked = flipStand
            }
        }

        if(flipSit == true) {
            //Utilities.instance.invertSittingThreshold = 100 + (self.coreOneObject?.SitPresence ?? 0 * 15)
        } else {
            //Utilities.instance.invertSittingThreshold = 70 + ((10 - (self.coreOneObject?.SitPresence ?? 0) * 15))
        }

        if(flipStand == true) {
            //Utilities.instance.invertStandingThreshold = 100 + (self.coreOneObject?.StandPresence ?? 0 * 15)
        } else {
            //Utilities.instance.invertStandingThreshold = 70 + ((10 - (self.coreOneObject?.StandPresence ?? 0) * 15))
        }
    }

    fun presenceSensitivity(value: Int, tag: Int) {
        if (tag == 3) {
            sittingControlHeaderValue.text = "${value}"
            setPresenceSittingSensitivity(value)
        } else {
            standingControlHeaderValue.text = "${value}"
            setPresenceStandingSensitivity(value)
        }
    }

    fun lightSensitivity(value: Int, command: Boolean) {
        println("lightSensitivity: $value")

        when(value) {
            0 -> {
                indicatorLightsValue.text = getString(R.string.desk_controls_light_brightness_low)
            }

            1-> {
                indicatorLightsValue.text = getString(R.string.desk_controls_light_brightness_dim)
            }

            2 -> {
                indicatorLightsValue.text = getString(R.string.desk_controls_light_brightness_high)
            }
        }

        if (command) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {

                val adjustLight = this.command.GetSetIndicatorLight(value)
                this.pulseMainActivity.sendCommand(adjustLight, "GetSetIndicatorLight")
            }
        }
    }

    fun awayStatus(value: Int, command: Boolean) {
        awayHeaderValue.text = getAwayString(value)
        if (command) {
            setAwayStatusDelay(value)
        }

    }

    fun getAwayString(value: Int) : String {

        return when(value) {
            1 -> {
                "30 sec"
            }
            2 -> {
                "1 min"
            }
            3 -> {
                "3 min"
            }
            4 -> {
                "5 min"
            }
            5 -> {
                "10 min"
            }
            else -> {
                "0 sec"
            }
        }

    }

    fun hideShowLegacyView(show: Boolean) {

        if (show) {
            sittingSensitivityLayout.show()
            standingSensitivityLayout.show()
        } else {
            sittingSensitivityLayout.hide()
            standingSensitivityLayout.hide()
        }

        saveUserAppState()
    }

    fun saveUserAppState() {
        /*let email = Utilities.instance.getLoggedEmail()
        let realm = try! Realm(configuration: getRealmForUser(username: email))
            let state = realm.objects(UserAppStates.self).filter("Email = %@", email)

            guard state.count > 0 else {
                return
            }

            try! realm.write {
                state[0].LegacyControls = isLegacy
                state[0].AutomaticControls = isAutomatic
                realm.add(state, update: .modified)
                realm.refresh()
            }*/
    }

    fun safetySensitivity(value: Int, command: Boolean) {
        safetyHeaderValue.text = "${value}"

        if(!safetyAlertShown) else {
            return
        }

        if(command) {
            safetyAlertShown = true
            fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                safetyHeaderValue.text = "${value}"
                setSafetySensitivty(value, command)
            }

            fun cancelAction() = DialogInterface.OnClickListener { dialog, which ->
                safetyAlertShown = false
                safetySlider?.value = safetyValue?.toFloat() ?: 0.0f
                val _safety = safetyValue
                safetyHeaderValue.text = "${_safety}"

            }

            pulseMainActivity.showALertDialog(getString(R.string.notice),
                getString(R.string.safety_alert),
                false, getString(R.string.btn_agree), okAction(), getString(R.string.btn_cancel), cancelAction())
        }
    }

    fun setSafetySensitivty(value: Int, withCommand: Boolean) {
        // 0 - 10
        safetyValue = value

        if (withCommand) {
            safetyAlertShown = false
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {

                val adjustSafety = this.command.GetSetCrushThreshold(value)
                this.pulseMainActivity.sendCommand(adjustSafety, "GetSetCrushThreshold")
            }
        }

    }

    fun setAwayStatusDelay(value: Int) {
        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {

            val adjustAwayDelay = this.command.GetSetAwayAdjust(value)
            this.pulseMainActivity.sendCommand(adjustAwayDelay, "GetSetAwayAdjust")
        }
    }

    fun setPresenceSittingSensitivity(value: Int) {
        // 0 to 10

        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            val adjustSitPresence = this.command.GetSetPNDThreshold(value)
            this.pulseMainActivity.sendCommand(adjustSitPresence, "GetSetPNDThreshold")
        }
    }

    fun setPresenceStandingSensitivity(value: Int) {
        // 0 to 10

        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            val adjustStandPresence = this.command.GetSetPNDStandThreshold(value)
            this.pulseMainActivity.sendCommand(adjustStandPresence, "GetSetPNDStandThreshold")
        }
    }

    fun setPresenceInvert(inverted: Boolean, invertType: INVERT_TYPE) {
        if(invertType == INVERT_TYPE.SIT) {
            if(inverted) {
                if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                    val noInvertSit = this.command.GetPresenceNoInverted()
                    this.pulseMainActivity.sendCommand(noInvertSit, "GetPresenceNoInverted")
                    enableHeatSensingFlipSitting = false
                }
            } else {

                if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                    val invertSit = this.command.GetPresenceInverted()
                    this.pulseMainActivity.sendCommand(invertSit, "GetPresenceInverted")
                    enableHeatSensingFlipSitting = true
                }
            }

            invertedStatusIndicators()
        }

        if (invertType == INVERT_TYPE.STAND) {
            if (inverted) {
                if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                    val noInvertStand = this.command.GetPresenceStandNoInverted()
                    this.pulseMainActivity.sendCommand(noInvertStand, "GetPresenceStandNoInverted")
                    enableHeatSensingFlipStanding = false
                }
            } else {

                if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                    val invertStand = this.command.GetPresenceStandInverted()
                    this.pulseMainActivity.sendCommand(invertStand, "GetPresenceStandInverted")
                    enableHeatSensingFlipStanding = true
                }
            }

            invertedStatusIndicators()
        }

    }
}