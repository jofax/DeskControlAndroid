package com.smartpods.android.pulseecho
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.dariopellegrini.spike.response.Spike
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.UserPreference
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.guard
import org.jetbrains.anko.appcompatV7.BuildConfig
import timber.log.Timber
import io.realm.Realm

class PulseApp: Application(), LifecycleObserver, Application.ActivityLifecycleCallbacks {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate() {
        super.onCreate()
        PulseApp.appContext = applicationContext
        PulseApp.EchoApp = this
        appResources =  resources
        //UserPreference.initEncryptedPrefs(this.applicationContext)
        UserPreference.initPrefs(this.applicationContext)


//        val sharedPreferences: SharedPreferences = this.applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
//        val sharedPreferences2: SharedPreferences = this.applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
//
//        val editor = sharedPreferences.edit()
//        editor.clear()
//        editor.apply()
//
//        val editor2 = sharedPreferences2.edit()
//        editor2.clear()
//        editor2.apply()
//
//        editor.commit()
//        editor2.commit()


        Spike.instance.configure(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        //setApplicationKey()
        Realm.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        val email = UserPreference.prefs.read(Constants.USER_EMAIL, "")
        if (email != null) {
            if (email.isNotEmpty()) {
                val realmConfiguration =  Utilities.getRealmForUser(email, true)
                Realm.setDefaultConfiguration(realmConfiguration)
            }
        }
    }

    companion object {
        lateinit var appContext: Context
        lateinit var appResources: Resources
        private var EchoApp: PulseApp? = null

        fun getApp(): PulseApp? {
            return EchoApp
        }
    }

    private fun setApplicationKey() {
        //UserPreference.encryptedPrefs.write(Constants.APP_KEY_ACCESS, Constants.APP_KEY)
    }

    fun checkBLEDeviceConnection() {
        val bm = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val devices = bm.getConnectedDevices(BluetoothGatt.GATT)
        var status = -1
        for (device in devices)
        {
            status = bm.getConnectionState(device, BluetoothGatt.GATT)
            println("ble status: $status")
            // compare status to:
            // BluetoothProfile.STATE_CONNECTED
            // BluetoothProfile.STATE_CONNECTING
            // BluetoothProfile.STATE_DISCONNECTED
            // BluetoothProfile.STATE_DISCONNECTING
        }
    }

    fun reconnectDevice() {
        val email = UserPreference.prefs.read(Constants.USER_EMAIL, "")
        if (email != null) {
            if (email.isNotEmpty()) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        SPBleManager.reconnectSmartpodsWithDevice(this.bluetoothAdapter.getRemoteDevice(Constants.BLE_UUID))
                    },
                    5000 // value in milliseconds
                )
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        //App in background
        SPBleManager.viewState = 2
        Log.e(TAG, "************* backgrounded")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {

        Log.e(TAG, "************* foregrounded")
        val email = Utilities.getLoggedEmail()
        (email.isNotEmpty()).guard { println("foreground empty email") }

        val userDisconnect: Boolean = Utilities.getUserNotifiedStatus("Device")

        if (!userDisconnect) {
            SPBleManager.viewState = 1
            reconnectDevice()
        }

//        SPBleManager.viewState = 1
//        reconnectDevice()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onActivityStarted(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
        TODO("Not yet implemented")
    }
}