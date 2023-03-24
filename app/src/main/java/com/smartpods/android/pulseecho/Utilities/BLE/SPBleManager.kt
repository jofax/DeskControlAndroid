package com.smartpods.android.pulseecho.Utilities.BLE

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import com.smartpods.android.pulseecho.Constants.CURRENT_LOGGED_USER
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Constants.PulseState
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.Utilities.SPRealmHelper
import com.smartpods.android.pulseecho.Utilities.Utilities
import com.smartpods.android.pulseecho.Utilities.guard
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue



private const val GATT_MIN_MTU_SIZE = 23
/** Maximum BLE MTU size as defined in gatt_api.h. */
private const val GATT_MAX_MTU_SIZE = 512

object SPBleManager {

    var listeners: MutableSet<WeakReference<SPBleEventListener>> = mutableSetOf()
    var viewState = 0

    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<SPBleOperationType>()
    private var pendingOperation: SPBleOperationType? = null
    private var mPulseState: PulseState = PulseState.Unknown
    private var connectionAttemptLimit = 2
    private var connectionAttempt = 0

    fun servicesOnDevice(device: BluetoothDevice): List<BluetoothGattService>? =
            deviceGattMap[device]?.services

    fun listenToBondStateChanges(context: Context) {
        context.applicationContext.registerReceiver(
                broadcastReceiver,
                IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }

    fun registerListener(listener: SPBleEventListener) {
        if (listeners.map { it.get() }.contains(listener)) { return }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
        Timber.d("Added listener $listener, ${listeners.size} listeners total")
        println("Added listener $listener, ${listeners.size} listeners total")
    }

    fun unregisterListener(listener: SPBleEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<SPBleEventListener>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
            Timber.d("Removed listener ${it.get()}, ${listeners.size} listeners total")
            println("Removed listener ${it.get()}, ${listeners.size} listeners total")
        }
    }

    fun connect(device: BluetoothDevice, context: Context) {

        if (device.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
        } else {
            if (mPulseState == PulseState.BoxChangeName) {
//            enqueueOperation(Connect(device, context.applicationContext))
//
//            val gatt = deviceGattMap[device]
//                ?: this@SPBleManager.run {
//                    println("Not connected to device! Aborting operation.")
//                    signalEndOfOperation()
//                    listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, appContext.getString(R.string.ble_connection_error_device)) }
//                    return
//                }
//
//            gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let { mChar ->
//                if (mChar !== null) {
//                    listeners.forEach { it.get()?.onBondComplete?.invoke(device, mChar) }
//                    sendHeartBeat(gatt)
//                } else {
//                    println("connect Empty characteristics")
//                }
//            }


                println("initiateBleConnection device status : " + device.bondState)
                initiateBleConnection(device,context)

            } else {
                if (device.bondState == BOND_BONDED && device.isConnected()) {
                    Timber.e("Already connected to ${device.address}!")
                    println("Already connected to ${device.address}!")

                    val gatt = deviceGattMap[device]
                        ?: this@SPBleManager.run {
                            println("Not connected to device! Aborting operation.")
                            signalEndOfOperation()
                            return
                        }
                    mPulseState = PulseState.Connected
                    boxDataStream(gatt)
                    sendHeartBeat(gatt)
                    sendCommandToCharacteristic(gatt.device, SPRequestParameters.Profile)
                    listeners.forEach { it.get()?.onPairedComplete?.invoke(device) }

                } else {
                    //listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, "Not connected to device! Aborting operation.") }
                    initiateBleConnection(device,context)
                }
            }
        }
    }

    private fun initiateBleConnection(device: BluetoothDevice, context: Context) {
        enqueueOperation(Connect(device, context.applicationContext))
        val gatt = deviceGattMap[device]
            ?: this@SPBleManager.run {
                println("Not connected to device! Aborting operation.")
                signalEndOfOperation()
                if (device.bondState == BOND_NONE) {
                    listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, appContext.getString(R.string.ble_connection_error_device)) }
                }

                return
            }

        println("debug initiateBleConnection: " + gatt.device.bondState)
        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let { mChar ->
            if (mChar !== null) {
                connectionAttempt = 0
                sendHeartBeat(gatt)

                if (gatt.device.bondState == BOND_BONDED) {
                    listeners.forEach { it.get()?.onBondComplete?.invoke(device, mChar) }
                }

            } else {
                println("connect Empty characteristics")
            }
        }
    }

    fun teardownConnection(device: BluetoothDevice) {
        if (device.isConnected()) {
            enqueueOperation(Disconnect(device))
        } else {
            Timber.e("Not connected to ${device.address}, cannot teardown connection!")
            println("Not connected to ${device.address}, cannot teardown connection!")
        }
    }

    fun forceDisconnect(device: BluetoothDevice) {
        val gatt = deviceGattMap[device]
            ?: this@SPBleManager.run {
                Timber.e("Not connected to ${device.address}! Aborting $device operation.")
                println("Not connected to ${device.address}! Aborting $device operation.")
                signalEndOfOperation()
                return
            }
        gatt.close()
        deviceGattMap.remove(device)
        listeners.forEach { it.get()?.onDisconnect?.invoke(device) }
        signalEndOfOperation()
    }

    fun readCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {

//        if (characteristic.value !== null) {
//            println("return readCharacteristic: ${characteristic.value.toHexString()}")
//        }

        if (device.isConnected() && characteristic.isReadable()) {
            enqueueOperation(CharacteristicRead(device, characteristic.uuid))
        } else if (!characteristic.isReadable()) {
            Timber.e("Attempting to read ${characteristic.uuid} that isn't readable!")
            println("Attempting to read ${characteristic.uuid} that isn't readable!")
        } else if (!device.isConnected()) {
            Timber.e("Not connected to ${device.address}, cannot perform characteristic read")
            println("Not connected to ${device.address}, cannot perform characteristic read")
        }
    }

    fun writeCharacteristic(
            device: BluetoothDevice,
            characteristic: BluetoothGattCharacteristic,
            payload: ByteArray
    ) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            characteristic.isNotifiable() -> BluetoothGattCharacteristic.PROPERTY_NOTIFY
            else -> {
                Timber.e("Characteristic ${characteristic.uuid} cannot be written to")
                println("Characteristic ${characteristic.uuid} cannot be written to")
                return
            }
        }

        if (device.isConnected()) {
            enqueueOperation(CharacteristicWrite(device, characteristic.uuid, writeType, payload))
        } else {
            Timber.e("Not connected to ${device.address}, cannot perform characteristic write")
            println("Not connected to ${device.address}, cannot perform characteristic write")
        }
    }

    fun readDescriptor(device: BluetoothDevice, descriptor: BluetoothGattDescriptor) {
        if (device.isConnected() && descriptor.isReadable()) {
            enqueueOperation(DescriptorRead(device, descriptor.uuid))
        } else if (!descriptor.isReadable()) {
            Timber.e("Attempting to read ${descriptor.uuid} that isn't readable!")
            println("Attempting to read ${descriptor.uuid} that isn't readable!")
        } else if (!device.isConnected()) {
            Timber.e("Not connected to ${device.address}, cannot perform descriptor read")
            println("Not connected to ${device.address}, cannot perform descriptor read")
        }
    }

    fun writeDescriptor(
            device: BluetoothDevice,
            descriptor: BluetoothGattDescriptor,
            payload: ByteArray
    ) {
        if (device.isConnected() && (descriptor.isWritable() || descriptor.isCccd())) {
            enqueueOperation(DescriptorWrite(device, descriptor.uuid, payload))
        } else if (!device.isConnected()) {
            Timber.e("Not connected to ${device.address}, cannot perform descriptor write")
            println("Not connected to ${device.address}, cannot perform descriptor write")
        } else if (!descriptor.isWritable() && !descriptor.isCccd()) {
            Timber.e("Descriptor ${descriptor.uuid} cannot be written to")
            println("Descriptor ${descriptor.uuid} cannot be written to")
        }
    }

    fun enableNotifications(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() &&
                (characteristic.isIndicatable() || characteristic.isNotifiable())
        ) {
            enqueueOperation(EnableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            Timber.e("Not connected to ${device.address}, cannot enable notifications")
            println("Not connected to ${device.address}, cannot enable notifications")
        } else if (!characteristic.isIndicatable() && !characteristic.isNotifiable()) {
            Timber.e("Characteristic ${characteristic.uuid} doesn't support notifications/indications")
            println("Characteristic ${characteristic.uuid} doesn't support notifications/indications")
        }
    }

    fun disableNotifications(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() &&
                (characteristic.isIndicatable() || characteristic.isNotifiable())
        ) {
            enqueueOperation(DisableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            Timber.e("Not connected to ${device.address}, cannot disable notifications")
            println("Not connected to ${device.address}, cannot disable notifications")
        } else if (!characteristic.isIndicatable() && !characteristic.isNotifiable()) {
            Timber.e("Characteristic ${characteristic.uuid} doesn't support notifications/indications")
            println("Characteristic ${characteristic.uuid} doesn't support notifications/indications")
        }
    }

    fun requestMtu(device: BluetoothDevice, mtu: Int) {
        if (device.isConnected()) {
            enqueueOperation(MtuRequest(device, mtu.coerceIn(GATT_MIN_MTU_SIZE, GATT_MAX_MTU_SIZE)))
        } else {
            Timber.e("Not connected to ${device.address}, cannot request MTU update!")
            println("Not connected to ${device.address}, cannot request MTU update!")
        }
    }

    // - Beginning of PRIVATE functions

    @Synchronized
    private fun enqueueOperation(operation: SPBleOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        Timber.d("End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    /**
     * Perform a given [BleOperationType]. All permission checks are performed before an operation
     * can be enqueued by [enqueueOperation].
     */
    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Timber.e("doNextOperation() called when an operation is pending! Aborting.")
            println("doNextOperation() called when an operation is pending! Aborting.")
            return
        }

        val operation = operationQueue.poll() ?: run {
            Timber.v("Operation queue empty, returning")
            println("Operation queue empty, returning")
            return
        }
        pendingOperation = operation

        // Handle Connect separately from other operations that require device to be connected
        if (operation is Connect) {
            with(operation) {
                Timber.w("Connecting to ${device.address}")
                println("Connecting to ${device.address}")
                device.connectGatt(context, false, callback)
            }
            return
        }

        // Check BluetoothGatt availability for other operations
        val gatt = deviceGattMap[operation.device]
                ?: this@SPBleManager.run {
                    Timber.e("Not connected to ${operation.device.address}! Aborting $operation operation.")
                    println("Not connected to ${operation.device.address}! Aborting $operation operation.")
                    signalEndOfOperation()
                    return
                }

        // TODO: Make sure each operation ultimately leads to signalEndOfOperation()
        // TODO: Refactor this into an BleOperationType abstract or extension function
        when (operation) {
            is Disconnect -> with(operation) {
                if (mPulseState.equals(PulseState.Disconnected)) {
                        Timber.w("Disconnecting from ${device.address}")
                    gatt.close()
                    deviceGattMap.remove(device)
                    listeners.forEach { it.get()?.onDisconnect?.invoke(device) }
                    signalEndOfOperation()
                }
            }
            is CharacteristicWrite -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    characteristic.writeType = writeType
                    characteristic.value = payload
                    gatt.writeCharacteristic(characteristic)
                } ?: this@SPBleManager.run {
                    Timber.e("Cannot find $characteristicUuid to write to")
                    println("Cannot find $characteristicUuid to write to")
                    signalEndOfOperation()
                }
            }
            is CharacteristicRead -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    gatt.readCharacteristic(characteristic)
                } ?: this@SPBleManager.run {
                    Timber.e("Cannot find $characteristicUuid to read from")
                    println("Cannot find $characteristicUuid to read from")
                    signalEndOfOperation()
                }
            }
            is DescriptorWrite -> with(operation) {
                gatt.findDescriptor(descriptorUuid)?.let { descriptor ->
                    descriptor.value = payload
                    gatt.writeDescriptor(descriptor)
                } ?: this@SPBleManager.run {
                    Timber.e("Cannot find $descriptorUuid to write to")
                    println("Cannot find $descriptorUuid to write to")
                    signalEndOfOperation()
                }
            }
            is DescriptorRead -> with(operation) {
                gatt.findDescriptor(descriptorUuid)?.let { descriptor ->
                    gatt.readDescriptor(descriptor)
                } ?: this@SPBleManager.run {
                    Timber.e("Cannot find $descriptorUuid to read from")
                    println("Cannot find $descriptorUuid to read from")
                    signalEndOfOperation()
                }
            }
            is EnableNotifications -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(Constants.CCC_DESCRIPTOR_UUID)
                    val payload = when {
                        characteristic.isIndicatable() ->
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        characteristic.isNotifiable() ->
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        else ->
                            error("${characteristic.uuid} doesn't support notifications/indications")
                    }

                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!gatt.setCharacteristicNotification(characteristic, true)) {
                            Timber.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                            println("setCharacteristicNotification failed for ${characteristic.uuid}")
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = payload
                        gatt.writeDescriptor(cccDescriptor)
                    } ?: this@SPBleManager.run {
                        Timber.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
                        println("${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@SPBleManager.run {
                    Timber.e("Cannot find $characteristicUuid! Failed to enable notifications.")
                    println("Cannot find $characteristicUuid! Failed to enable notifications.")
                    signalEndOfOperation()
                }
            }
            is DisableNotifications -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(Constants.CCC_DESCRIPTOR_UUID)
                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!gatt.setCharacteristicNotification(characteristic, false)) {
                            Timber.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                            println("setCharacteristicNotification failed for ${characteristic.uuid}")
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(cccDescriptor)
                    } ?: this@SPBleManager.run {
                        Timber.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
                        println("${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@SPBleManager.run {
                    Timber.e("Cannot find $characteristicUuid! Failed to disable notifications.")
                    println("Cannot find $characteristicUuid! Failed to disable notifications.")
                    signalEndOfOperation()
                }
            }
            is MtuRequest -> with(operation) {
                gatt.requestMtu(mtu)
            }
        }
    }

    val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    //need to save the connected peripheral
                    Timber.w("onConnectionStateChange: connected to $deviceAddress")
                    println("onConnectionStateChange: connected to $deviceAddress")
                    println("onConnectionStateChange: bonded to ${gatt.device.bondState}")

                    if (gatt.device.bondState == BOND_NONE || gatt.device.bondState == BOND_BONDING) {
                        gatt.device.createBond()
                    } else {
                        deviceGattMap[gatt.device] = gatt
                        Handler(Looper.getMainLooper()).post {
                            gatt.discoverServices()
                        }

                        //getSerialAndRegistrationCode(gatt)

                    }



                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Timber.e("onConnectionStateChange: disconnected from $deviceAddress")
                    println("onConnectionStateChange: disconnected from $deviceAddress")
                    teardownConnection(gatt.device)
                }
            } else {
                Timber.e("onConnectionStateChange: status $status encountered for $deviceAddress!")
                println("onConnectionStateChange: status $status encountered for $deviceAddress!")
                if (pendingOperation is Connect) {
                    signalEndOfOperation()
                }

                println("mPulseState: $mPulseState")

                if (mPulseState == PulseState.BoxChangeName) {
                    //mPulseState = PulseState.Disconnected
                    //teardownConnection(gatt.device) //double check prior for bonding
                    return
                } else {
                    mPulseState = PulseState.Disconnected
                    teardownConnection(gatt.device) //double check prior for bonding
                    //deviceGattMap.remove(gatt.device)
                    //gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Timber.w("Discovered ${services.size} services for ${device.address}.")
                    println("Discovered ${services.size} services for ${device.address}.")
                    printGattTable()
                    requestMtu(device, GATT_MAX_MTU_SIZE)

                    println("device.bondState : ${device.bondState}")

                    if (device.bondState == BOND_NONE || device.bondState == BOND_BONDING)   {
                        mPulseState = PulseState.BoxChangeName
                        connectionAttempt += 1
                        //changePeripheralNameHeartbeat(gatt)
                        sendHeartBeat(gatt)
                    } else {
                        sendHeartBeat(gatt)
                    }
                    startBleStream(gatt.device)
                    listeners.forEach { it.get()?.onConnectionSetupComplete?.invoke(this) }
                } else {
                    Timber.e("Service discovery failed due to status $status")
                    println("Service discovery failed due to status $status")
                    teardownConnection(gatt.device)
                }
            }

            if (pendingOperation is Connect) {
                signalEndOfOperation()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Timber.w("ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
            println("ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
            listeners.forEach { it.get()?.onMtuChanged?.invoke(gatt.device, mtu) }

            if (pendingOperation is MtuRequest) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        //Timber.i("Read characteristic $uuid | value: ${value.toHexString()}")
                        //println("Read characteristic $uuid | value: ${value.toHexString()}")
                        listeners.forEach { it.get()?.onCharacteristicRead?.invoke(gatt.device, this) }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Timber.e("Read not permitted for $uuid!")
                        println("Read not permitted for $uuid!")
                    }
                    else -> {
                        Timber.e("Characteristic read failed for $uuid, error: $status")
                        println("Characteristic read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicRead) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            with(characteristic) {
                println("onCharacteristicWrite status $status")
                println("onCharacteristicWrite UUID ${characteristic.uuid.toString()}")
                println("onCharacteristicWrite value ${characteristic.value.toHashSet()}")
                println("onCharacteristicWrite value ${characteristic.value.toHexString()}")
                println("onCharacteristicWrite value ${characteristic.value.toString()}")
                println("onCharacteristicWrite value ${characteristic.value.toList()}")

                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Timber.i("onCharacteristicWrite Wrote to characteristic $uuid | value: ${value.toHexString()}")
                        listeners.forEach { it.get()?.onCharacteristicWrite?.invoke(gatt.device, this) }
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Timber.e("onCharacteristicWrite Write exceeded connection ATT MTU! $uuid!")
                        println("onCharacteristicWrite Write invalid attribute length $uuid!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Timber.e("onCharacteristicWrite Write not permitted for $uuid!")
                        println("v Write not permitted for $uuid!")
                    }
                    else -> {
                        Timber.e("onCharacteristicWrite Characteristic write failed for $uuid, error: $status")
                        println("onCharacteristicWrite Characteristic write failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicWrite) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                //readCharacteristic(gatt.device ,characteristic)
                val response = value.toHexString()
                //println("onCharacteristicChanged ubyteArray: ${value}")
                //println("onCharacteristicChanged hex: $response")


                SPDataParser.initWithString(characteristic.value)

                if (value.toHexString() == Constants.CHANGE_NAME_HEARTBEAT) {
                    reconnectSmartpodsWithGatt(gatt)

                    if (gatt.device.bondState == BOND_BONDED) {
                        listeners.forEach { it.get()?.onBondComplete?.invoke(gatt.device, this) }
                    }
                }

                //Timber.i("Characteristic $uuid changed | value: ${value.toHexString()}")
                //println("onCharacteristicChanged Characteristic $uuid changed | value: ${value.toHexString()}")
                //println("onCharacteristicChanged Characteristic $uuid changed | value string: ${value.toString()}")
                listeners.forEach { it.get()?.onCharacteristicChanged?.invoke(gatt.device, this) }
            }
        }




        override fun onDescriptorRead(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int
        ) {
            with(descriptor) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Timber.i("Read descriptor $uuid | value: ${value.toHexString()}")
                        println("Read descriptor $uuid | value: ${value.toHexString()}")
                        listeners.forEach { it.get()?.onDescriptorRead?.invoke(gatt.device, this) }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Timber.e("Read not permitted for $uuid!")
                        println("Read not permitted for $uuid!")
                    }
                    else -> {
                        Timber.e("Descriptor read failed for $uuid, error: $status")
                        println("Descriptor read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is DescriptorRead) {
                signalEndOfOperation()
            }
        }

        override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int
        ) {
            with(descriptor) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Timber.i("Wrote to descriptor $uuid | value: ${value.toHexString()}")
                        println("Wrote to descriptor $uuid | value: ${value.toHexString()}")

                        if (isCccd()) {
                            onCccdWrite(gatt, value, characteristic)
                        } else {
                            listeners.forEach { it.get()?.onDescriptorWrite?.invoke(gatt.device, this) }
                        }
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Timber.e("Write not permitted for $uuid!")
                        println("Write not permitted for $uuid!")
                    }
                    else -> {
                        Timber.e("Descriptor write failed for $uuid, error: $status")
                        println("Descriptor write failed for $uuid, error: $status")
                    }
                }
            }

            if (descriptor.isCccd() &&
                    (pendingOperation is EnableNotifications || pendingOperation is DisableNotifications)
            ) {
                signalEndOfOperation()
            } else if (!descriptor.isCccd() && pendingOperation is DescriptorWrite) {
                signalEndOfOperation()
            }
        }

        private fun onCccdWrite(
                gatt: BluetoothGatt,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic
        ) {
            val charUuid = characteristic.uuid
            val notificationsEnabled =
                    value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ||
                            value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
            val notificationsDisabled =
                    value.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            when {
                notificationsEnabled -> {
                    Timber.w("Notifications or indications ENABLED on $charUuid")
                    println("Notifications or indications ENABLED on $charUuid")
                    listeners.forEach {
                        it.get()?.onNotificationsEnabled?.invoke(
                                gatt.device,
                                characteristic
                        )
                    }
                }
                notificationsDisabled -> {
                    Timber.w("Notifications or indications DISABLED on $charUuid")
                    println("Notifications or indications DISABLED on $charUuid")
                    listeners.forEach {
                        it.get()?.onNotificationsDisabled?.invoke(
                                gatt.device,
                                characteristic
                        )
                    }
                }
                else -> {
                    Timber.e("Unexpected value ${value.toHexString()} on CCCD of $charUuid")
                    println("Unexpected value ${value.toHexString()} on CCCD of $charUuid")
                }
            }
        }

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            with(intent) {
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val previousBondState = getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
                    val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val bondTransition = "${previousBondState.toBondStateDescription()} to " +
                            bondState.toBondStateDescription()
                    Timber.w("${device?.address} bond state changed | $bondTransition")
                    println("${device?.address} bond state changed | $bondTransition")
                    println("${device?.address} bond device state changed | ${device?.bondState}")

                    if (device?.bondState == BOND_BONDED) {
                        //initiateBleConnection(device,context)
                        println("execute connect device or send heartBeat : ${mPulseState}")
                        connectionAttempt = 0
                        connect(device, context)
                    }

                    if (device?.bondState == BOND_BONDING) {
                        println("attempt to bond device  : ${mPulseState}")
                    }

                    if (device?.bondState == BOND_NONE) {
                        println("no bond exist  : ${mPulseState}")
                        listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, appContext.getString(R.string.ble_connection_error_device)) }
                    }
                }
            }
        }

        private fun Int.toBondStateDescription() = when (this) {
            BluetoothDevice.BOND_BONDED -> "BONDED"
            BluetoothDevice.BOND_BONDING -> "BONDING"
            BluetoothDevice.BOND_NONE -> "NOT BONDED"
            else -> "ERROR: $this"
        }
    }

    fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)
    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

    // - Beginning of SMARTPODS functions
    fun setNotifyCharacteristic(gatt: BluetoothGatt) {
        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {
            if (it !== null) {
                enableNotifications(gatt.device, it)
            } else {
                println("Empty characteristics")
            }

        }
    }


    fun changePeripheralNameHeartbeat (gatt: BluetoothGatt) {
        //val nameChange = byteArrayOfInts(0x13, 0x11, 0x62, 0x06, 0x73, 0x6D, 0x61, 0x72, 0x74, 0x70, 0x6F, 0x64, 0x73, 0x32, 0x78)
        val nameChange = SPRequestParameters.BLEHearbeatWithName
        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {
            if (it !== null) {
                if (gatt.setCharacteristicNotification(it, true)) {
                    println("changePeripheralNameHeartbeat characteristic: $it")
                    writeCharacteristic(gatt.device, it, nameChange)
                    setNotifyCharacteristic(gatt)
                } else {
                    println("Failed to register notification")
                }
            } else {
                println("Empty characteristics")
            }

        }

        /*gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {

            if (it !== null) {
                if (gatt.setCharacteristicNotification(it, true)){
                    val descriptor = it.getDescriptor(UUID.fromString(Constants.CCC_DESCRIPTOR_UUID))
                    if (descriptor != null){
                        if (BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 && it.properties != 0) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        } else if (BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 && it.properties != 0) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        } else {
                            println("The characteristic does not have NOTIFY or INDICATE property set")

                        }
                        writeDescriptor(gatt.device, descriptor, nameChange)
                    } else {
                        println("Failed to set client characteristic notification")
                    }
                } else {
                    println("Failed to register notification")
                }
            }
        }*/
    }

    fun sendHeartBeatWithDevice(device: BluetoothDevice) {
        val heartBeat = if (viewState == 1) SPRequestParameters.BLEHeartbeatForeground else SPRequestParameters.BLEHeartbeatBackground
        println("sendHeartBeatWithDevice HEARTBEAT PACKET: ${heartBeat} | $viewState")

        (device.isConnected()).guard {  }

        val gatt = deviceGattMap[device]
            ?: this@SPBleManager.run {
                println("Not connected to device! Aborting operation.")
                listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, "Not connected to device! Aborting operation.") }
                signalEndOfOperation()
                return
            }

        println("sendHeartBeatWithDevice gatt: ${gatt.device}")

        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {
            if (it !== null) {
                println("sendHeartBeatWithDevice characteristic: $it")
                //enableNotifications(gatt.device, it);
                writeCharacteristic(gatt.device, it, heartBeat)
            } else {
                println("Empty characteristics")
            }

        }

    }

    fun sendHeartBeat(gatt: BluetoothGatt) {
        val heartBeat = if (viewState == 1) SPRequestParameters.BLEHeartbeatForeground else SPRequestParameters.BLEHeartbeatBackground
        println("sendHeartBeat HEARTBEAT PACKET: ${heartBeat} | $viewState")
        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {
            if (it !== null) {
                println("sendHeartBeat characteristic: $it")
                //enableNotifications(gatt.device, it);
                writeCharacteristic(gatt.device, it, heartBeat)
            } else {
                println("Empty characteristics")
            }

        }
    }

    fun sendCommandToCharacteristic(device: BluetoothDevice, payload: ByteArray) {

        val gatt = deviceGattMap[device]
            ?: this@SPBleManager.run {
                println("Not connected to device! Aborting operation.")
                listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, "Not connected to device! Aborting operation.") }
                signalEndOfOperation()
                return
            }

        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {
            if (it !== null) {
                println("sendCommandToCharacteristic characteristic: $it with payload: $payload")
                writeCharacteristic(gatt.device, it, payload)

            } else {
                println("Empty characteristics")
            }

        }
    }

    fun startBleStream(device: BluetoothDevice) {
        val gatt = deviceGattMap[device]
            ?: this@SPBleManager.run {
                println("Not connected to device! Aborting operation.")
                listeners.forEach { it.get()?.onDeviceConnectivityError?.invoke(device, "Not connected to device! Aborting operation.") }
                signalEndOfOperation()
                return
            }

        boxDataStream(gatt)
        val email = Utilities.getLoggedEmail()
        SPRealmHelper.saveOrUpdateObjectWithData(hashMapOf(
            "DeviceDisconnectedByUser" to false), email, REALM_OBJECT_TYPE.PULSEAPPSTATE)
    }


    fun boxDataStream (gatt: BluetoothGatt) {
        gatt.findCharacteristic(UUID.fromString(Constants.BLE_CHARACTERISTICS))?.let {
            if (it !== null) {
                if (gatt.setCharacteristicNotification(it, true)) {
                    println("boxDataStream characteristic: $it")
                    readCharacteristic(gatt.device, it)
                } else {
                    println("Failed to register notification")
                }
            } else {
                println("Empty characteristics")
            }

        }

    }

    fun reconnectSmartpodsWithGatt(gatt:BluetoothGatt) {
        println("reconnectSmartpodsWithGatt: {$gatt}")
        //gatt.device.connectGatt(appContext, false, callback)
        connect(gatt.device, appContext)
        //sendHeartBeat(gatt)


    }

    fun reconnectSmartpodsWithDevice(device:BluetoothDevice) {
        println("reconnectSmartpodsWithDevice: {$device}")
        device.connectGatt(appContext, true, callback)
        //connect(device, appContext)
    }

    fun forceRemoveDeviceAndBond(gatt:BluetoothGatt) {
        Timber.w("Disconnecting from ${gatt.device.address}")
        gatt.disconnect()
        gatt.close()
        deviceGattMap.remove(gatt.device)
        signalEndOfOperation()

    }

    fun getSerialAndRegistrationCode(device:BluetoothDevice) {
        if (device.bondState == BOND_BONDED) {
            if (device.isConnected()) {
                println("Get SPRequestParameters.Information")
                sendCommandToCharacteristic(device,
                    SPRequestParameters.Information)
            }
        }
    }

}
