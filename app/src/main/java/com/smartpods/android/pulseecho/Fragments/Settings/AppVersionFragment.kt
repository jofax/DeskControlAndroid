package com.smartpods.android.pulseecho.Fragments.Settings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.BuildConfig
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import com.smartpods.android.pulseecho.ViewModel.AppVersionViewModel
import kotlinx.android.synthetic.main.activity_profile_fragment.*
import kotlinx.android.synthetic.main.app_version_fragment.*
import org.jetbrains.anko.support.v4.toast

class AppVersionFragment : BaseFragment() {

    companion object {
        fun newInstance() = AppVersionFragment()
    }

    private lateinit var viewModel: AppVersionViewModel
    var fwVersion: String = ""

    private val spDataParseEventListener by lazy {
        var progress = 0
        DataEventListener().apply {
            onIdentifierDataEventReceived = {

                if (fwVersion != it.Version) {
                    fwVersion = it.Version
                    deskFWVersionTitle?.text = fwVersion

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.app_version_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AppVersionViewModel::class.java)
        // TODO: Use the ViewModel
        SPDataParser.registerListener(spDataParseEventListener)
        this.populateBuildVersionContent()
        getBoxData()

    }

    fun populateBuildVersionContent() {
        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val appVersion = getString(R.string.app_ver) + " " + versionName
        appVersionTitle.text = appVersion
    }

    fun getBoxData() {
        if (pulseMainActivity.deviceIsInitialized()) {
            if (pulseMainActivity.deviceConnected &&  pulseMainActivity.SPBleDevice.isConnected()) {
                SPBleManager.sendCommandToCharacteristic(pulseMainActivity.SPBleDevice,
                    SPRequestParameters.Information)
            }
        } else {
            toast(R.string.device_not_connected)
        }


    }
}