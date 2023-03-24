package com.smartpods.android.pulseecho.BLEScanner

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.smartpods.android.pulseecho.BaseActivity
import com.smartpods.android.pulseecho.Constants.Constants.ENABLE_BLUETOOTH_REQUEST_CODE
import com.smartpods.android.pulseecho.Constants.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.smartpods.android.pulseecho.CustomUI.BMIDialogFragment
import com.smartpods.android.pulseecho.CustomUI.PairingDialogFragment
import com.smartpods.android.pulseecho.Interfaces.CoreObject
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleEventListener
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.BLE.smartpodsBleDevice
import com.smartpods.android.pulseecho.Utilities.BLE.toHexString
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlinx.android.synthetic.main.activity_device_list.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompatV7.BuildConfig
import timber.log.Timber
import info.androidhive.fontawesome.FontDrawable
import org.jetbrains.anko.bluetoothManager
import javax.sql.ConnectionEventListener

class DeviceListActivity : BaseActivity() {

    private lateinit var mbtnClosePopUp: ImageButton
    private lateinit var mbtnScanDevice: ImageButton
    private lateinit var mbtnPairingVideo: ImageButton
    private lateinit var mScanDeviceActive: FontDrawable
    private lateinit var mScanDevice: FontDrawable
    private lateinit var mDeviceScanner: CircularProgressIndicator
    private var mIndexToConnect: Int = 0

    private val bleScanner by lazy {
        //bluetoothAdapter.bluetoothLeScanner
        bluetoothManager.adapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { mbtnScanDevice.visibility = if (value) View.GONE else View.VISIBLE }
        }

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result, idx ->
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                mIndexToConnect = idx
                println("scanResultAdapter connect: " + result.device)
                Timber.w("Connecting to $address")
                SPBleManager.connect(this, this@DeviceListActivity)

            }
        }
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        mbtnScanDevice = findViewById(R.id.btnScanDevice)
        mbtnScanDevice.setOnClickListener {
            mbtnScanDevice.isEnabled = false
            if (isScanning) stopBleScan() else startBleScan() }

        val popUpCloseIcon = FontDrawable(this, R.string.fa_times_solid, true, false)
        mScanDeviceActive = FontDrawable(this, R.string.fa_sync_alt_solid, true, false)
        mScanDevice = FontDrawable(this, R.string.fa_times_solid, true, false)
        mbtnScanDevice.setImageDrawable(mScanDeviceActive)

        mbtnClosePopUp =  findViewById(R.id.btnCloseDeviceList)
        mbtnClosePopUp.setImageDrawable(popUpCloseIcon)

        mbtnClosePopUp.setOnClickListener{

            this.finish()
        }

        mbtnPairingVideo = findViewById(R.id.btnPairingVideo)
        mDeviceScanner = findViewById(R.id.circularDeviceList)

        mbtnPairingVideo.setOnClickListener{
            val dialog = PairingDialogFragment()
            val fragmentManager = this.supportFragmentManager
            dialog.show(fragmentManager, "PairingDialogFragment")
        }

        setupRecyclerView()
        SPBleManager.registerListener(connectionEventListener)
        SPDataParser.registerListener(spDataParseEventListener)
        //startBleScan()
    }

    override fun onResume() {
        super.onResume()

        mbtnScanDevice.isEnabled = true
        if (!bluetoothManager.adapter.isEnabled) {
            promptEnableBluetooth()
        } else {
            //startBleScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    /*******************************************
     * Private functions
     *******************************************/

    private fun promptEnableBluetooth() {
        if (!bluetoothManager.adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
            mbtnScanDevice.visibility = View.GONE
            mDeviceScanner.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                stopBleScan()
            }, 10000) // delaying for 4 seconds...
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
        mbtnScanDevice.isEnabled = true
        mbtnScanDevice.visibility = View.VISIBLE
        mDeviceScanner.visibility = View.GONE
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }

        fun okAction() = DialogInterface.OnClickListener { dialog, which ->
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        showALertDialog(getString(R.string.location_required), getString(R.string.location_required_message), false, getString(R.string.btn_ok), okAction())
    }

    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@DeviceListActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    //Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }

                if (result.device.name != null) {
                    if (result.device.name.startsWith("Smartpods", ignoreCase = true) || result.device.name.startsWith("Smartpods", ignoreCase = true)) {
                        println("Found BLE device! Name: $result.device.name")
                        scanResults.add(result)
                        scanResultAdapter.notifyItemInserted(scanResults.size - 1)
                    }
                }

                //scanResults.add(result)
                //scanResultAdapter.notifyItemInserted(scanResults.size - 1)

            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

    private val spDataParseEventListener by lazy {
        DataEventListener().apply {
            onCoreDataEventReceived = {
                //println("spDataParseEventListener : $it")
                if (it.Length == 20) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            finish()
                            SPBleManager.unregisterListener(connectionEventListener)
                        },
                        2000 // value in milliseconds
                    )
                }
            }
        }
    }

    private val connectionEventListener by lazy {
        SPBleEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                println("debug onConnectionSetupComplete bond state: " + gatt.device.bondState)
                  //if (gatt.device.bondState == 11) {
                      //scanResultAdapter.notifyItemChanged(this@DeviceListActivity.mIndexToConnect)
                      //gatt.close()
                      //gatt.disconnect()
                  //}
//                Intent(this@MainActivity, BleOperationsActivity::class.java).also {
//                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
//                    startActivity(it)
//                }
            }
            onDisconnect = {
                println("debug DeviceListActivity onDisconnect: " + it)
                fun okAction() = DialogInterface.OnClickListener { dialog, which ->
                    scanResultAdapter.notifyItemChanged(this@DeviceListActivity.mIndexToConnect, it)
                }

                showALertDialog(getString(R.string.alert_disconnected), getString(R.string.alert_disconnected_message),false, getString(R.string.btn_ok), okAction())
            }

            onNotificationsDisabled = { device, gattChar ->
                println("debug DeviceListActivity onNotificationsDisabled: " + device)
                if (gattChar.value != null) {
                    println("debug DeviceListActivity onNotificationsDisabled: " + gattChar.value.toHexString())
                }
            }

            onNotificationsEnabled = { device, gattChar ->
                println("debug DeviceListActivity onNotificationsEnabled: " + device)
                if (gattChar.value != null) {
                    println("debug DeviceListActivity onNotificationsEnabled: " + gattChar.value.toHexString())
                }
            }

            onCharacteristicWrite = { device, gattChar ->
                println("debug DeviceListActivity onCharacteristicWrite: " + device)
                if (gattChar.value != null) {
                    println("debug DeviceListActivity onCharacteristicWrite: " + gattChar.value.toHexString())
                }
            }

            onCharacteristicRead = { device, desc ->
                println("debug DeviceListActivity onCharacteristicRead: " + device)
                println("debug DeviceListActivity onCharacteristicRead: " + desc)
            }

            onCharacteristicChanged = { device, gattChar ->
                println("debug DeviceListActivity onCharacteristicChanged: " + device)

                if (gattChar.value != null) {
                    println("debug DeviceListActivity onCharacteristicChanged: " + gattChar.value.toHexString())

                }


            }

            onDescriptorWrite = { device, desc ->
                println("debug DeviceListActivity onDescriptorWrite: " + device)
                println("debug DeviceListActivity onDescriptorWrite: " + desc)
            }

            onDescriptorRead = { device, desc ->
                println("debug DeviceListActivity onDescriptorRead: " + device)
                println("debug DeviceListActivity onDescriptorRead: " + desc)
            }

            onMtuChanged = { gatt, obj ->
                println("debug DeviceListActivity onMtuChanged: " + gatt)
                println("debug DeviceListActivity onMtuChanged: " + obj)
            }

            onBondComplete =  { gatt, obj ->
                println("debug DeviceListActivity onBondComplete: " + gatt)
                println("debug DeviceListActivity onBondComplete: " + obj)
                println("debug DeviceListActivity onBondComplete gatt.isConnected(): " + gatt.isConnected())
                println("debug DeviceListActivity onBondComplete condition: " + (gatt.bondState == BluetoothDevice.BOND_BONDED && gatt.isConnected()))
                /*if (gatt.bondState == BluetoothDevice.BOND_BONDED && gatt.isConnected()) {
                    SPBleManager.startBleStream(Utilities.getSmartpodsDevice())
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            finish()
                            SPBleManager.unregisterListener(this)
                        },
                        2000 // value in milliseconds
                    )
                } else {
                    deviceConnectError(getString(R.string.ble_connection_error_device))
                }*/

                SPBleManager.sendHeartBeatWithDevice(Utilities.getSmartpodsDevice())
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        SPBleManager.startBleStream(Utilities.getSmartpodsDevice())
                        finish()
                        SPBleManager.unregisterListener(this)
                    },
                    3000 // value in milliseconds
                )


            }

            onPairedComplete =  { gatt ->
                //finish()
                println("debug DeviceListActivity onPairedComplete: " + gatt)
            }

            onDeviceConnectivityError = { device, desc ->
                deviceConnectError(getString(R.string.ble_connection_error_device))
            }
        }
    }

    fun deviceConnectError(message: String) {
        toast(applicationContext,message)
        scanResultAdapter.notifyItemChanged(this@DeviceListActivity.mIndexToConnect)
    }

    override fun onDestroy() {
        super.onDestroy()
        scan_results_recycler_view.adapter = null
    }
}