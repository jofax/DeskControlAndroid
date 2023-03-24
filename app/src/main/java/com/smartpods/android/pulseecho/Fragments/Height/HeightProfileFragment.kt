package com.smartpods.android.pulseecho.Fragments.Height

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.MovementType
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.Utilities.SPTimer
import com.smartpods.android.pulseecho.ViewModel.HeightProfileViewModel
import kotlinx.android.synthetic.main.height_profile_fragment.*
import kotlinx.coroutines.*
import com.smartpods.android.pulseecho.Utilities.Utilities
import io.realm.Realm.getApplicationContext
import java.io.IOException
import java.lang.Math.abs
import androidx.lifecycle.Observer
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.PulseUserProfile
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.Utilities.SPRealmHelper
import com.smartpods.android.pulseecho.Utilities.guard
import org.jetbrains.anko.support.v4.toast

class HeightProfileFragment : BaseFragment() {

    var currentHeight: Int = 0
    var movesReported: Int = 0
    var standHeight: Int = 0
    var sittingHeight: Int = 0

    var standCommandPressCount: Int = 0
    var sitCommandPressCount: Int = 0
    var stopCommandPressCount: Int = 0

    var userSitHeight: Int = 0
    var userStandHeight: Int = 0

    var sittingHeightTruncated: Boolean = false
    var standHeightTruncated: Boolean = false

    var writeCommandSequence = SPTimer()
    private var desk_movement = MovementType.INVALID
    val handler = Handler(Looper.getMainLooper())
    var commandSequence: Boolean = false

    val runnableCode = object : Runnable {
        override fun run() {
            commandSequence = true
            Log.d("Handlers", "Called on main thread")
            println("desk movement type ${desk_movement}")
            handler.postDelayed(this, 250)

            if (desk_movement === MovementType.UP) {
                moveDeskUp()
            }

            if (desk_movement === MovementType.DOWN) {
                moveDeskDown()
            }
        }
    }


    private val spDataParseEventListener by lazy {
        var progress = 0
        DataEventListener().apply {
            onCoreDataEventReceived = {
                //println("HeightProfileFragment onCoreDataEventReceived : $it")

                if (currentHeightTitle != null) {
                    currentHeightTitle.text = it.ReportedVertPos.toString()
                }

                currentHeight = it.ReportedVertPos
                movesReported = it.NextMove

                //println("StandHeightAdjusted : ${it.StandHeightAdjusted}")
                //println("SitHeightAdjusted : ${it.SitHeightAdjusted}")

                if (sittingHeightTruncated != it.SitHeightAdjusted) {
                    sittingHeightTruncated = it.SitHeightAdjusted
                }

                if (standHeightTruncated != it.StandHeightAdjusted) {
                    standHeightTruncated = it.StandHeightAdjusted
                }

            }

            onVerticalProfileDataEventReceived = {

                standHeight = it.StandingPos
                sittingHeight = it.SittingPos

                println("HeightProfileFragment onVerticalProfileDataEventReceived stand: ${it.StandingPos} | sit: ${it.SittingPos}")
            }
        }
    }

    companion object {
        fun newInstance() = HeightProfileFragment()
    }

    private lateinit var viewModel: HeightProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.height_profile_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HeightProfileViewModel::class.java)
        // TODO: Use the ViewModel

        SPDataParser.registerListener(spDataParseEventListener)
        setViewActions()

    }

    override fun onResume() {
        super.onResume()
        fragmentNavTitle("Height Settings", true, false)
        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            getBoxData(true)
        } else {
            requestProfileSettings()
        }
    }

    override fun onStart() {
        super.onStart()

    }


    @SuppressLint("ClickableViewAccessibility")
    fun setViewActions() {
        imageUpArrow.setOnTouchListener { v, event ->
            println("imageUpArrow event: ${event.action}")
            desk_movement = MovementType.UP
            when (event?.action) {
                MotionEvent.ACTION_MOVE ->  {
                    if (!commandSequence) {
                        println("RUN COMMAND SEQUENCE")
                        imageUpArrow.setImageResource(R.drawable.up_desk_clickk)
                        imageUpArrow.backgroundTintList =
                            //getApplicationContext()?.let { ContextCompat.getColorStateList(it, R.color.smartpods_green) };
                            appContext.getColorStateList(R.color.smartpods_green)
                        handler.post(runnableCode)
                    }
                }

                MotionEvent.ACTION_UP ->  {
                    print("BUTTON PRESSED REMOVED")
                    stopDeskMovement()
                    imageUpArrow.setImageResource(R.drawable.up_desk)
                    commandSequence = false
                    desk_movement = MovementType.INVALID
                    handler.removeCallbacks(runnableCode)
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        imageDownArrow.setOnTouchListener { v, event ->
            println("imageUpArrow event: ${event.action}")
            desk_movement = MovementType.DOWN
            when (event?.action) {
                MotionEvent.ACTION_MOVE ->  {
                    if (!commandSequence) {
                        println("RUN COMMAND SEQUENCE")
                        imageDownArrow.setImageResource(R.drawable.down_desk_click)
                        handler.post(runnableCode)
                    }

                }

                MotionEvent.ACTION_UP ->  {
                    print("BUTTON PRESSED REMOVED")
                    stopDeskMovement()
                    imageDownArrow.setImageResource(R.drawable.down_desk)
                    commandSequence = false
                    desk_movement = MovementType.INVALID
                    handler.removeCallbacks(runnableCode)
                }
            }

            v?.onTouchEvent(event) ?: true
        }

        btnSetStand.setOnClickListener{
            updateStandingPosition()
        }

        btnSetSit.setOnClickListener{
            updateSittingPosition()
        }
    }

    private fun requestProfileSettings() {

        //(isAdded && isVisible).guard { println("requestProfileSettings should not be called") }

        if (view != null) else return
        println("requestProfileSettings called")
        val email = Utilities.getLoggedEmail()
        try {
            if (!email.isEmpty()) {
                pulseMainActivity.showActivityLoader(true)
               val hasNetwork =  appContext.let { this.pulseMainActivity.isNetworkAvailable(it) }
                println("hasNetwork : $hasNetwork")

                if (hasNetwork == true) {
                    //request updated profile settigs from cloud
                    viewModel.requestProfileSettings(email).observe(viewLifecycleOwner, Observer {
                        pulseMainActivity.showActivityLoader(false)
                        if (it.GenericResponse.Success) {
                            println("PROFILE SETTINGS RESPONSE: $it")
                            if (it.Settings != null) {
                                println("PROFILE SETTINGS DATA : ${it.Settings}")
                                    txtStandingValue.text = it.StandingPosition.toString()
                                    txtSittingValue.text = it.SittingPosition.toString()

                                    userSitHeight = it.SittingPosition
                                    userStandHeight = it.StandingPosition
                                    syncUserHeightPreference()
                            }
                        } else {
                            if (it.GenericResponse.Message == "Unauthorized") {
                                println("redirect to login")
                                pulseMainActivity.redirectToLoginPage(email)
                            }
                        }
                    })

                } else {
                    //fetch locally saved profile settings

                    val profile = viewModel.getProfileSettings(email)
                    if (profile != null) {
                        userSitHeight = profile.SittingPosition
                        userStandHeight = profile.StandingPosition
                        syncUserHeightPreference()
                    }

                }
            }
        } catch (e: IOException) {
            e.message?.let { pulseMainActivity.fail(it) }
        }
    }

    fun getBoxData(syncProfile: Boolean) {

        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                    SPRequestParameters.Profile )

                if (syncProfile) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        requestProfileSettings()
                    }, 2000)
                }
            }
        }
    }

    fun syncUserHeightPreference() {
        val updateSittingHeight = (userSitHeight != currentHeight) && sittingHeightTruncated
        val updateStandHeight = (userStandHeight != currentHeight) && standHeightTruncated
        val email = Utilities.getLoggedEmail()

        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            val sitCommandPacket = command.GetSetDownCommand(userSitHeight.toDouble())
            val standCommandPacket = command.GetSetTopCommand(userStandHeight.toDouble())

            if (!Utilities.getUserNotifiedStatus("Height")) {

                if (updateSittingHeight) {
                    //pulseMainActivity.sendCommand(sitCommandPacket, "GetSetDownCommand")
                }

                if (updateStandHeight) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        //pulseMainActivity.sendCommand(standCommandPacket, "GetSetTopCommand")
                    }, 500)
                }

            }

            if (updateSittingHeight || updateStandHeight) {
                txtStandingValue.text = standHeight.toString()
                txtSittingValue.text = sittingHeight.toString()

                fun okAction() = DialogInterface.OnClickListener { dialog, _ ->
                    SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf(
                        "IsNotifiedForHeightAdjustments" to true), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)
                    dialog.dismiss()

                }

                if (!Utilities.getUserNotifiedStatus("Height")) {
                    pulseMainActivity.showALertDialog(getString(R.string.information),
                        getString(R.string.sit_stand_adjusted),
                        true, getString(R.string.btn_ok), okAction())
                }
            }
        }
    }

    fun moveDeskUp() {

        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                val commandPacket = command.GetMoveUpCommand()
                pulseMainActivity.sendCommand(commandPacket, "GetMoveUpCommand")
            }
        } else {
            toast(R.string.device_not_connected)
        }
    }

    fun moveDeskDown() {
        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                val commandPacket = command.GetMoveDownCommand()
                pulseMainActivity.sendCommand(commandPacket, "GetMoveDownCommand")
            }
        } else {
            toast(R.string.device_not_connected)
        }
    }

    fun stopDeskMovement() {

        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                val commandPacket = command.GetStopCommand()
                pulseMainActivity.sendCommand(commandPacket, "GetStopCommand")
                desk_movement = MovementType.INVALID
                writeCommandSequence.cancelTimer()
            }
        }
    }

    fun checkForTruncatedSitAndStandHeights(tag: Int) {
        /*val updateSittingHeight = (PulseDataState.instance.sittingHeight != PulseDataState.instance.currentHeight) && PulseDataState.instance.sittingHeightTruncated
        val updateStandHeight = (PulseDataState.instance.standHeight != PulseDataState.instance.currentHeight) && PulseDataState.instance.standHeightTruncated
        if(updateStandHeight || updateSittingHeight) {
        }

        if (tag == 0 && PulseDataState.instance.sittingHeightTruncated) {
            self.showAlert(title: "generic.notice".localize(), message: "deskHeight.sit_height_adjusted".localize())
        } else if (tag == 1 && PulseDataState.instance.standHeightTruncated)  {
            self.showAlert(title: "generic.notice".localize(), message: "deskHeight.stand_height_adjusted".localize())
        }*/
    }

    fun updateSittingPosition() {
        val newSitOffset = currentHeight
        val currentStand = standHeight
        val diff = kotlin.math.abs(currentStand - newSitOffset)

        if (currentStand < newSitOffset) {

            fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            }

            pulseMainActivity.showALertDialog(getString(R.string.information),
                getString(R.string.set_sitting_error),
                true, getString(R.string.btn_ok), okAction())

            return
        }

        if (diff < Constants.sitStandDifference * 10) {
            val _difference = Constants.sitStandDifference * 10
            val message = "The difference between sitting and standing must be greater than $_difference (MM)"

            fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            }


            pulseMainActivity.showALertDialog(getString(R.string.information),
                message,
                true, getString(R.string.btn_ok), okAction())

            return
        }

        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            val commandPacket = command.GetSetDownCommand(newSitOffset.toDouble())
            pulseMainActivity.sendCommand(commandPacket, "GetSetDownCommand")
        }

        print("sitting truncated: $sittingHeightTruncated")
        print("sitting newSitOffset: $newSitOffset")

        Handler(Looper.getMainLooper()).postDelayed(
            {
//                if (sittingHeightTruncated) {
//                    checkForTruncatedSitAndStandHeights(0)
//
//                    Handler(Looper.getMainLooper()).postDelayed(
//                        {
//                            getBoxData(false)
//                            syncronizeSittingHeight(sittingHeight)
//                            syncronizeStandingHeight(standHeight)
//                        },
//                        500 // value in milliseconds
//                    )
//                } else {
//                    syncronizeSittingHeight(newSitOffset)
//                }

                getBoxData(false)
                syncronizeSittingHeight(newSitOffset)
            },
            1000 // value in milliseconds
        )
    }

    private fun syncronizeSittingHeight(height: Int) {
        val email = Utilities.getLoggedEmail()

        val mData: HashMap<String, Any> = hashMapOf("SittingPosition" to height)
        SPRealmHelper.saveOrUpdateObjectWithData(mData, email, REALM_OBJECT_TYPE.PULSEPROFILE)

        if (!sittingHeightTruncated) {
            txtSittingValue.text = height.toString()
        }

        val loggedUser = Utilities.typeOfUserLogged()
        (loggedUser != CURRENT_LOGGED_USER.Cloud).guard {
            println("locally syncronizeStandinggHeight called")
        }

        var mProfile: HashMap<String, Any> = hashMapOf()
        val profile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE)

        if (profile != null) {
            mProfile = (profile as PulseUserProfile).getProfileParameters(mProfile)
            println("cloud syncronizeStandinggHeight called")
            viewModel.requestUpdateProfileSettings(email, mProfile)
        }

        SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf(
            "IsNotifiedForHeightAdjustments" to false), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)

    }

    fun updateStandingPosition() {
        val newStandOffset = currentHeight
        val currentSit = sittingHeight
        val diff = abs(newStandOffset - currentSit)


        print("(self.currentHeight < standHeight) ${currentHeight < standHeight}")
        print("self.currentHeight > currentSit: ${currentHeight > currentSit}")
        print("updateStandingPosition: ${(currentHeight > currentSit) && (currentHeight < standHeight)}")

        if (currentHeight < currentSit) {
            fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            }


            pulseMainActivity.showALertDialog(getString(R.string.information),
                getString(R.string.set_standing_error),
                true, getString(R.string.btn_ok), okAction())

            return
        }

        if (diff < Constants.sitStandDifference * 10) {  //10 in mm
            val _difference = Constants.sitStandDifference * 10
            val message = "The difference between sitting and standing must be greater than $_difference (MM)"

            fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            }


            pulseMainActivity.showALertDialog(getString(R.string.information),
                message,
                true, getString(R.string.btn_ok), okAction())

            return
        }

        if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
            val commandPacket = command.GetSetTopCommand(newStandOffset.toDouble())
            pulseMainActivity.sendCommand(commandPacket, "GetSetTopCommand")
        }


        print("standing truncated: $standHeightTruncated")

        Handler(Looper.getMainLooper()).postDelayed(
            {
//                if (standHeightTruncated) {
//                    checkForTruncatedSitAndStandHeights(1)
//
//                    Handler(Looper.getMainLooper()).postDelayed(
//                        {
//                            getBoxData(false)
//                            syncronizeStandingHeight(standHeight)
//                            syncronizeSittingHeight(sittingHeight)
//
//                        },
//                        500 // value in milliseconds
//                    )
//                } else {
//                    syncronizeStandingHeight(newStandOffset)
//                }
                getBoxData(false)
                syncronizeStandingHeight(newStandOffset)
            },
            1000 // value in milliseconds
        )

    }

    fun syncronizeStandingHeight(height: Int) {
        val email = Utilities.getLoggedEmail()

        val mData: HashMap<String, Any> = hashMapOf("StandingPosition" to height)
        SPRealmHelper.saveOrUpdateObjectWithData(mData, email, REALM_OBJECT_TYPE.PULSEPROFILE)

        if (!standHeightTruncated) {
            txtStandingValue.text = height.toString()
        }

        val loggedUser = Utilities.typeOfUserLogged()
        (loggedUser != CURRENT_LOGGED_USER.Cloud).guard {
            println("locally syncronizeStandinggHeight called")
        }

        var mProfile: HashMap<String, Any> = hashMapOf()
        val profile = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.PULSEPROFILE)

        if (profile != null) {
            mProfile = (profile as PulseUserProfile).getProfileParameters(mProfile)
                println("cloud syncronizeStandinggHeight called")
            viewModel.requestUpdateProfileSettings(email, mProfile)
        }

        SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf(
            "IsNotifiedForHeightAdjustments" to false), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)

    }

}