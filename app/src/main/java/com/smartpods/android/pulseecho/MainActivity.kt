package com.smartpods.android.pulseecho

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.smartpods.android.pulseecho.Adapters.TabPageAdapter
import com.smartpods.android.pulseecho.Constants.Constants
import com.smartpods.android.pulseecho.Interfaces.CoreObject
import com.smartpods.android.pulseecho.Utilities.*
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleEventListener
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager
import com.smartpods.android.pulseecho.Utilities.BLE.SPBleManager.isConnected
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.DataEventListener
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPDataParser
import com.smartpods.android.pulseecho.Utilities.ParserAndCommand.SPRequestParameters
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_main_tab_icon.view.*
import info.androidhive.fontawesome.FontDrawable
import kotlinx.android.synthetic.main.sp_with_menu_action_bar.*

class MainActivity : BaseActivity() {

    //lateinit var tabLayout: TabLayout
    //lateinit var viewPager: ViewPager

    var iconSelected = arrayOf(R.drawable.home_click, R.drawable.height_settings_click, R.drawable.statistics_click, R.drawable.settings_click)
    var iconUnselected = arrayOf(R.drawable.home, R.drawable.height_settings, R.drawable.statistics, R.drawable.settings)

    var screenTitle = ""
    var bleConnected = false
    var isDeskControlActive = false

    var heartBeatSentCount = 0
    var heartBeatSentLimit = 1

    private val connectionEventListener by lazy {
        SPBleEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                println("MainActivity onConnectionSetupComplete bond state: " + gatt.device.bondState)
                bleConnected = gatt.device.bondState == 12
                SPBleStatusIndicator(bleConnected, false)

                SPBleManager.startBleStream(Utilities.getSmartpodsDevice())
                SPBleManager.sendHeartBeatWithDevice(Utilities.getSmartpodsDevice())

            }
            onDisconnect = {
                println("MainActivity onDisconnect: " + it)
            }

            onNotificationsDisabled = { device, gattChar ->

                if (gattChar.value != null) {
                    //println("MainActivity onNotificationsDisabled: " + device)
                    //println("MainActivity onNotificationsDisabled: " + gattChar.value.toHexString())
                }
            }
            onNotificationsEnabled = { device, gattChar ->
                //println("MainActivity onNotificationsEnabled: " + device)
                //println("MainActivity onNotificationsEnabled: " + gattChar.value.toHexString())
            }

            onCharacteristicWrite = { device, gattChar ->
                if (gattChar.value != null) {
                    //println("MainActivity onCharacteristicWrite: " + device)
                    //println("MainActivity onCharacteristicWrite: " + gattChar.value.toHexString())
                }
            }

            onCharacteristicRead = { device, desc ->

                //println("MainActivity onCharacteristicRead: " + device)
                //println("MainActivity onCharacteristicRead: " + desc)
            }

            onCharacteristicChanged = { device, gattChar ->
                if (gattChar.value != null) {
                    //println("MainActivity onCharacteristicChanged: " + device)
                    //println("MainActivity onCharacteristicChanged: " + gattChar.value.toHexString())
                    //SPDataParser.initWithString(gattChar.value)

                }
            }

            onDescriptorWrite = { device, desc ->
                //println("MainActivity onDescriptorWrite: " + device)
                //println("MainActivity onDescriptorWrite: " + desc)
            }

            onDescriptorRead = { device, desc ->
                //println("MainActivity onDescriptorRead: " + device)
                //println("MainActivity onDescriptorRead: " + desc)
            }

            onMtuChanged = { gatt, obj ->
                //println("MainActivity onMtuChanged: " + gatt)
                //println("MainActivity onMtuChanged: " + obj)
            }

            onBondComplete =  { gatt, obj ->
                //println("MainActivity onBondComplete: " + gatt)
                //println("MainActivity onBondComplete: " + obj)
            }

            onPairedComplete =  { gatt ->
                finish()
                //println("MainActivity onPairedComplete: " + gatt)
            }

            onDeviceConnectivityError =  { device, message ->

                //println("MainActivity onDeviceConnectivityError: Device - {$device} Message - {$message}")
                toast(applicationContext, message)
            }

            onDeviceDataError =  { device, message ->
                println("MainActivity onDeviceDataError: Device - {$device} Message - {$message}")
            }
        }
    }

    private val spDataParseEventListener by lazy {
        DataEventListener().apply {
            onCoreDataEventReceived = {
//                if (it.HeartBeatOut == true) {
//                    if (SPBleDevice.isConnected()) {
//
//                        println("MainActivity CORE OBJECT: ${it.HeartBeatOut}")
//                        println("heartBeatSentCount: $heartBeatSentCount")
//                        println("check if device connected:  ${SPBleDevice.isConnected()}")
//
//                        //SPBleManager.sendHeartBeatWithDevice(SPBleDevice)
//                        SPBleManager.sendHeartBeatWithDevice(Utilities.getSmartpodsDevice())
//                    }
//                } else {
//                    heartBeatSentCount = 0
//                }

                if (it.Movingupstatus) {
                    deskActionIndicators(0)
                } else if (it.Movingdownstatus) {
                    deskActionIndicators(1)
                } else {
                    deskActionIndicators(3)
                }

            }

        }
    }


    private val spAppScreenEventHandler by lazy {
        MainTabEventHandler().apply {
            onSelectTabScreen = {
                tabLayout.getTabAt(it)?.select()
                tabLayout.getTabAt(it)?.let {
                    it.customView?.customMainTabIcon?.setImageResource(iconSelected[0])
                }
                setTabTitle(it)
                println("spAppScreenEventHandler called")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTabView()
        SPBleManager.registerListener(connectionEventListener)
        SPDataParser.registerListener(spDataParseEventListener)
        SPAppEventHandler.registerListener(spAppScreenEventHandler)
        setTabTitle(tabLayout.selectedTabPosition)
        box_controls_linear_layout.slideAnimation(SlideDirection.RIGHT, SlideType.HIDE)

        val email = UserPreference.prefs.read(Constants.USER_EMAIL, "")
        if (email != null) {
            if (email.isNotEmpty()) {
                setCustomActionBar(screenTitle, email, true, false, deviceConnected)
            } else {
                setCustomActionBar(screenTitle, "", true, false, deviceConnected)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        println("main activity on resume")
        println("view visible: ")

        val _test = listOf(20,1,2,183,0,0,112,4,11,182,5,0,1,119,34,8,0,0,248,181)
        val _obj = _test.map { it.toByte()  }

        Utilities.validCRC16(_obj.toByteArray())

    }

    fun setTabView() {
        //tabLayout = findViewById(R.id.tabLayout)
        //viewPager = findViewById(R.id.viewPager)
        tabLayout.setSelectedTabIndicator(0)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.home))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.height_settings))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.statistics))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.settings))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = TabPageAdapter(
            this, supportFragmentManager,
            tabLayout.tabCount
        )

        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.let {
                val imgView: View = layoutInflater.inflate(R.layout.custom_main_tab_icon, null)
                val imgIcon: ImageView = imgView.findViewById(R.id.customMainTabIcon)
                imgIcon.setImageResource(iconUnselected[i])
                it.setCustomView(imgIcon)
            }
        }

        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Handler(Looper.getMainLooper()).post {
                    viewPager.currentItem = tab.position
                    tab.customView?.customMainTabIcon?.setImageResource(iconSelected[tab.position])
                    setTabTitle(tab.position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                Handler(Looper.getMainLooper()).post {
                    viewPager.currentItem = tab.position
                    tab.customView?.customMainTabIcon?.setImageResource(iconUnselected[tab.position])
                    setTabTitle(tab.position)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        tabLayout.getTabAt(0)?.let {
            it.customView?.customMainTabIcon?.setImageResource(iconSelected[0])
        }
        setTabTitle(0)

        box_controls_linear_layout.bringToFront()
        val closeDrawer = FontDrawable(this, R.string.fa_angle_double_right_solid, true, false)
        btn_close_drawer.setImageDrawable(closeDrawer)
        btn_close_drawer.setOnClickListener{
            btn_desk_control.backgroundTintList = ColorStateList.valueOf(Utilities.getCustomRawColor(R.color.smartpods_blue))
            box_controls_linear_layout.slideAnimation(SlideDirection.RIGHT, SlideType.HIDE)
            btn_desk_control.visibility = View.VISIBLE
        }

        btn_desk_control.setOnClickListener{
            isDeskControlActive = !isDeskControlActive
            println("fab_desk_quick_controls clicked: $isDeskControlActive")
            btn_desk_control.backgroundTintList = ColorStateList.valueOf(Utilities.getCustomRawColor(R.color.smartpods_green))
            box_controls_linear_layout.slideAnimation(SlideDirection.LEFT, SlideType.SHOW)
            it.visibility = View.GONE
//            if (isDeskControlActive) {
//                fab_desk_quick_controls.backgroundTintList = ColorStateList.valueOf(Utilities.getCustomRawColor(R.color.smartpods_green))
//                box_controls_linear_layout.slideAnimation(SlideDirection.LEFT, SlideType.SHOW)
//                it.visibility = View.GONE
//
//            } else {
//                fab_desk_quick_controls.backgroundTintList = ColorStateList.valueOf(Utilities.getCustomRawColor(R.color.smartpods_blue))
//                box_controls_linear_layout.slideAnimation(SlideDirection.RIGHT, SlideType.HIDE)
//                it.visibility = View.VISIBLE
//            }


        }

        btn_stand_desk_action.setOnClickListener{
            println("btn_stand_desk_action clicked")
            deskActionIndicators(0)
            moveDeskUp()

        }

        btn_sit_desk_action.setOnClickListener {
            println("btn_sit_desk_action clicked")
            deskActionIndicators(1)
            moveDeskDown()
        }


        btn_stop_desk_action.setOnClickListener{
            println("btn_stop_desk_action clicked")
            deskActionIndicators(2)
            stopDeskMovement()
        }
    }

    fun deskActionIndicators(tag: Int) {
        when(tag) {
            0 -> {
                btn_stand_desk_action.setImageDrawable(getDrawable(R.drawable.stand_click))
                btn_sit_desk_action.setImageDrawable(getDrawable(R.drawable.sit))
                btn_stop_desk_action.setImageDrawable(getDrawable(R.drawable.stop))
               }
            1 -> {
                btn_stand_desk_action.setImageDrawable(getDrawable(R.drawable.stand))
                btn_sit_desk_action.setImageDrawable(getDrawable(R.drawable.sit_click))
                btn_stop_desk_action.setImageDrawable(getDrawable(R.drawable.stop))
            }
            2 -> {
                btn_stand_desk_action.setImageDrawable(getDrawable(R.drawable.stand))
                btn_sit_desk_action.setImageDrawable(getDrawable(R.drawable.sit))
                btn_stop_desk_action.setImageDrawable(getDrawable(R.drawable.stop_click))
            }
            else ->  {
                btn_stand_desk_action.setImageDrawable(getDrawable(R.drawable.stand))
                btn_sit_desk_action.setImageDrawable(getDrawable(R.drawable.sit))
                btn_stop_desk_action.setImageDrawable(getDrawable(R.drawable.stop))
            }
        }
    }

    fun setTabTitle(index: Int) {
        when (index) {
            0 -> screenTitle = "Home"
            1 -> screenTitle = "Height Settings"
            2 -> screenTitle = "Desk Statistics"
            3 -> screenTitle = "Settings"
        }

        print("tab index $index")
        setScreenHeader(bleConnected)

    }

    fun setScreenHeader(ble: Boolean) {

        val email = UserPreference.prefs.read(Constants.USER_EMAIL, "")
        if (email != null) {
            if (email.isNotEmpty()) {
                setViewTitle(screenTitle)
            } else {
                setViewTitle(screenTitle)
            }
        }
    }

    fun resetTabIcons() {
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.let {
                it.customView?.customMainTabIcon?.setImageResource(iconUnselected[i])
            }
        }
    }

}