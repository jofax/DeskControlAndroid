package com.smartpods.android.pulseecho.Utilities

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object UserPreference {
    lateinit var encryptedPrefs: AppPreference
    lateinit var prefs: AppPreference

    // Add your key strings here...

    fun initEncryptedPrefs(context: Context) {
        encryptedPrefs =
            AppPreference(
                "ENCRYPTED_PREFS",
                context,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
    }

    fun initPrefs(context: Context) {
        prefs = AppPreference("PREFS", context)
    }
}