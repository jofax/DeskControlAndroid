package com.smartpods.android.pulseecho.Utilities

import android.util.Log
import com.smartpods.android.pulseecho.Constants.REALM_OBJECT_TYPE
import com.smartpods.android.pulseecho.Model.*
import io.realm.Realm
import io.realm.RealmObject
import io.realm.kotlin.where
import org.jetbrains.anko.db.REAL


object SPRealmHelper {
    lateinit var realm: Realm
    init {
        print("SPRealmHelper initialized")
    }

    fun saveObject(clssObj: RealmObject,
                   email: String,
                   type: REALM_OBJECT_TYPE) {
        realm = Realm.getInstance(Utilities.getRealmForUser(email, true))
        try {
            realm.executeTransaction {

                when(type) {
                    REALM_OBJECT_TYPE.USERMODEL -> {
                        UserModelDAO.saveUser(it, clssObj)
                    }
                    REALM_OBJECT_TYPE.SPCLIENTINFO -> {
                        SPCLientInfoDAO.saveClientInfo(it, clssObj)
                    }
                    REALM_OBJECT_TYPE.PULSEPROFILE -> {
                        UserProfileDao.saveUserProfile(it, clssObj)
                    }
                    REALM_OBJECT_TYPE.PULSEDATAPUSH -> {
                        DataPushDao.savePushCredentials(it, clssObj)
                    }
                    REALM_OBJECT_TYPE.PULSEDEVICES -> {
                        PulseDeviceDao.savePulseDevice(it, clssObj)
                    }
                }
            }
        } finally {
            close()
        }
    }

    fun saveOrUpdateObjectWithData(data: HashMap<String, Any>, email: String, type: REALM_OBJECT_TYPE)  {
        var realm = Realm.getInstance(Utilities.getRealmForUser(email, true))

        try {
            realm.executeTransaction {
                when(type) {
                    REALM_OBJECT_TYPE.PULSEPROFILE -> {
                        UserProfileDao.saveOrUpdateProfile(it, email,  data)
                    }
                    REALM_OBJECT_TYPE.PULSEAPPSTATE -> {
                        PulseAppStateDao.saveOrUpdateAppState(it,email,data)
                    }
                    REALM_OBJECT_TYPE.PULSEDEVICES -> {
                        if (data.containsKey("Serial")) {
                            val serial = data["Serial"] as String
                            PulseDeviceDao.saveOrUpdatePulseDevice(it,email,serial,data)
                        } else {
                            PulseDeviceDao.saveOrUpdatePulseDevice(it,email,"",data)
                        }
                    }
                    REALM_OBJECT_TYPE.PULSEDATAPUSH -> {
                        if (data.containsKey("Serial")) {
                            val serial = data["Serial"] as String
                            DataPushDao.saveOrUpdatePulseDataPush(it,email, serial, data)
                        } else {
                            DataPushDao.saveOrUpdatePulseDataPush(it,email,"",data)
                        }
                    }

                    REALM_OBJECT_TYPE.USERMODEL -> {
                        UserModelDAO.saveOrUpdateUser(it, email, data)
                    }

                    REALM_OBJECT_TYPE.SPCLIENTINFO -> {
                        SPCLientInfoDAO.saveOrUpdateClientInfo(it, email, data)
                    }
                }
            }
        } finally {
            close()
        }
    }

    fun getObject(email: String, type: REALM_OBJECT_TYPE, extra: HashMap<String, Any> = hashMapOf()): RealmObject? {
        realm = Realm.getInstance(Utilities.getRealmForUser(email, true))
        return when(type) {
            REALM_OBJECT_TYPE.USERMODEL -> {
                UserModelDAO.getUser(realm,email)
            }
            REALM_OBJECT_TYPE.SPCLIENTINFO -> {
                SPCLientInfoDAO.getClientInfo(realm,email)
            }
            REALM_OBJECT_TYPE.PULSEPROFILE -> {
                UserProfileDao.getUserProfile(realm, email)
            }

            REALM_OBJECT_TYPE.PULSEDATAPUSH -> {

                if (extra.containsKey("Serial")) {
                    val serial = extra["Serial"] as String
                    DataPushDao.getPushCredentials(realm,email,serial)
                } else {
                    null
                }
            }

            REALM_OBJECT_TYPE.PULSEDEVICES -> {
               if (extra.containsKey("Serial")) {
                   val serial = extra["Serial"] as String
                   PulseDeviceDao.getPulseDeviceWithSerial(realm,email,serial)
               } else {
                   PulseDeviceDao.getPulseDevice(realm,email)
               }
            }

            REALM_OBJECT_TYPE.PULSEAPPSTATE -> {
                PulseAppStateDao.getPulseAppState(realm, email)
            }

            else -> {
                null
            }
        }
    }

    fun removeObject(email: String, type: REALM_OBJECT_TYPE, extra: HashMap<String, Any> = hashMapOf()) {
        realm = Realm.getInstance(Utilities.getRealmForUser(email, true))
        try {
            realm.executeTransaction {
                when(type) {
                    REALM_OBJECT_TYPE.USERMODEL -> {
                        UserModelDAO.deleteUser(it,email)
                    }
                    REALM_OBJECT_TYPE.SPCLIENTINFO -> {
                        SPCLientInfoDAO.deleteClientInfo(it,email)
                    }
                    REALM_OBJECT_TYPE.PULSEPROFILE -> {
                        UserProfileDao.deleteUserProfile(it, email)
                    }

                    REALM_OBJECT_TYPE.PULSEDATAPUSH -> {
                        if (extra.containsKey("Serial")) {
                            val serial = extra["Serial"] as String
                            DataPushDao.deletePushCredentials(it, email, serial)
                        }
                    }

                    REALM_OBJECT_TYPE.PULSEDEVICES -> {
                        if (extra.containsKey("Serial")) {
                            val serial = extra["Serial"] as String
                            PulseDeviceDao.deletePulseDevice(it,email, serial)
                        }
                    }

                    REALM_OBJECT_TYPE.PULSEAPPSTATE -> {
                        PulseAppStateDao.deleteAppState(it, email)
                    }
                }
            }
        } finally {
            close()
        }
    }

    fun removeObjectWithParam(email: String, type: REALM_OBJECT_TYPE, param: String) {
        realm = Realm.getInstance(Utilities.getRealmForUser(email, true))
        try {
            realm.executeTransaction {
                when(type) {
                    REALM_OBJECT_TYPE.USERMODEL -> {
                        UserModelDAO.deleteUser(it,email)
                    }
                    REALM_OBJECT_TYPE.SPCLIENTINFO -> {
                        SPCLientInfoDAO.deleteClientInfo(it,email)
                    }
                    REALM_OBJECT_TYPE.PULSEPROFILE -> {
                        UserProfileDao.deleteUserProfile(it, email)
                    }

                    REALM_OBJECT_TYPE.PULSEDATAPUSH -> {
                        if (param.isNotEmpty()) {
                            DataPushDao.deletePushCredentials(it, email, param)
                        }
                    }

                    REALM_OBJECT_TYPE.PULSEDEVICES -> {
                        if (param.isNotEmpty()) {
                            PulseDeviceDao.deletePulseDevice(it,email, param)
                        }
                    }
                    REALM_OBJECT_TYPE.PULSEAPPSTATE -> {
                        PulseAppStateDao.deleteAppState(it, email)
                    }

                }
            }
        } finally {
            close()
        }
    }

    fun close() {
        //realm.close()
    }
}


/*
* UserModel Data Access Object
* */

object UserModelDAO {
    private val TAG = UserModelDAO::class.simpleName

    fun getUser(realm: Realm, email: String): UserModel? {
        val user = realm.where(UserModel::class.java).equalTo("Email", email).findFirst()
        Log.i(TAG, "Realm read object: $user")
        return user
    }

    fun saveUser(realm: Realm, user: RealmObject) {
        realm.copyToRealmOrUpdate(user)
        if (!realm.isAutoRefresh) {
            realm.refresh()
        }
        Log.i(TAG, "Realm persisted object: $user successfully}")
    }

    fun saveOrUpdateUser(realm: Realm, email: String, data: HashMap<String,Any>): UserModel? {
        var mUser: UserModel?

        mUser = realm.where(UserModel::class.java)
            .equalTo("Email", email).findFirst()

        return if (mUser != null) {
            mUser.initWithHash(data)
            mUser
        } else {
            mUser = realm.createObject(UserModel::class.java, email)
            mUser.initWithHash(data)
            mUser
        }

    }

    fun deleteUser(realm: Realm, email: String) {
        val user = realm.where(UserModel::class.java).equalTo("Email", email).findAll()
        user.deleteFirstFromRealm()
        Log.i(TAG, "Realm removed object: $user")
    }

}

/*
* SPClientInfo Data Access Object
* */

object SPCLientInfoDAO {
    private val TAG = SPCLientInfoDAO::class.simpleName
    fun getClientInfo(realm: Realm, email: String): SPClientInfo? {
        val info = realm.where(SPClientInfo::class.java).equalTo("Email", email).findFirst()
        Log.i(TAG, "Realm read object: $info")
        return info
    }

    fun saveClientInfo(realm: Realm, info: RealmObject) {
        realm.copyToRealmOrUpdate(info)
        Log.i(TAG, "Realm persisted object: $info successfully}")
    }

    fun saveOrUpdateClientInfo(realm: Realm, email: String, data: HashMap<String,Any>): SPClientInfo? {
        var mClientInfo: SPClientInfo?

        mClientInfo = realm.where(SPClientInfo::class.java)
            .equalTo("Email", email).findFirst()

        return if (mClientInfo != null) {
            mClientInfo.initWithHash(data)
            mClientInfo
        } else {
            mClientInfo = realm.createObject(SPClientInfo::class.java, email)
            mClientInfo.initWithHash(data)
            mClientInfo
        }

    }

    fun deleteClientInfo(realm: Realm, email: String) {
        val info = realm.where(SPClientInfo::class.java).equalTo("Email", email).findAll()
        info.deleteFirstFromRealm()
        Log.i(TAG, "Realm removed object: $info")
    }

}

/*
* UserProfile Data Access Object
* */

object UserProfileDao {
    private val TAG = UserProfileDao::class.simpleName

    fun getUserProfile(realm: Realm, email: String): PulseUserProfile? {
        val profile = realm.where(PulseUserProfile::class.java).equalTo("Email", email).findFirst()
        Log.i(TAG, "Realm getUserProfile read object: $profile")
        return profile
    }

    fun saveUserProfile(realm: Realm, profile: RealmObject) {
        realm.copyToRealmOrUpdate(profile)
        Log.i(TAG, "Realm persisted object: $profile successfully}")
    }

    fun saveOrUpdateProfile(realm: Realm, email: String, data: HashMap<String,Any>): PulseUserProfile? {
        /*var mProfile: PulseUserProfile? = null

        realm.executeTransaction {
            realm.use { realm ->
                mProfile = realm.where(PulseUserProfile::class.java)
                    .equalTo("Email", email).findFirst()

                if (mProfile != null) {
                    (mProfile as PulseUserProfile).initWithHash(data, false)
                } else {
                    mProfile = realm.createObject(PulseUserProfile::class.java)
                    (mProfile as PulseUserProfile).initWithHash(data, true)
                }
            }
        }
        return mProfile*/
        var mPulseProfile: PulseUserProfile?

        mPulseProfile = realm.where(PulseUserProfile::class.java).equalTo("Email", email).findFirst()
        return if (mPulseProfile != null) {
            mPulseProfile.initWithHash(data, false)
            mPulseProfile
        } else {
            mPulseProfile = realm.createObject(PulseUserProfile::class.java, email)
            mPulseProfile.initWithHash(data, true)
            mPulseProfile
        }
    }

    fun deleteUserProfile(realm: Realm, email: String) {
        val user = realm.where(PulseUserProfile::class.java).equalTo("Email", email).findAll()
        user.deleteFirstFromRealm()
        Log.i(TAG, "Realm removed object: $user")
    }
}

/*
* PulseDataPush Data Access Object
* */

object DataPushDao {
    private val TAG = DataPushDao::class.simpleName

    fun getPushCredentials(realm: Realm, email: String, serial: String): PulseDataPush? {
        val dataPush = realm.where(PulseDataPush::class.java).equalTo("Email", email).equalTo("Serial", serial).findFirst()
        Log.i(TAG, "Realm dataPush read object: $dataPush")
        return dataPush
    }

    fun savePushCredentials(realm: Realm, dataPush: RealmObject) {
        realm.copyToRealmOrUpdate(dataPush)
        Log.i(TAG, "Realm persisted object: $dataPush successfully}")
    }

    fun saveOrUpdatePulseDataPush(realm: Realm, email: String, serial: String, data: HashMap<String,Any>): PulseDataPush? {
        var mPushCredentials: PulseDataPush?

        println("saveOrUpdatePulseDataPush: $data")

        mPushCredentials = realm.where(PulseDataPush::class.java)
            .equalTo("Email", email).equalTo("Serial", serial).findFirst()

        return if (mPushCredentials != null) {
            mPushCredentials.initWithHash(data)
            mPushCredentials
        } else {
            val currentTimestamp = System.currentTimeMillis()
            mPushCredentials = realm.createObject(PulseDataPush::class.java, currentTimestamp.toString())
            mPushCredentials.initWithHash(data)
            mPushCredentials
        }

    }

    fun deletePushCredentials(realm: Realm, email: String, serial: String) {
        val credential = realm.where(PulseDataPush::class.java).equalTo("Email", email).equalTo("Serial", serial).findAll()
        credential.deleteFirstFromRealm()
        Log.i(TAG, "Realm removed object: $credential")
    }
}

/**
 * PulseDevices Data Access Object
 * */

object PulseDeviceDao {
    private val TAG = PulseDeviceDao::class.simpleName

    fun getPulseDevice(realm: Realm, email: String): PulseDevices? {
        val dataPush = realm.where(PulseDevices::class.java).equalTo("Email", email).findFirst()
        Log.i(PulseDeviceDao.TAG, "Realm pulse device read object: $dataPush")
        return dataPush
    }

    fun getPulseDeviceWithSerial(realm: Realm, email: String, serial: String): PulseDevices? {
        val dataPush = realm.where(PulseDevices::class.java).equalTo("Email", email).equalTo("Serial", serial).findFirst()
        Log.i(PulseDeviceDao.TAG, "Realm pulse device read object: $dataPush")
        return dataPush
    }

    fun savePulseDevice(realm: Realm, dataPush: RealmObject) {
        realm.copyToRealmOrUpdate(dataPush)
        Log.i(PulseDeviceDao.TAG, "Realm persisted object: $dataPush successfully}")
    }

    fun saveOrUpdatePulseDevice(realm: Realm, email: String, serial: String, data: HashMap<String,Any>): PulseDevices? {
        var mPulseDevice: PulseDevices?

        mPulseDevice = realm.where(PulseDevices::class.java).equalTo("Email", email).findFirst()
        return if (mPulseDevice != null) {
            mPulseDevice.initWithHash(data, false)
            mPulseDevice
        } else {
            mPulseDevice = realm.createObject(PulseDevices::class.java, email)
            mPulseDevice.initWithHash(data, true)
            mPulseDevice
        }

    }

    fun deletePulseDevice(realm: Realm, email: String, serial: String) {
        val credential = realm.where(PulseDataPush::class.java).equalTo("Email", email).equalTo("Serial", serial).findAll()
        credential.deleteFirstFromRealm()
        Log.i(PulseDeviceDao.TAG, "Realm removed object: $credential")
    }
}

/*
* PulseAppStates Data Access Object
* */

object PulseAppStateDao {
    private val TAG = PulseAppStateDao::class.simpleName

    fun getPulseAppState(realm: Realm, email: String): PulseAppStates? {
        val appState = realm.where(PulseAppStates::class.java).equalTo("Email", email).findFirst()
        Log.i(PulseAppStateDao.TAG, "Realm getPulseAppState read object: $appState")
        return appState
    }


    fun saveOrUpdateAppState(realm: Realm, email: String, data: HashMap<String,Any>): PulseAppStates? {
        var mAppState: PulseAppStates?

        mAppState = realm.where(PulseAppStates::class.java)
            .equalTo("Email", email).findFirst()

        if (mAppState != null) {
            mAppState.initWithHash(data, false)
        } else {
            mAppState = realm.createObject(PulseAppStates::class.java, email)
            mAppState.initWithHash(data, true)
        }
        return mAppState
    }

    fun deleteAppState(realm: Realm, email: String) {
        val appState = realm.where(PulseAppStates::class.java).equalTo("Email", email).findAll()
        appState.deleteFirstFromRealm()
        Log.i(PulseAppStateDao.TAG, "Realm removed object: $appState")
    }
}