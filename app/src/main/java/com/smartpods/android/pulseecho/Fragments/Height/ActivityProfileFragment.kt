package com.smartpods.android.pulseecho.Fragments.Height

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.ViewModel.ActivityProfileViewModel
import kotlinx.android.synthetic.main.activity_profile_fragment.*
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.smartpods.android.pulseecho.Constants.ACTIVITY_PROFILE_TYPE
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.CUSTOM_ACTIVITY_PROFILE
import com.smartpods.android.pulseecho.Constants.ProfileSettingsType
import com.smartpods.android.pulseecho.CustomUI.CircularProgressIndicator
import com.smartpods.android.pulseecho.Utilities.SPAppEventHandler
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.hide
import com.smartpods.android.pulseecho.Utilities.show
import com.vicpin.krealmextensions.isAutoIncrementPK
import org.jetbrains.anko.support.v4.toast
import kotlin.math.roundToInt

class ActivityProfileFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    private val spDataParseEventListener by lazy {
        var progress = 0
        DataEventListener().apply {
            onCoreDataEventReceived = {
                //println("ActivityProfileFragment onCoreDataEventReceived : $it")

                activity?.runOnUiThread {
                    //timer progress status
                    if (activityProfileCircular_progress != null) {
                        if (activityProfileCircular_progress.progress != it.MainTimerCycleSeconds.toDouble()) {
                            activityProfileCircular_progress.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())

                        }
                    }

                    if (activityCustomThirtyProfileCircular_progress != null) {
                        if (activityCustomThirtyProfileCircular_progress.progress != it.MainTimerCycleSeconds.toDouble()) {
                            activityCustomThirtyProfileCircular_progress.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())
                        }
                    }

                    //progress status
                    if (activityProfileCircleProgressTimer != null) {
                        if (activityProfileCircleProgressTimer.progress != it.MainTimerCycleSeconds.toDouble()) {
                            activityProfileCircleProgressTimer.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())
                        }
                    }

                    if (activityCustomThirtyProfileCircular_progress != null) {
                        if (activityCustomThirtyProfileCircular_progress.progress != it.MainTimerCycleSeconds.toDouble()) {
                            activityCustomThirtyProfileCircular_progress.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())
                        }
                    }

                    if (activityCustomThirtyProfileCircleProgressTimer != null) {
                        if (activityCustomThirtyProfileCircleProgressTimer.progress != it.MainTimerCycleSeconds.toDouble()) {
                            activityCustomThirtyProfileCircleProgressTimer.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())
                        }
                    }
                }

            }

            onVerticalProfileDataEventReceived = {
                //println("ActivityProfileFragment onVerticalProfileDataEventReceived : $it")
                //activityProfileCircleProgressTimer.movements = it.movements
                //activityProfileCircleProgressTimer.maxProgress = 3600.0
            }
        }
    }

    var selectedCustomDuration = CUSTOM_ACTIVITY_PROFILE.NONE
    var standingDuration: Int = 5
    var selectedStanding30FirstHour: Int = 5
    var selectedStanding30SecondHour: Int = 5
    var selectedStanding60Hour: Int = 5
    var firstStartDurationAngle: Int = 270
    var secondStartDurationAngle: Int = 90
    var selectedCustomThirtyDuration: Int = 1
    var activity_profile_duration: List<HashMap<String, Int>> = listOf()
    var commitProfileCount: Int = 0
    var commitProfileLimit: Int = 2
    var selectedProfileSettingsType = ProfileSettingsType.ACTIVE
    var standingTime1: Int = 5
    var standingTime2: Int = 5

    companion object {
        fun newInstance() = ActivityProfileFragment()
    }

    private lateinit var viewModel: ActivityProfileViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ActivityProfileViewModel::class.java)
        // TODO: Use the ViewModel

        SPDataParser.registerListener(spDataParseEventListener)
        setViewActions()
        getBoxData()
        circularProgressView()

        activityProfileHeartProgressLayout.show()
        activityCustomThirtyLayout.hide()
        activityProfileCustomThirtyDuration.hide()
    }

    override fun onResume() {
        super.onResume()
        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            getBoxData()
        }
        fragmentNavTitle("Height Settings", true, false)
        requestProfileSettings()
    }

    fun setViewActions() {

        btnPresetFive.setOnClickListener{
            activityProfileHeartProgressLayout.show()
            activityCustomThirtyLayout.hide()
            activityProfileCustomThirtyDuration.hide()
            this.resetPresetActions()
            this.setImageBackground(0)
            this.standingTime1 = 5
            this.standingTime2 = 0
            selectedProfileSettingsType = ProfileSettingsType.ACTIVE
        }

        btnPresetFifteen.setOnClickListener{
            activityProfileHeartProgressLayout.show()
            activityCustomThirtyLayout.hide()
            activityProfileCustomThirtyDuration.hide()
            this.resetPresetActions()
            this.setImageBackground(1)
            this.standingTime1 = 15
            this.standingTime2 = 0
            selectedProfileSettingsType = ProfileSettingsType.MODERATELYACTIVE
        }

        btnPresetThirty.setOnClickListener{
            activityProfileHeartProgressLayout.show()
            activityCustomThirtyLayout.hide()
            activityProfileCustomThirtyDuration.hide()
            this.resetPresetActions()
            this.setImageBackground(2)
            this.standingTime1 = 15
            this.standingTime2 = 15
            selectedProfileSettingsType = ProfileSettingsType.VERYACTIVE
        }

        btnCustom30.setOnClickListener{
            this.selectedCustomDuration = CUSTOM_ACTIVITY_PROFILE.THIRTY
            selectedProfileSettingsType = ProfileSettingsType.CUSTOM
            this.setDurationAdapter()
            activityProfileCustomThirtyDuration.show()
            this.resetPresetActions()
            this.setImageBackground(3)
            activityProfileHeartProgressLayout.hide()
            activityCustomThirtyLayout.show()
            activityCustomDurationTitleLabel.text = getString(R.string.activity_profile_bottom_first_30)

            activityProfileCustomBackground.show()
            customProfile2ndControls.show()
            customProfile1stControls.show()

            custom30firstPeriodTitle.show()
            btnEditFirst.show()

            this.drawCustomMovementLayout(CUSTOM_ACTIVITY_PROFILE.THIRTY)

            this.customThirtyGlowAnimation(1)
        }

        btnCustom60.setOnClickListener{
            this.selectedCustomDuration = CUSTOM_ACTIVITY_PROFILE.SIXTY
            selectedProfileSettingsType = ProfileSettingsType.CUSTOM
            this.setDurationAdapter()
            activityProfileCustomThirtyDuration.show()
            this.resetPresetActions()
            this.setImageBackground(4)
            activityProfileCustomBackground.hide()
            customProfile2ndControls.hide()
            customProfile1stControls.hide()
            activityCustomDurationTitleLabel.text = getString(R.string.activity_profile_hour_cycle)

            activityProfileHeartProgressLayout.hide()
            activityCustomThirtyLayout.show()

            this.drawCustomMovementLayout(CUSTOM_ACTIVITY_PROFILE.SIXTY)
        }

        btnEditFirst.setOnClickListener{
            this.customThirtyGlowAnimation(1)
            this.setDurationSelected(1)
        }

        btnEditSecond.setOnClickListener{
            this.customThirtyGlowAnimation(2)
            this.setDurationSelected(2)
        }

        btnMakeMeHealthy.setOnClickListener{
            when(Utilities.typeOfUserLogged()) {
                CURRENT_LOGGED_USER.Guest -> {

                }

                CURRENT_LOGGED_USER.Cloud -> {
                    viewModel.updateUserProfile(this,
                        selectedProfileSettingsType.rawValue,
                        standingTime1,
                        standingTime2).observe(this.viewLifecycleOwner, {
                            if (it) {
                                pushProfileToTheBox()
                            }
                    })
                }

                CURRENT_LOGGED_USER.None -> {

                }

                CURRENT_LOGGED_USER.Local -> {

                }
            }
        }
    }

    fun setDurationSelected(tag: Int) {
        val itemArray = arrayOf(resources.getStringArray(R.array.custom_30_durations))
        var itemPos = 0

        itemPos = if (tag == 1) {
            itemArray.indexOf(selectedStanding30FirstHour.toString())
        } else {
            itemArray.indexOf(selectedStanding30SecondHour.toString())
        }

        activityCustomDurationSpinner.setSelection(itemPos)
    }

    fun pushProfileToTheBox() {

        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                val activity_profile = command.GenerateVerticalProfile(activity_profile_duration)

                while (commitProfileCount <= commitProfileLimit) {
                    pulseMainActivity.sendCommand(activity_profile, "GenerateVerticalProfile")
                    commitProfileCount++

                    if (commitProfileCount == commitProfileLimit) {
                        commitProfileCount = 0

                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                this.getBoxData()
                            },
                            1000 // value in milliseconds
                        )

                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                SPAppEventHandler.showTabSelected(0)
                            },
                            1000 // value in milliseconds
                        )

                        break
                    }
                }
            }
        } else {
            toast(R.string.device_not_connected)
        }



        //defaultViewValues()
    }

    fun defaultViewValues() {
        standingDuration = 5
        selectedStanding30FirstHour = 5
        selectedStanding30SecondHour = 5
        selectedStanding60Hour  = 5
        firstStartDurationAngle = 270
        secondStartDurationAngle = 90
        activity_profile_duration = listOf()
    }

    fun customThirtyGlowAnimation(tag: Int) {
        activityProfileCustomFirstView.clearAnimation()
        activityProfileCustomSecondView.clearAnimation()
        selectedCustomThirtyDuration = tag
        when (tag) {
            1 -> {

                custom30secondPeriodTitle.show()
                btnEditSecond.show()

                custom30firstPeriodTitle.hide()
                btnEditFirst.hide()

                activityCustomDurationTitleLabel.text = getString(R.string.activity_profile_bottom_first_30)
                activityProfileCustomFirstView.setBackgroundResource(R.drawable.custom_thirty_first_bg_on)
                activityProfileCustomSecondView.setBackgroundResource(R.drawable.custom_thirty_second_bg_off)

                val animation = AlphaAnimation(1.toFloat(), 0f)
                animation.duration = 2000
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                animation.repeatMode = Animation.REVERSE
                activityProfileCustomFirstView.startAnimation(animation)
            }

            2 -> {
                custom30secondPeriodTitle.hide()
                btnEditSecond.hide()

                custom30firstPeriodTitle.show()
                btnEditFirst.show()

                activityCustomDurationTitleLabel.text = getString(R.string.activity_profile_bottom_second_30)
                activityProfileCustomFirstView.setBackgroundResource(R.drawable.custom_thirty_first_bg_off)
                activityProfileCustomSecondView.setBackgroundResource(R.drawable.custom_thirty_second_bg_on)

                val animation = AlphaAnimation(1.toFloat(), 0f)
                animation.duration = 2000
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                animation.repeatMode = Animation.REVERSE
                activityProfileCustomSecondView.startAnimation(animation)
            }
        }
    }

    fun setDurationAdapter() {

        when(selectedCustomDuration) {
            CUSTOM_ACTIVITY_PROFILE.NONE -> {}
            CUSTOM_ACTIVITY_PROFILE.THIRTY -> {
                this.context?.let {
                    ArrayAdapter.createFromResource(
                        it,
                        R.array.custom_30_durations,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        activityCustomDurationSpinner.adapter = adapter
                        activityCustomDurationSpinner.onItemSelectedListener = this
                    }
                }
            }

            CUSTOM_ACTIVITY_PROFILE.SIXTY -> {
                this.context?.let {
                    ArrayAdapter.createFromResource(
                        it,
                        R.array.custom_60_durations,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        activityCustomDurationSpinner.adapter = adapter
                        activityCustomDurationSpinner.onItemSelectedListener = this
                    }
                }
            }
        }
    }

    fun resetPresetActions() {
        //selectedCustomDuration = CUSTOM_ACTIVITY_PROFILE.NONE
        btnPresetFive.setImageDrawable(this.context?.let {
            getDrawable(
                it,
                R.drawable.five)
        })

        btnPresetFifteen.setImageDrawable(this.context?.let {
            getDrawable(
                it,
                R.drawable.fifteen)
        })

        btnPresetThirty.setImageDrawable(this.context?.let {
            getDrawable(
                it,
                R.drawable.thirty)
        })

        btnCustom30.setBackgroundResource(R.drawable.sp_rounded_button)
        btnCustom60.setBackgroundResource(R.drawable.sp_rounded_button)
    }

    fun setImageBackground(tag: Int) {
        when(tag) {
            0 -> {
                btnPresetFive.setImageDrawable(this.context?.let {
                    getDrawable(
                        it,
                        R.drawable.five_click)
                })
                presetMinuteTitle.text = resources.getString(R.string.activity_profile_five_preset)
                createActivityProfileMovements(ACTIVITY_PROFILE_TYPE.PRESETFIVE)
            }

            1 -> {
                btnPresetFifteen.setImageDrawable(this.context?.let {
                    getDrawable(
                        it,
                        R.drawable.fifteen_click)
                })
                presetMinuteTitle.text = resources.getString(R.string.activity_profile_fifteen_preset)
                createActivityProfileMovements(ACTIVITY_PROFILE_TYPE.PRESETFIFTEEN)
            }

            2 -> {
                btnPresetThirty.setImageDrawable(this.context?.let {
                    getDrawable(
                        it,
                        R.drawable.thirty_click)
                })
                presetMinuteTitle.text = resources.getString(R.string.activity_profile_thirty_preset)
                createActivityProfileMovements(ACTIVITY_PROFILE_TYPE.PRESETTHIRTY)
            }

            3 -> {
                btnCustom30.setBackgroundResource(R.drawable.sp_rounded_button_selected)
            }

            4 -> {
                btnCustom60.setBackgroundResource(R.drawable.sp_rounded_button_selected)
            }
        }

    }

    fun requestProfileSettings() {
        if (view != null) else return
        viewModel.getProfileSettings(this).observe(this.viewLifecycleOwner, {
            if (it) {
                println("requestProfileSettings : ${viewModel.userProfile.Email}")

                updateUI()
            }
        })
    }

    fun getBoxData() {

        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                    SPRequestParameters.Profile )
            }
        }

    }

    fun updateUI() {
        selectedProfileSettingsType = ProfileSettingsType(viewModel.userProfile.ProfileSettingType)
        println("viewModel.userProfile : ${viewModel.userProfile.Email}")
        println("viewModel.selectedProfileSettingsType : $selectedProfileSettingsType")

        when(selectedProfileSettingsType) {
            ProfileSettingsType.ACTIVE -> {
                btnPresetFive.callOnClick()
            }

            ProfileSettingsType.MODERATELYACTIVE -> {
                btnPresetFifteen.callOnClick()
            }

            ProfileSettingsType.VERYACTIVE -> {
                btnPresetThirty.callOnClick()
            }

            ProfileSettingsType.CUSTOM -> {
                if (viewModel.userProfile.StandingTime2 == 0) {
                    btnCustom30.callOnClick()
                } else {
                    btnCustom60.callOnClick()
                }
            }
        }

    }

    fun createCustomDuration(duration: List<HashMap<String, Int>>) {


        activityCustomThirtyProfileCircleProgressTimer.movements = duration
        activityCustomThirtyProfileCircleProgressTimer.maxProgress = 3600.0
        activityCustomThirtyProfileCircleProgressTimer.setProgress(0.0, 3600.0)
    }

    private fun circularProgressView() {

        activityProfileCircularbackground.setDotWidthDp(500)
        activityProfileCircularbackground.maxProgress = 100.0
        activityProfileCircularbackground.setShouldDrawDot(false)
        activityProfileCircularbackground.isAnimationEnabled = false
        activityProfileCircularbackground.setShouldDrawDot(false)
        activityProfileCircularbackground.setDotWidthDp(0)
        activityProfileCircularbackground.isFillBackgroundEnabled = true
        activityProfileCircularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_white_text)
        activityProfileCircularbackground.alpha = 0.0F
        activityProfileCircularbackground.setProgressStrokeWidthDp(0)
        activityProfileCircularbackground.setProgress(100.0, 100.0)

        activityCustomThirtyProfileCircularbackground.setDotWidthDp(500)
        activityCustomThirtyProfileCircularbackground.maxProgress = 100.0
        activityCustomThirtyProfileCircularbackground.setShouldDrawDot(false)
        activityCustomThirtyProfileCircularbackground.isAnimationEnabled = false
        activityCustomThirtyProfileCircularbackground.setShouldDrawDot(false)
        activityCustomThirtyProfileCircularbackground.setDotWidthDp(0)
        activityCustomThirtyProfileCircularbackground.isFillBackgroundEnabled = true
        activityCustomThirtyProfileCircularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_white_text)
        activityCustomThirtyProfileCircularbackground.alpha = 0.0F
        activityCustomThirtyProfileCircularbackground.setProgressStrokeWidthDp(0)
        activityCustomThirtyProfileCircularbackground.setProgress(100.0, 100.0)


        //Heart Progress Timer with knob
        activityProfileCircular_progress.maxProgress = 3600.0
        activityProfileCircular_progress.setShouldDrawDot(true)
        activityProfileCircular_progress.isAnimationEnabled = true
        activityProfileCircular_progress.progressColor = Utilities.getCustomRawColor(R.color.smartpods_green)
        activityProfileCircular_progress.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_blue)
        activityProfileCircular_progress.dotColor = Utilities.getCustomRawColor(R.color.smartpods_green)
        activityProfileCircular_progress.setShouldDrawDot(true)
        activityProfileCircular_progress.setDotWidthDp(10)
        activityProfileCircular_progress.progressStrokeCap = CircularProgressIndicator.CAP_ROUND
        activityProfileCircular_progress.setProgress(0.0, 3600.0)

        activityProfileCircleProgressTimer.maxProgress = 3600.0
        activityProfileCircleProgressTimer.setProgress(0.0, 3600.0)

        activityCustomThirtyProfileCircular_progress.maxProgress = 3600.0
        activityCustomThirtyProfileCircular_progress.setShouldDrawDot(true)
        activityCustomThirtyProfileCircular_progress.isAnimationEnabled = true
        activityCustomThirtyProfileCircular_progress.progressColor = Utilities.getCustomRawColor(R.color.smartpods_green)
        activityCustomThirtyProfileCircular_progress.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_blue)
        activityCustomThirtyProfileCircular_progress.dotColor = Utilities.getCustomRawColor(R.color.smartpods_green)
        activityCustomThirtyProfileCircular_progress.setShouldDrawDot(true)
        activityCustomThirtyProfileCircular_progress.setDotWidthDp(10)
        activityCustomThirtyProfileCircular_progress.progressStrokeCap = CircularProgressIndicator.CAP_ROUND
        activityCustomThirtyProfileCircular_progress.setProgress(0.0, 3600.0)

        activityCustomThirtyProfileCircleProgressTimer.maxProgress = 3600.0
        activityCustomThirtyProfileCircleProgressTimer.setProgress(0.0, 3600.0)

        this.setImageBackground(0)
        createActivityProfileMovements(ACTIVITY_PROFILE_TYPE.PRESETFIVE)
        drawCustomMovementLayout(CUSTOM_ACTIVITY_PROFILE.THIRTY)
    }

    fun drawCustomMovementLayout(custom: CUSTOM_ACTIVITY_PROFILE) {

        when(custom) {
            CUSTOM_ACTIVITY_PROFILE.THIRTY -> {
                val firstPeriodStandSweepAngle = Utilities.getSweepAngle(30 - selectedStanding30FirstHour)
                val firstPeriodStandTime = (30 - selectedStanding30FirstHour) * 60
                val firstPeriodDurationSweepAngle = Utilities.getSweepAngle(selectedStanding30FirstHour)
                val firstPeriodDurationStartAngle = Utilities.getDurationAngle(30 - selectedStanding30FirstHour)
                val firstPeriodTime =  1800 - (selectedStanding30FirstHour * 60) //selectedStanding30FirstHour * 60

                val secondPeriodTime = 3600 - (selectedStanding30SecondHour * 60)
                val secondPeriodStandSweepAngle = Utilities.getSweepAngle(30 - selectedStanding30SecondHour)
                val secondPeriodStandTime = (30 - selectedStanding30SecondHour) * 60
                val secondPeriodDurationSweepAngle = Utilities.getSweepAngle(selectedStanding30SecondHour)
                val secondPeriodDurationStartAngle = Utilities.getDurationAngle(60 - selectedStanding30SecondHour)

                var activity_cycle: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to firstPeriodStandSweepAngle.toInt(),"start" to firstStartDurationAngle, "key" to 7, "value" to 3, "raw" to 3),
                        hashMapOf("sweep" to firstPeriodDurationSweepAngle.toInt(),"start" to firstPeriodDurationStartAngle.roundToInt(),"key" to 4, "value" to firstPeriodTime, "raw" to firstPeriodTime),
                    hashMapOf("sweep" to secondPeriodStandSweepAngle.toInt(),"start" to secondStartDurationAngle, "key" to 7, "value" to 1800, "raw" to 1800),
                   hashMapOf("sweep" to secondPeriodDurationSweepAngle.toInt(),"start" to secondPeriodDurationStartAngle.roundToInt(),"key" to 4, "value" to secondPeriodTime, "raw" to secondPeriodTime))

                this.activity_profile_duration = activity_cycle
                this.createCustomDuration(this.activity_profile_duration)
            }
            CUSTOM_ACTIVITY_PROFILE.SIXTY -> {
                val standPeriodTime = 3600 - (selectedStanding60Hour * 60)
                val standSweepAngle = Utilities.getSweepAngle(60 - selectedStanding60Hour)
                val periodDurationSweepAngle = Utilities.getSweepAngle(selectedStanding60Hour)
                val periodDurationStartAngle = Utilities.getDurationAngle(60 - selectedStanding60Hour)

                var activity_cycle: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to standSweepAngle.toInt(),"start" to 270, "key" to 7, "value" to 3, "raw" to 3),
                    hashMapOf("sweep" to periodDurationSweepAngle.roundToInt(),"start" to periodDurationStartAngle.roundToInt(),"key" to 4, "value" to standPeriodTime, "raw" to standPeriodTime))
                this.activity_profile_duration = activity_cycle
                this.createCustomDuration(activity_cycle)
            }
        }

    }

    fun createActivityProfileMovements(preset: ACTIVITY_PROFILE_TYPE) {
        when(preset) {
            ACTIVITY_PROFILE_TYPE.PRESETFIVE -> {
                val activity_cycle: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to 360,"start" to 270, "key" to 7, "value" to 2700, "raw" to 3),
                    hashMapOf("sweep" to 30,"start" to 240,"key" to 4, "value" to 3300, "raw" to 3300))

                activityProfileCircleProgressTimer.movements = activity_cycle
                activityProfileCircleProgressTimer.maxProgress = 3600.0
                activityProfileCircleProgressTimer.setProgress(0.0, 3600.0)
                this.activity_profile_duration = activity_cycle
            }

            ACTIVITY_PROFILE_TYPE.PRESETFIFTEEN -> {
                val activity_cycle: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to 360,"start" to 270, "key" to 7, "value" to 2700, "raw" to 3),
                    hashMapOf("sweep" to 90,"start" to 180,"key" to 4, "value" to 2700, "raw" to 2700))

                activityProfileCircleProgressTimer.movements = activity_cycle
                activityProfileCircleProgressTimer.maxProgress = 3600.0
                activityProfileCircleProgressTimer.setProgress(0.0, 3600.0)
                this.activity_profile_duration = activity_cycle
            }

            ACTIVITY_PROFILE_TYPE.PRESETTHIRTY -> {
                val activity_cycle: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to 135,"start" to 270, "key" to 7, "value" to 3, "raw" to 3),
                    hashMapOf("sweep" to 90,"start" to 0,"key" to 4, "value" to 900, "raw" to 900),
                    hashMapOf("sweep" to 90,"start" to 90,"key" to 7, "value" to 1800, "raw" to 1800),
                    hashMapOf("sweep" to 90,"start" to 180,"key" to 4, "value" to 2700, "raw" to 2700))

                activityProfileCircleProgressTimer.movements = activity_cycle
                activityProfileCircleProgressTimer.maxProgress = 3600.0
                activityProfileCircleProgressTimer.setProgress(0.0, 3600.0)
                this.activity_profile_duration = activity_cycle
            }

            ACTIVITY_PROFILE_TYPE.CUSTOMTHIRTY -> {
                println("createActivityProfileMovements CUSTOM THIRTY")
            }

            ACTIVITY_PROFILE_TYPE.CUSTOMSIXTY -> {
                println("createActivityProfileMovements CUSTOM SIXTY")
            }
        }
    }

    //Spinner Required Methods
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (this.selectedCustomDuration) {
            CUSTOM_ACTIVITY_PROFILE.THIRTY -> {
                when(selectedCustomThirtyDuration) {
                    1 -> {
                        selectedStanding30FirstHour = activityCustomDurationSpinner.getItemAtPosition(position).toString().toInt()
                        this.standingTime1 = selectedStanding30FirstHour
                        drawCustomMovementLayout(this.selectedCustomDuration)
                    }

                    2 -> {
                        selectedStanding30SecondHour = activityCustomDurationSpinner.getItemAtPosition(position).toString().toInt()
                        this.standingTime1 = selectedStanding30SecondHour
                        drawCustomMovementLayout(this.selectedCustomDuration)
                    }
                }
            }

            CUSTOM_ACTIVITY_PROFILE.SIXTY -> {
                selectedStanding60Hour = activityCustomDurationSpinner.getItemAtPosition(position).toString().toInt()
                this.standingTime1 = selectedStanding60Hour
                this.standingTime2 = 0
                drawCustomMovementLayout(CUSTOM_ACTIVITY_PROFILE.SIXTY)
            }

        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}
