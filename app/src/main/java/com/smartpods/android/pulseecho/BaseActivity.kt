package com.smartpods.android.pulseecho
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.smartpods.android.pulseecho.Activity.PulseLoginActivity
import com.smartpods.android.pulseecho.BLEScanner.DeviceListActivity
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.CustomUI.BMIDialogFragment
import com.smartpods.android.pulseecho.CustomUI.InteractivePopDialog
import com.smartpods.android.pulseecho.Model.*
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.Utilities.*
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleEventListener
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.byteArrayOfInts
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.BLE.toHexString
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.PulseCommands
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.ViewModel.DeskInfoViewModel
import com.smartpods.android.pulseecho.ViewModel.HeightProfileViewModel
import com.smartpods.android.pulseecho.ViewModel.UserProfileViewModel
import info.androidhive.fontawesome.FontDrawable
import kotlinx.android.synthetic.main.sp_with_menu_action_bar.*
import kotlinx.android.synthetic.main.user_profile_fragment.*
import org.jetbrains.anko.support.v4.toast
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

abstract class BaseActivity : AppCompatActivity(),  AdapterView.OnItemSelectedListener {
    private var currentApiVersion: Int = 0

    lateinit var SPBleDevice: BluetoothDevice

    private lateinit var screenTitle: TextView
    private lateinit var userScreenTitleText: TextView
    lateinit var btnBluetooth: ImageButton
    private lateinit var btnCloud: ImageButton
    private lateinit var btnLogout: ImageButton
    private lateinit var appStatusLoader: ProgressBar
    var deviceConnected: Boolean = false
    var command = PulseCommands()

    lateinit var navController: NavController

    private val characteristics by lazy {
        SPBleManager.servicesOnDevice(SPBleDevice)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    private var notifyingCharacteristics = mutableListOf<UUID>()

    private val connectionEventListener by lazy {
        SPBleEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                println("BaseActivity onConnectionSetupComplete bond state: " + gatt.device.bondState)
                deviceConnected = gatt.device.bondState == 12
                SPBleDevice = gatt.device
                //SPBleStatusIndicator(gatt.getConnectionState(gatt.device).toBo==)
                //print("Base Activity deviceConnected: $deviceConnected")
            }
            onDisconnect = {
                //println("BaseActivity onDisconnect: " + it)
            }

            onCharacteristicWrite = { device, gattChar ->
                //println("BaseActivity onCharacteristicWrite: " + device)
                if (gattChar.value != null) {
                    //println("BaseActivity onCharacteristicWrite: " + gattChar.value.toHexString())
                }
            }

            onCharacteristicRead = { device, desc ->
                //println("BaseActivity onCharacteristicRead: " + device)
                //println("BaseActivity onCharacteristicRead: " + desc)
            }

            onCharacteristicChanged = { device, gattChar ->
                //println("BaseActivity onCharacteristicChanged: " + device)
                if (gattChar.value != null) {
                    //println("BaseActivity onCharacteristicChanged: " + gattChar.value.toHexString())
                }
            }

            onBondComplete =  { device, obj ->
                //println("BaseActivity onBondComplete: " + device)
                //println("BaseActivity onBondComplete: " + obj)
                SPBleDevice = device
                deviceConnected = device.bondState == BluetoothDevice.BOND_BONDED

            }

            onPairedComplete =  { gatt ->
                //println("BaseActivity onPairedComplete: " + gatt)

            }

            onDeviceConnectivityError =  { device, message ->

                //println("BaseActivity onDeviceConnectivityError: Device - {$device} Message - {$message}")
            }

            onDeviceDataError =  { device, message ->
                //println("BaseActivity onDeviceDataError: Device - {$device} Message - {$message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SPBleManager.registerListener(connectionEventListener)
        SPBleManager.listenToBondStateChanges(this)

        initializeUI()
    }


    override fun onBackPressed() {
        //super.onBackPressed()
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun initializeUI() {




    }

    @SuppressLint("WrongConstant")
    fun setCustomActionBar(title: String, user: String, cloud: Boolean, back: Boolean, ble: Boolean) {
        println("setCustomActionBar : $title | $user | $cloud | $back | $ble")
        if (user.isEmpty()) {
            this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            supportActionBar!!.setDisplayShowCustomEnabled(true)
            supportActionBar!!.setCustomView(R.layout.sp_title_action_bar)
            supportActionBar!!.elevation = 0F

            appStatusLoader = findViewById(R.id.statusIndicator)
            screenTitle = findViewById(R.id.screenTitleText)

            screenTitle.text = title
            appStatusLoader.isVisible = false

        } else {
            this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            supportActionBar!!.setDisplayShowCustomEnabled(true)
            supportActionBar!!.setCustomView(R.layout.sp_with_menu_action_bar)
            supportActionBar!!.elevation = 0F

            appStatusLoader = findViewById(R.id.statusIndicator)
            userScreenTitleText = findViewById(R.id.userScreenTitleText)

            userScreenTitleText.text = title
            appStatusLoader.isVisible = false

            btnBluetooth = findViewById(R.id.btnBle)
            btnCloud = findViewById(R.id.btnCloud)
            btnLogout = findViewById(R.id.btnLogout)

            this.navButtonToggle(title, ble, true, back)
            this.setActionListeners()
        }
    }

    fun setViewTitle(title: String) {
        if (this::userScreenTitleText.isInitialized) {
            userScreenTitleText.text = title
        }
    }

    fun setActionListeners() {
        this.bleButtonAction()
        this.btnLogout.setOnClickListener{
            showRightDropDownMenu(it, R.menu.sp_right_menu)
        }

    }

    override fun onResume() {
        println("base activity is visible")

//        SPBleManager.listenToBondStateChanges(this).apply {
//            println("this is base activity listen to bond state $this")
//            fun onReceive(context: Context, intent: Intent) {
//                with(intent) {
//                    if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
//                        val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                        val previousBondState = getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
//                        val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
//
//                    }
//                }
//            }
//        }

        super.onResume()
        if (this::btnBluetooth.isInitialized) {
            btnBluetooth.isEnabled = true
        }
    }

    fun showRightDropDownMenu(v: View, @MenuRes menuRes: Int) {
        val rightMenuPopUp = PopupMenu(baseContext, v)
        rightMenuPopUp.menuInflater.inflate(menuRes, rightMenuPopUp.menu)

        val email = UserPreference.prefs.read(Constants.USER_EMAIL, "")
        if (email != null) {
            if (email.isNotEmpty()) {
                rightMenuPopUp.menu.add(0, 0, 0, email)
                rightMenuPopUp.menu.add(0, 1, 0, "Contact Support")
                rightMenuPopUp.menu.add(0, 2, 1, "Logout")
            } else {
                rightMenuPopUp.menu.add(0, 0, 0, "Contact Support")
            }
        } else {
            rightMenuPopUp.menu.add(0, 0, 0, "Contact Support")
        }

        rightMenuPopUp.setOnDismissListener {
            // Respond to popup being dismissed.
        }

        rightMenuPopUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->

            val email = UserPreference.prefs.read(Constants.USER_EMAIL, "")
            if (email != null) {
                if (email.isNotEmpty()) {
                    when (item.itemId) {
                        0 ->
                            println("email")
                        1 ->
                            this.redirectToSupport()
                        2 -> {
                            println("logout menu clicked")

                            redirectToLoginPage(email)
                        }

                    }
                } else {
                    when (item.itemId) {
                        0 -> this.redirectToSupport()
                    }
                }
            } else {

            }
            true
        })


        // Show the popup menu.
        rightMenuPopUp.show()
    }

    fun redirectToSupport() {
        //supportUrl
        val openURL = Intent(android.content.Intent.ACTION_VIEW)
        openURL.data = Uri.parse(Constants.supportUrl)
        startActivity(openURL)
    }

    fun bleButtonAction() {

        this.btnBluetooth.setOnClickListener{
            //btnBluetooth.isEnabled = false

            if (this::btnBluetooth.isInitialized) {
                //btnBluetooth.isEnabled = true
                if (this::SPBleDevice.isInitialized && SPBleDevice != null) {
                    if (SPBleDevice.isConnected()) {
                            userDisconnect()
                    } else {
                        showScanDeviceList()
                    }
                } else {
                    showScanDeviceList()
                }
            } else {
                showScanDeviceList()
            }
        }
    }

    fun userDisconnect() {
        Utilities.disconnectDevice(SPBleDevice)
        deviceConnected = false
        Utilities.hasProfileSync = false
        SPBleStatusIndicator(false, false)
    }

    fun showScanDeviceList() {
        val intent = Intent(this, DeviceListActivity::class.java)
        this.startActivity(intent)
    }

    fun redirectToLoginPage(email: String) {
        val serial = Utilities.getSerialNumber()
        Utilities.hasProfileSync = false
        UserPreference.prefs.remove(Constants.USER_EMAIL)
        UserPreference.prefs.remove(Constants.current_logged_user_type)
        UserPreference.prefs.remove("SerialNumber")
        UserPreference.prefs.remove("desk_mode")
        UserPreference.prefs.clear()
        //UserPreference.prefs.clear()
        //UserPreference.encryptedPrefs.clear()

        SPRealmHelper.removeObject(email, REALM_OBJECT_TYPE.USERMODEL)
        SPRealmHelper.removeObject(email, REALM_OBJECT_TYPE.SPCLIENTINFO)
        SPRealmHelper.removeObject(email, REALM_OBJECT_TYPE.PULSEPROFILE)
        SPRealmHelper.removeObject(email, REALM_OBJECT_TYPE.PULSEAPPSTATE)
        SPRealmHelper.removeObjectWithParam(email, REALM_OBJECT_TYPE.PULSEDEVICES,
            serial.toString()
        )
        SPRealmHelper.removeObjectWithParam(email, REALM_OBJECT_TYPE.PULSEDATAPUSH,
            serial.toString()
        )

        Utilities.desktopApphasPriority = false
        Utilities.isDeskCurrentlyBooked = false

        if (this::SPBleDevice.isInitialized) {
            SPBleManager.teardownConnection(SPBleDevice)
        }

        val intent = Intent(this, PulseLoginActivity::class.java)
        startActivity(intent)

        finish()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }


    fun SPBleStatusIndicator(connected: Boolean, backButton: Boolean) {
        val bleIcon = FontDrawable(this, R.string.fa_bluetooth_b, false, true)
        val backIcon = FontDrawable(this, R.string.fa_angle_left_solid, true, false)
        //backIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_blue))

        if (this::btnBluetooth.isInitialized) {

            if (this::SPBleDevice.isInitialized && SPBleDevice != null) {
                if (connected) {
                    bleIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_green))
                } else {
                    bleIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_gray))
                }
            } else {
                bleIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_gray))
            }

            btnBluetooth.setImageDrawable(bleIcon)
        }
    }

    fun navButtonToggle(title: String, bleConnected: Boolean, userLogged: Boolean, backButton: Boolean) {

        val bleIcon = FontDrawable(this, R.string.fa_bluetooth_b, false, true)
        val cloudIcon = FontDrawable(this, R.string.fa_cloud_solid, true, false)
        val elipseIcon = FontDrawable(this, R.string.fa_ellipsis_v_solid, true, false)
        elipseIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_green))
        bleIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_gray))

        if (userLogged) {
            cloudIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_green))
        } else {
            cloudIcon.setTextColor(ContextCompat.getColor(this, R.color.smartpods_gray))
        }

        if (this::userScreenTitleText.isInitialized) {
            userScreenTitleText.text = title
        }

        if (this::btnCloud.isInitialized) {
            btnCloud.setImageDrawable(cloudIcon)
            if (backButton) {
                btnCloud.hide()
            } else {
                btnCloud.show()
            }
        }

        if (this::btnLogout.isInitialized) {
            btnLogout.setImageDrawable(elipseIcon)
        }

        if (this::btnBluetooth.isInitialized) {
            btnBluetooth.setImageDrawable(bleIcon)
            SPBleStatusIndicator(bleConnected, backButton)
        }


    }

    fun backButtonPressed() {

    }

    fun mainNavigationControl(mTitle: String) {
        this.navButtonToggle(mTitle, deviceConnected, true, false)
        val bleIcon = FontDrawable(PulseApp.appContext, R.string.fa_bluetooth_b, false, true)
        if (this.deviceConnected &&  this.SPBleDevice.isConnected()) {
            bleIcon.setTextColor(ContextCompat.getColor(PulseApp.appContext, R.color.smartpods_green))
        } else {
            bleIcon.setTextColor(ContextCompat.getColor(PulseApp.appContext, R.color.smartpods_gray))
        }

        if (this::btnBluetooth.isInitialized) {
            this.btnBluetooth.setImageDrawable(bleIcon)
        }
    }

    fun Context.goToActivity(
        activity: Activity,
        extras: HashMap<String, Any>,
        cls: Class<*>?
    ) {
        val intent = Intent(activity, cls)
        intent.flags = FLAG_ACTIVITY_NEW_TASK

        for (item in extras) {
            //String value
            if (item.value is String) {
                intent.putExtra(item.key, (item.value as String))
            }
            //Integer value
            if (item.value is Int) {
                intent.putExtra(item.key, (item.value as Int))
            }
        }
        startActivity(intent)
    }

    fun Context.goToActivityWithResult(
        activity: Activity,
        tag: Int,
        extras: HashMap<String, Any>,
        cls: Class<*>?
    ) {
        val intent = Intent(activity, cls)

        for (item in extras) {
            //String value
            if (item.value is String) {
                intent.putExtra(item.key, (item.value as String))
            }
            //Integer value
            if (item.value is Int) {
                intent.putExtra(item.key, (item.value as Int))
            }
        }
        startActivityForResult(intent, tag)
    }


    fun  Context.showStatusLoader(context: Context = applicationContext, show: Boolean) {
        appStatusLoader.isVisible = show
    }

    fun Context.toast(
        context: Context = applicationContext,
        message: String,
        duration: Int = Toast.LENGTH_LONG
    ){
        Toast.makeText(context, message, duration).show()
    }

    fun showActivityLoader(show: Boolean) {
        if (this::appStatusLoader.isInitialized) {
            appStatusLoader.isVisible = show
        }
    }

    fun showToastView(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showALertDialog(
        mTitle: String,
        mMessage: String,
        mCancellable: Boolean,
        mPositiveTitle: String? = null,
        mPositiveOnclick: DialogInterface.OnClickListener? = null,
        mCancelTitle: String? = null,
        mCancelOnclick: DialogInterface.OnClickListener? = null
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(mTitle)
            builder.setMessage(mMessage)
            builder.setCancelable(mCancellable)

            if (mPositiveOnclick != null) {
                builder.setPositiveButton(mPositiveTitle, mPositiveOnclick)
            }
            if (mCancelOnclick != null) {
                builder.setNegativeButton(mCancelTitle, mCancelOnclick)
            }

            if (!(this is Activity && (this as Activity).isFinishing)) {
                builder.show()
            }
        }

    }

    fun stopDeskMovement() {
        if (::SPBleDevice.isInitialized  && SPBleDevice.isConnected()) {
            val commandPacket = command.GetStopCommand()
            sendCommand(commandPacket, "GetStopCommand")
        }

    }

    fun moveDeskUp() {
        if (::SPBleDevice.isInitialized && SPBleDevice.isConnected()) {
            val commandPacket = command.GetMoveStandingCommand()
            sendCommand(commandPacket, "GetMoveUpCommand")
        }
    }

    fun moveDeskDown() {

        if (::SPBleDevice.isInitialized &&  SPBleDevice.isConnected()) {
            val commandPacket = command.GetMoveSittingCommand()
            sendCommand(commandPacket, "GetMoveDownCommand")
        }
    }

    fun sendCommand(command: ByteArray, identifier: String) {
        if (::SPBleDevice.isInitialized &&  SPBleDevice.isConnected()) {
            println("send command : ${identifier} | payload: ${command}")

            if (Utilities.isDeskCurrentlyBooked) {
                showToastView(getString(R.string.desk_currently_booked))
            } else {
                SPBleManager.sendCommandToCharacteristic(
                    Utilities.getSmartpodsDevice(), command
                )
            }
        }
    }

    fun getBoxInformation() {
        val commandPacket = SPRequestParameters.GetAESKey
        sendCommand(commandPacket, "SPRequestParameters.GetAESKey")
    }

    fun deviceIsInitialized(): Boolean {
        println("deviceIsInitialized : ${this::SPBleDevice.isInitialized}")
        return this::SPBleDevice.isInitialized
    }

    fun updateDeviceConnectStatus(email: String,
                                  serial: String,
                                  registration: String,
                                  connected: Boolean) {

        val loggedUser = Utilities.typeOfUserLogged()

        (loggedUser != CURRENT_LOGGED_USER.Cloud).guard { return }

        if (connected) {
            val commandPacket = SPRequestParameters.GetAESKey
            sendCommand(commandPacket, "SPRequestParameters.GetAESKey")
        }

        val clientInfo = SPRealmHelper.getObject(email, REALM_OBJECT_TYPE.SPCLIENTINFO)

        if (clientInfo != null) else return

        (clientInfo !== null).guard { println("REALM_OBJECT_TYPE.SPCLIENTINFO is empty ") }

        var appState = clientInfo as SPClientInfo

        (appState != null).guard { return }

        var syncParams: HashMap<String, Any> = hashMapOf()
        syncParams = if (appState.OrgCode.isNotEmpty()) {
            hashMapOf("Connected" to connected,
                "OrgCode" to appState.OrgCode ,
                "SerialNumber" to serial,
                "RegistrationId" to registration)
        } else {
            hashMapOf("Connected" to connected,
                "SerialNumber" to serial,
                "RegistrationId" to registration)
        }

        val deskInfoViewModel = DeskInfoViewModel()
        deskInfoViewModel.deskConnectRequest(email, syncParams).observe(this, {
            when(it.GenericResponse.ResultCode) {
                0 -> {
                    val mCommand = command.DeskBookingInfo(it.BookingInfo)

                    if (deviceIsInitialized()) {
                        SPBleManager.sendCommandToCharacteristic(Utilities.getSmartpodsDevice(), mCommand)
                    }

                    if (appState.OrgCode.isEmpty()) {
                        //val userViewModel = UserProfileViewModel()
                        val heightProfileViewModel = HeightProfileViewModel()
                        val pulseToken = PulseToken(it.mJSON)

                        SPRealmHelper.saveOrUpdateObjectWithData(pulseToken.toHashParameters(), email, REALM_OBJECT_TYPE.SPCLIENTINFO)
                        //userViewModel.requestUserDetails(email).observe(this, {})
                        // Push the default profile
                        val mRequestParameters = Utilities.getDefaultActivityProfile(email)
                        heightProfileViewModel.requestUpdateProfileSettings(email, mRequestParameters).observe(this, {
                            val defaultProfile = command.GenerateVerticalProfile(Constants.defaultProfileSettingsMovement)
                            val setSit = command.GetSetDownCommand(Constants.defaultSittingPosition.toDouble())
                            val setStand = command.GetSetTopCommand(Constants.defaultStandingPosition.toDouble())

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    sendCommand(defaultProfile, "DefaultProfile")
                                },
                                500 // value in milliseconds
                            )

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    sendCommand(setSit, "GetSetDownCommand")
                                },
                                500 // value in milliseconds
                            )

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    sendCommand(setStand, "GetSetTopCommand")
                                },
                                500 // value in milliseconds
                            )

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        })
                    }
                }

                6 -> {

                    fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                        Utilities.saveOrUpdatePulseDevice(hashMapOf("State" to 0))
                        SPBleManager.teardownConnection(Utilities.getSmartpodsDevice())
                        redirectToLoginPage(email)
                        dialog.dismiss()
                    }
                    showALertDialog(getString(R.string.notice),
                        it.GenericResponse.Message,
                        false,
                        getString(R.string.btn_ok), okAction())

                }
            }
        })
    }

    fun syncProfileSettings() {
        val email = Utilities.getLoggedEmail()
        when(Utilities.typeOfUserLogged()) {
            CURRENT_LOGGED_USER.Guest -> {

            }

            CURRENT_LOGGED_USER.Cloud -> {
                val heightProfileViewModel = HeightProfileViewModel()
                val profileSettings = heightProfileViewModel.getProfileSettings(email)
                if (profileSettings != null) {
                    var profileObj = profileSettings.toJSONObject()
                    profileObj.put("Success", false)
                    profileObj.put("ResultCode", 0)
                    profileObj.put("Message", "")
                        if (::SPBleDevice.isInitialized &&  SPBleDevice.isConnected()) {
                            println("sync profile settings to local | cloud | box")
                            val mProfile = ProfileObject(profileObj)
                            val commandPacket = command.CreateVerticalProfile(mProfile)
                            sendCommand(commandPacket, "CreateVerticalProfile")

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    val standCommand = command.GetSetTopCommand(mProfile.StandingPosition.toDouble())
                                    sendCommand(standCommand, "GetSetTopCommand")
                                },
                                500 // value in milliseconds
                            )

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    val sitCommand = command.GetSetDownCommand(mProfile.SittingPosition.toDouble())
                                    sendCommand(sitCommand, "GetSetDownCommand")
                                },
                                500 // value in milliseconds
                            )

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    sendCommand(SPRequestParameters.Profile, "SPRequestParameters.Profile")
                                },
                                1000 // value in milliseconds
                            )
                        }
                }
            }

            CURRENT_LOGGED_USER.None -> {

            }

            CURRENT_LOGGED_USER.Local -> {

            }
        }

    }

    fun showInteractiveMovePopUp(movement: Int) {

        (movement != 0).guard { return }

        val nextMovement = if (movement == 4) "Stand" else "Sit"

        runOnUiThread {
            val dialog = InteractivePopDialog(nextMovement)
            val fragmentManager = this.supportFragmentManager
            if (!(this is Activity && (this as Activity).isFinishing)) {
                dialog.show(fragmentManager, "BMIDialogFragment")
            }
        }
    }

    fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    /**
     * Enables back button support. Simply navigates one element up on the stack.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /*******************************************
     * Spinner Right Menu functions
     *******************************************/

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }



    /*******************************************
     * Extension functions
     *******************************************/

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    fun Activity.isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun EditText.showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun toUnsignedInt(x: Byte): Int {
        return x.toInt() and 0xff
    }

    fun ByteArray.toHexString() : String {
        return this.joinToString("") {
            java.lang.String.format("%02x", it)
        }
    }
}
