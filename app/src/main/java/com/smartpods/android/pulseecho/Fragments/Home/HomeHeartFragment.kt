package com.smartpods.android.pulseecho .Fragments.Home

import android.app.Activity
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.Rect
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.CommandType
import com.smartpods.android.pulseecho.Constants.MovementType
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.CustomUI.CircularProgressIndicator
import com.smartpods.android.pulseecho.CustomUI.CustomShapeProgressView
import com.smartpods.android.pulseecho.CustomUI.HeartProgressCircle
import com.smartpods.android.pulseecho.Fragments.Home.HeartAccumulationFragment
import com.smartpods.android.pulseecho.Fragments.Home.HeartAccumulationFragmentDirections
import com.smartpods.android.pulseecho.Fragments.Home.HomeHeartFragmentDirections
import com.smartpods.android.pulseecho.Interfaces.PulseVerticalProfile
import com.smartpods.android.pulseecho.Model.PulseAppStates
import com.smartpods.android.pulseecho.Model.PulseDevices
import com.smartpods.android.pulseecho.Model.UserObject
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.*
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleEventListener
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.BLE.toHexString
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.PulseCommand
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.Utilities.Utilities.format
import com.smartpods.android.pulseecho.ViewModel.DeskInfoViewModel
import com.smartpods.android.pulseecho.ViewModel.HomeHeartViewModel
import com.smartpods.android.pulseecho.ViewModel.UserProfileViewModel
import info.androidhive.fontawesome.FontDrawable
import io.realm.Realm
import io.realm.Realm.getApplicationContext
import kotlinx.android.synthetic.main.home_heart_fragment.*
import kotlinx.android.synthetic.main.sp_with_menu_action_bar.*
import kotlinx.android.synthetic.main.user_desk_statistics_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.image
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import java.io.IOException
import java.lang.Math.abs
import java.text.DecimalFormat
import kotlin.experimental.and
import kotlin.math.roundToInt

var countDownTimer : CountDownTimer?= null

class HomeHeartFragment : BaseFragment() {

    private var heartProgress: Int = 0
    private var awayStatus: Boolean = false
    private var safetyStatus: Boolean = false
    private var runSwitchStatus: Boolean = false
    private var heightStatus: Boolean = false
    private var commissioningFlag: Boolean = false
    private var calibrationMode: Boolean = false
    private var alertnateATBMode: Boolean = false
    private var currentDeskMode: String = ""
    private var isInteractive: Boolean = false
    var mCurrentDeskMode = currentDeskMode
    private var pendingMovementCode: Int = 0
    private var interactivePopUpShowed = false
    private var safetyPopUpShowed = false
    private var isDeskCurrentlyBooked = false
    private lateinit var verticalMovementProfile: PulseVerticalProfile

    var dailyHearts: Double = 0.0
    var dailyHeartsTotal: Double = 0.0
    var heartProgressValue = 0.0
    var isInteractivePopUpShowing = false
    var InteractivePopUpShowState = false


    companion object {
        fun newInstance() = HomeHeartFragment()
    }

    private lateinit var viewModel: UserProfileViewModel

    private val connectionEventListener by lazy {

        SPBleEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                println("HomeFragment onConnectionSetupComplete bond state: " + gatt.device.bondState)

                if (gatt.device.bondState == 12) {
                    if (gatt.device.isConnected()) {
                        SPBleManager.sendCommandToCharacteristic(gatt.device,
                            SPRequestParameters.All )
                    }

                    val email = Utilities.getLoggedEmail()
                    val serial = Utilities.getSerialNumber()
                    Utilities.saveOrUpdatePulseDevice(hashMapOf("Email" to email,
                        "Identifier" to gatt.device.address.toString(),
                        "Serial" to serial.toString(),
                        "State" to gatt.device.bondState,
                        "PeripheralName" to gatt.device.name.toString(),
                        "DisconnectedByUser" to false))
                }
            }
            onDisconnect = {
                println("HomeFragment onDisconnect: " + it)
                bleNotConnectedView()
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

    private val spDataParseEventListener by lazy {

        DataEventListener().apply {
            onCoreDataEventReceived = {
                //println("MainTimerCycleSeconds : ${it.MainTimerCycleSeconds}")

                val timeRemaining = it.MainTimerCycleSeconds
                heartProgressValue = timeRemaining.toDouble()

                safetyStatus = it.SafetyStatus
                awayStatus = it.AwayStatus
                runSwitchStatus = it.RunSwitch
                heightStatus = it.HeightSensorStatus
                calibrationMode = it.AlternateCalibrationMode
                alertnateATBMode = it.AlternateAITBMode
                isInteractive = it.UseInteractiveMode
                isDeskCurrentlyBooked = it.DeskCurrentlyBooked

                if (awayStatus) {
                    setAwayImageStatus()
                } else if (safetyStatus) {
                    setSafetyStatus()
                } else if (!heightStatus) {
                    checkHeightSensorStatus()
                } else if (calibrationMode) {
                    calibrationModeStatus()
                } else if (alertnateATBMode) {
                    automationModeStatus()
                } else if (isDeskCurrentlyBooked == true){
                    deskCurrentlyBooked()
                }

                //print("runswitch: ${it.RunSwitch}")

                if (it.RunSwitch == false) {
                    currentDeskMode = "Manual"
                }

                if (it.RunSwitch && it.UseInteractiveMode) {
                    currentDeskMode = "Interactive"
                }

                if (it.RunSwitch && !it.UseInteractiveMode) {
                    currentDeskMode = "Automatic"
                }

                if (mCurrentDeskMode !== currentDeskMode) {
                    mCurrentDeskMode = currentDeskMode
                    setDeskMode(currentDeskMode ?: "Manual")
                }

                if (heartProgressValue == 3600.0) {
                    addHeartProgress(1.0, true)
                }

                nextMovementTitleText(it.NextMove)

                val offset = it.TimesreportedVertPos
                val mtimeOffset = offset - it.MainTimerCycleSeconds

                if (safetyStatus || awayStatus || runSwitchStatus  || heightStatus || calibrationMode || alertnateATBMode) {


                    if (homeCountdown != null) {
                        var watch = StopWatch(mtimeOffset)
                        if (offset === -1) {

                            val timeFormat = String.format("%02d:00", abs(watch.minutes()))
                            homeCountdown.text = if (offset == 3)  "00:00" else timeFormat
                        } else {
                            val timeFormat = String.format("%02d:%02d", abs(watch.minutes()), abs(watch.seconds()))
                            //homeCountdown.text = if (offset == 3)  "00:00" else "${abs(watch.minutes())}:${abs(watch.seconds())}"
                            homeCountdown.text = if (offset == 3)  "00:00" else timeFormat
                        }
                    }

                } else {
                    if (offset == 3) {
                        val mlastMove = verticalMovementProfile.movements.last()
                            if (mlastMove != null) {
                                val mNewTime = 3599 - it.MainTimerCycleSeconds
                                updateCountdownTimer(mNewTime)
                            } else {
                                updateCountdownTimer(if (offset == 3) 0 else mtimeOffset)
                            }
                    } else {
                        updateCountdownTimer(if (offset == 3)  0 else mtimeOffset)
                    }
                }

                //PENDING MOVEMENT AND COMMISSIONING
                val _pendinMove = it.PendingMove
                pendingMovementCode = _pendinMove
                commissioningFlag = it.CommissioningFlag
                isInteractivePopUpShowing = (_pendinMove != 0 &&  isInteractive)

                if (!commissioningFlag) {
                    setCommissioningFlag()
                }

                if(_pendinMove != 0 &&  isInteractive) {
                    if (isInteractivePopUpShowing != InteractivePopUpShowState) {
                        InteractivePopUpShowState = true
                        pulseMainActivity.showInteractiveMovePopUp(_pendinMove)
                    }


                } else {
//                    if self?.interactivePopUpShowed ?? false {
//                        self?.interactivePopUpShowed = false
//                        self?.interactivePopUp.dismiss(animated: true, completion: nil)
//                    }

                    InteractivePopUpShowState = false
                }

                activity?.runOnUiThread {
                    //println("MainTimerCycleSeconds:  ${it.MainTimerCycleSeconds.toDouble()}")

                    if (it.MainTimerCycleSeconds != 0) {
                        if (circular_progress != null && heartCircleProgressTimer != null) {
                            if (circular_progress.progress != it.MainTimerCycleSeconds.toDouble() &&
                                heartCircleProgressTimer.progress != it.MainTimerCycleSeconds.toDouble()
                            ) {

                                if (Utilities.hasProfileSync) {
                                    circular_progress.setProgress(it.MainTimerCycleSeconds.toDouble(), 3600.0)
                                    heartCircleProgressTimer.setProgress(it.MainTimerCycleSeconds.toDouble(), 3600.0)

                                    //circular_progress.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())
                                    //heartCircleProgressTimer.setCurrentProgress(it.MainTimerCycleSeconds.toDouble())
                                }

                            }
                        }
                    }
                }

            }

            onVerticalProfileDataEventReceived = {
                println("onVerticalProfileDataEventReceived : ${it.movements}")
                verticalMovementProfile = it

                activity?.runOnUiThread {
                    if (heartCircleProgressTimer != null) {
                        heartCircleProgressTimer.movements = it.movements
                        heartCircleProgressTimer.maxProgress = 3600.0
                        Utilities.hasProfileSync = true
                    }
                }

            }

            onDesktopAppHasPriority = {
                if (it) {
                    desktopAppHasPriority()
                }
            }

            onResumeNormalBLEResponse = {
                if (it) {
                    val mode  = UserPreference.prefs.read("desk_mode", "Manual")
                    setDeskMode(mode ?: "Manual")
                }
            }

            onNewBlePairAttemptResponse = {
                if (it) {
                    pulseMainActivity.userDisconnect()
                    pulseMainActivity.showToastView(getString(R.string.device_new_pair))
                }
            }

            onIdentifierDataEventReceived = {

                val loggedUser = Utilities.typeOfUserLogged()

                (loggedUser != CURRENT_LOGGED_USER.Cloud).guard {  }

                val email = Utilities.getLoggedEmail()

                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {

                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                pulseMainActivity.updateDeviceConnectStatus(email, it.SerialNumber, it.RegistrationID, true)
                            },
                            1000 // value in milliseconds
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_heart_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        SPBleManager.registerListener(connectionEventListener)
        SPDataParser.registerListener(spDataParseEventListener)
        initilizeUI()
        getBoxData()

    }

    private fun initilizeUI() {

        heartProgressView()
        circularProgressView()
        //bleNotConnectedView()

        heartStatsTotalContainer.setOnClickListener {
            //this.pulseMainActivity.syncProfileSettings()
            runBlocking {
                //SPDataParser.deskViewModel.checkDeskBookingInformation()
                val deskInfoVM = DeskInfoViewModel()
                deskInfoVM.testPingPacket()
            }
        }

        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            val mode  = UserPreference.prefs.read("desk_mode", "Manual")
            setDeskMode(mode ?: "Manual")
        } else {
            bleNotConnectedView()
        }

        homeDeskModeContainer.setOnClickListener{
            SPAppEventHandler.showTabSelected(3)
        }

        heartProgressLayout.setOnClickListener{
            println("heart view clicked")
//            val action = LetterListFragmentDirections.actionLetterListFragmentToWordListFragment(letter = holder.button.text.toString())
//            // Navigate using that action
//            holder.view.findNavController().navigate(action)

            val action = HomeHeartFragmentDirections.actionHomeHeartFragmentToHeartAccumulationFragment2()
            it.findNavController().navigate(action)
        }

    }

    fun getBoxData() {
        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                SPRequestParameters.Profile )
        }
    }

    fun heartDetailsOnclickListener(details: Boolean) {
        if (details) {
            heartProgressLayout.setOnClickListener{
                println("heart view clicked")
                val action = HomeHeartFragmentDirections.actionHomeHeartFragmentToHeartAccumulationFragment2()
                it.findNavController().navigate(action)
            }
            circularImageView.setOnClickListener(null)
        } else {
            heartProgressLayout.setOnClickListener(null)
            circularImageView.setOnClickListener{
                println("acknowledge command")
               this.acknowledgeStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                val mode  = UserPreference.prefs.read("desk_mode", "Manual")
                setDeskMode(mode ?: "Manual")
            } else {
                bleNotConnectedView()
            }
        } else {
            bleNotConnectedView()
        }

        //homeHeartNavigationControl()
        requestUserInfo()
        fragmentNavTitle("Home", true, false)

    }

    fun homeHeartNavigationControl() {
        if (pulseMainActivity != null) {
            this.pulseMainActivity.navButtonToggle("Home", deviceConnected, true, false)
            val bleIcon = FontDrawable(appContext, R.string.fa_bluetooth_b, false, true)
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                bleIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_green))
            } else {
                bleIcon.setTextColor(ContextCompat.getColor(appContext, R.color.smartpods_gray))
            }

            if (pulseMainActivity.btnBluetooth != null) {
                this.pulseMainActivity.btnBluetooth.setImageDrawable(bleIcon)
            }
        }
    }

    fun requestUserInfo() {

        if (view != null) else return

        val email = Utilities.getLoggedEmail()

//        val appState = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEAPPSTATE)
//
//        if (appState != null) {
//            val mAppState = appState as PulseAppStates
//            InteractivePopUpShowState = mAppState.InteractivePopUpShowed
//        }


        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
                val hasNetwork = appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork) {
                    //request user information from cloud
                    viewModel.requestUserDetails(email).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)

                        if (it.GenericResponse.Success) {
                            if (it != null) {
                                updateUI(it)
                            }
                        } else {
                            errorResponse(it.GenericResponse, email)
                        }
                    })

                } else {
                    //fetch locally saved profile settings
                    val mUser = viewModel.getUserLocally(email)
                    val mUserObj = UserObject(Utilities.toJSONParameters(mUser))
                    updateUI(mUserObj)
                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { spEventHandler ->
            pulseMainActivity.showActivityLoader(false)
            spEventHandler.getContentIfNotHandled()?.let {
                toast(it)
            }
        })
    }

    fun updateUI(obj: UserObject) {
        heartStatsDailyCount.text = obj.Hearts.Today.format(0) //String.format("%.0f", obj.Hearts.Today)
        heartStatsTotalCount.text = obj.Hearts.Total.toString()
    }

    private fun circularProgressView() {

        circularbackground.setDotWidthDp(500)
        circularbackground.maxProgress = 100.0
        circularbackground.setShouldDrawDot(false)
        circularbackground.isAnimationEnabled = false
        circularbackground.setShouldDrawDot(false)
        circularbackground.setDotWidthDp(0)
        circularbackground.isFillBackgroundEnabled = true
        circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_white_text)
        circularbackground.alpha = 0.0F
        circularbackground.setProgressStrokeWidthDp(0)
        //circularbackground.setProgress(100.0, 100.0)

        //Heart Progress Timer with knob
        circular_progress.maxProgress = 3600.0
        circular_progress.setShouldDrawDot(true)
        circular_progress.isAnimationEnabled = true
        circular_progress.progressColor = Utilities.getCustomRawColor(R.color.smartpods_green)
        circular_progress.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_blue)
        circular_progress.dotColor = Utilities.getCustomRawColor(R.color.smartpods_green)
        circular_progress.setShouldDrawDot(true)
        circular_progress.setDotWidthDp(10)
        circular_progress.progressStrokeCap = CircularProgressIndicator.CAP_ROUND
        circular_progress.setProgress(0.0, 3600.0)

        heartCircleProgressTimer.isAnimationEnabled = false

        //Sit/Stand Progress
//        val sampleMap: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to 900,"start" to 0, "key" to 7, "value" to 3),
//            hashMapOf("sweep" to 1800,"start" to 900,"key" to 4, "value" to 900),
//            hashMapOf("sweep" to 2700,"start" to 1800,"key" to 7, "value" to 1800),
//            hashMapOf("sweep" to 0,"start" to 2700,"key" to 4, "value" to 2700))

        //[{key=7, value=3, start=0, end=900},
        // {key=4, value=900, start=900, end=1800},
        // {key=7, value=1800, start=1800, end=2700},
        // {key=4, value=2700, start=2700, end=0}]

//        val sampleMap: List<HashMap<String, Int>> = arrayOf(hashMapOf("angle" to 45,"start" to 270, "key" to 7, "value" to 3),
//            hashMapOf("angle" to 135,"start" to 315,"key" to 4, "value" to 900),
//            hashMapOf("angle" to 90,"start" to 90,"key" to 7, "value" to 1800),
//            hashMapOf("angle" to 90,"start" to 180,"key" to 4, "value" to 2700))

//        val sampleMap: List<HashMap<String, Int>> = listOf(hashMapOf("end" to 135,"start" to 270, "key" to 7, "value" to 3),
//            hashMapOf("sweep" to 22,"start" to 0,"key" to 4, "value" to 900),
//            hashMapOf("sweep" to 90,"start" to 90,"key" to 7, "value" to 1800),
//            hashMapOf("sweep" to 90,"start" to 180,"key" to 4, "value" to 2700))

//        val sampleMap: List<HashMap<String, Int>> = listOf(hashMapOf("sweep" to 45,"start" to 270, "key" to 7, "value" to 3),
//            hashMapOf("sweep" to 90,"start" to 0,"key" to 4, "value" to 900))
//
//        heartCircleProgressTimer.movements = sampleMap
//        heartCircleProgressTimer.maxProgress = 3600.0
        //heartCircleProgressTimer.setProgress(1000.0, 3600.0)

        var progress = 0

//        countDownTimer = object  : CountDownTimer(3600,1){
//            override fun onFinish() {
//                println("countDownTimer finish")
//            }
//
//            override fun onTick(millisUntilFinished: Long) {
//                println("millisUntilFinished : $millisUntilFinished")
//                progress += 1
//                if (circular_progress !== null) {
//                    circular_progress.setProgress(progress.toDouble(), 3600.0)
//                }
//                if (heartCircleProgressTimer !== null) {
//                    heartCircleProgressTimer.setProgress(progress.toDouble(), 3600.0)
//                }
//            }
//        }
//
//        countDownTimer!!.start()


    }

    private fun heartProgressView() {
        val displayMetrics = DisplayMetrics()
        val windowsManager = appContext.getSystemService(WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)
        var width = displayMetrics.widthPixels
        var height = displayMetrics.heightPixels
        println("screen size: $width | $height")

        heartAnimated.setMax(100)
        heartAnimated.setShape(CustomShapeProgressView.Shape.HEART)
        heartAnimated.setFrontWaveColor(Utilities.getCustomRawColor(R.color.smartpods_bluish_white))
        heartAnimated.setBehindWaveColor(Utilities.getCustomRawColor(R.color.smartpods_gray))
        heartAnimated.setBorderColor(Utilities.getCustomRawColor(R.color.smartpods_blue))
        heartAnimated.setAnimationSpeed(100)
        heartAnimated.setWaveOffset(heartProgress)
        heartAnimated.setWaveStrong(20)
        heartAnimated.setHideText(true)
        heartAnimated.startAnimation()
        heartAnimated.mProgress = 0
    }

    fun nextMovementTitleText(movement: Int) {

        if (homeNextMove != null) {
            if (movement == MovementType.DOWN.rawValue) {
                homeNextMove.text = "Sit"
            } else {
                homeNextMove.text = "Stand"
            }
        }
    }

    fun updateCountdownTimer(timeOffset: Int) {
        val watch = StopWatch(timeOffset)
        if (homeCountdown != null) {
            if (timeOffset == 3599) {
                homeCountdown.text = "00:00"
            } else {
                homeCountdown.text = "${abs(watch.minutes())}:${abs(watch.seconds())}"
            }
        }


    }

    fun addHeartProgress(value: Double, alertStatus: Boolean) {

        if (pulseMainActivity.SPBleDevice.isConnected()) {
            heartAnimated.mProgress += value.toInt()

        if (heartAnimated?.mProgress?.toDouble() ?: 0.0 >= 100.0) {
            if (alertStatus) {
                pulseMainActivity.showALertDialog("Congratulations!",
                    "You have added progress in your heart, keep up the good work.", false, "OK", null, null, null)
            }
                dailyHeartsTotal += 1
                dailyHearts = 0.0
                heartAnimated?.mProgress = 0
                refreshHeartsDailyAndTotal(dailyHearts, dailyHeartsTotal)
            }
        }
    }

    fun refreshHeartsDailyAndTotal(today: Double, total: Double) {
        heartStatsTotalCount?.text = total.toInt().toString()
        heartStatsDailyCount?.text = today.toInt().toString()
    }

    fun setDeskMode(mode: String) {

        if (mode == "Automatic") {
            if (circular_progress != null ||
                heartCircleProgressTimer != null ||
                circularImageView != null ||
                manualModeNotifier != null ||
                heartAnimated != null ||
                circularbackground != null || imgHomeDeskMode != null
            ) {

                activity?.runOnUiThread {
                    imgHomeDeskMode.setImageResource(R.drawable.automatic)
                    circular_progress.show()
                    heartCircleProgressTimer.show()
                    heartAnimated.show()
                    heartAnimated.mProgress = 0
                    circularbackground.progressBackgroundColor =
                        Utilities.getCustomRawColor(R.color.smartpods_white_text)
                    circularbackground.alpha = 1.0F
                    circular_progress.alpha = 1.0F
                    circularImageView.hide()
                    manualModeNotifier.hide()

                }

            }

        }

        if (mode == "Interactive") {
            if (circular_progress != null ||
                heartCircleProgressTimer != null ||
                circularImageView != null ||
                manualModeNotifier != null ||
                heartAnimated != null ||
                circularbackground != null || imgHomeDeskMode != null
            ) {

                activity?.runOnUiThread {
                    imgHomeDeskMode.setImageResource(R.drawable.interactive)
                    circular_progress.show()
                    heartCircleProgressTimer.show()
                    heartAnimated.show()
                    heartAnimated.mProgress = 0
                    circularbackground.progressBackgroundColor =
                        Utilities.getCustomRawColor(R.color.smartpods_white_text)
                    circularbackground.alpha = 1.0F
                    circular_progress.alpha = 1.0F
                    circularImageView.hide()
                    manualModeNotifier.hide()

                }

            }
        }

        if (mode == "Manual") {
            if (circular_progress != null ||
                heartCircleProgressTimer != null ||
                circularImageView != null ||
                manualModeNotifier != null ||
                heartAnimated != null ||
                circularbackground != null || imgHomeDeskMode != null
            ) {


                activity?.runOnUiThread {

                    if (circularImageView != null) { circularImageView.show() }
                    if (manualModeNotifier != null) {
                        manualModeNotifier.show()
                        manualModeNotifier?.isSelected = true
                        manualModeNotifier?.text =
                            "We’ve noticed that your desk has been inactive for too long. To achieve the most benefit from your Smartpods workstation, we recommend you move at least twice each hour."
                        manualModeNotifier.ellipsize = TextUtils.TruncateAt.MARQUEE
                    }
                    if (imgHomeDeskMode != null) { imgHomeDeskMode.setImageResource(R.drawable.manual) }
                    if (circularImageView != null) { circularImageView.setImageResource(R.drawable.manual_mode) }

                    if (circularbackground != null) {
                        circularbackground.progressBackgroundColor =
                            Utilities.getCustomRawColor(R.color.smartpods_blue)
                        circularbackground.alpha = 1.0F
                    }

                    if (circular_progress != null) {
                        circular_progress.alpha = 1.0F
                        circular_progress.hide()
                    }

                    if (heartCircleProgressTimer != null) {
                        heartCircleProgressTimer.hide()
                    }

                    if (heartAnimated != null) {
                        heartAnimated.hide()
                    }


                }
            }

        }
    }

    fun acknowledgeStatus() {
        heartDetailsOnclickListener(true)
        val acknowledgeStatus = command.GetAknowledgeSafetyCommand()
        this.pulseMainActivity.sendCommand(acknowledgeStatus, "GetAknowledgeSafetyCommand")
        this.enableHeartView(true, "")

    }

    fun enableHeartView(show: Boolean, message: String) {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {

            activity?.runOnUiThread {
                manualModeNotifier?.text = message
                if (show) {
                    manualModeNotifier.hide()
                    circularImageView.hide()
                    circularImageView.setImageResource(0)
                    circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_white_text)
                    heartAnimated.show()
                    heartCircleProgressTimer.show()
                    circular_progress.show()
                } else {
                    manualModeNotifier.show()
                    circularImageView.show()

                    heartAnimated.hide()
                    heartCircleProgressTimer.hide()
                    circular_progress.hide()
                }
            }

        }
    }

    fun bleNotConnectedView() {

        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {

            activity?.runOnUiThread {
                circularImageView.show()
                manualModeNotifier.show()

                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "Device not connected"

                circularImageView.setImageResource(R.drawable.ble_not_connected)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_gray)
                circularbackground.alpha = 1.0F
                circular_progress.alpha = 0.0F

                circular_progress.hide()
                heartCircleProgressTimer.hide()
                heartAnimated.hide()
            }

        }

    }

    fun desktopAppHasPriority() {

        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "Desktop app has priority"


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.desktop)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_blue)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()
            }

        }
    }

    fun setAwayImageStatus() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "User Not Detected"


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.user_not_detected)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_purple)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(false)
            }

        }
    }

    fun setSafetyStatus() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = ""


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.safety_triggered)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_yellow)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(false)
            }

        }
    }

    fun checkHeightSensorStatus() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "Your height sensor is disconnected"


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.height_sensor_disconnected)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_red)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(false)
            }

        }
    }

    fun automationModeStatus() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "Automation-in-a-Box Mode"


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.automation)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.green)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(false)
            }

        }
    }

    fun calibrationModeStatus() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "Auto-Calibration Mode"


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.calibration_mode)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_orange)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(false)
            }

        }
    }

    fun setCommissioningFlag() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = ""


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.commissioning_required)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_red)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(false)
            }

        }
    }

    fun deskCurrentlyBooked() {
        if (circular_progress != null ||
            heartCircleProgressTimer != null ||
            circularImageView != null ||
            manualModeNotifier != null ||
            heartAnimated != null ||
            circularbackground != null ||
            imgHomeDeskMode != null) {


            activity?.runOnUiThread {
                manualModeNotifier.show()
                manualModeNotifier.ellipsize = null
                manualModeNotifier?.isSelected = false
                manualModeNotifier?.text = "Desk currently booked."


                circularImageView.show()
                circularImageView.setImageResource(R.drawable.desk_vacant)
                circularbackground.progressBackgroundColor = Utilities.getCustomRawColor(R.color.smartpods_blue)
                circularbackground.alpha = 1.0F

                heartAnimated.hide()
                heartCircleProgressTimer.hide()
                circular_progress.hide()

                heartDetailsOnclickListener(true)
            }

        }
    }
}