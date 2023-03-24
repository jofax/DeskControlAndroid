package com.smartpods.android.pulseecho.Fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.tabs.TabLayout
import com.smartpods.android.pulseecho.Adapters.SettingsTabPage
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.ViewModel.SettingsViewModel
import kotlinx.android.synthetic.main.custom_main_tab_icon.view.*
import kotlinx.android.synthetic.main.height_settings_fragment.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : BaseFragment() {

    lateinit var tabLayout: TabLayout
    var tabSelected: Int = 0
    var iconSelected = arrayOf(
        R.drawable.desk_sub_click,
        R.drawable.sensor_sub_click,
        R.drawable.credential_sub_click,
        R.drawable.version_sub_click,
    )
    var iconUnselected = arrayOf(
        R.drawable.desk_sub,
        R.drawable.sensor_sub,
        R.drawable.credential_sub,
        R.drawable.version_sub,
    )

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        // TODO: Use the ViewModel

        tabLayout = activity?.findViewById(R.id.settingsTablayout) ?: TabLayout(PulseApp.appContext)
        setTabView()
    }

    override fun onResume() {
        println("homeFragment onResume")

        if (tabLayout !== null && settingsViewPager !== null) {
            settingsViewPager.currentItem = tabSelected
        }

        super.onResume()
    }

    private fun setTabView() {
        tabLayout.setSelectedTabIndicator(0)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.desk_sub))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.sensor_sub))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.credential_sub))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.version_sub))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = activity?.let {
            SettingsTabPage(
                PulseApp.appContext, childFragmentManager,
                tabLayout.tabCount
            )
        }

        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.let {
                val imgView: View = layoutInflater.inflate(R.layout.custom_main_tab_icon, null)
                val imgIcon: ImageView = imgView.findViewById(R.id.customMainTabIcon)
                //imgIcon.setImageResource(iconUnselected[i])
                //it.setCustomView(imgIcon)

                if (iconUnselected[i] !== null) {
                    imgIcon.setImageResource(iconUnselected[i])
                    it.setCustomView(imgIcon)
                }
            }
        }

        settingsViewPager.adapter = adapter
        settingsViewPager.isPagingEnabled = false
        settingsViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                println("onTabSelected : ${tab.position}")
                settingsViewPager.currentItem = tab.position
                tabSelected = tab.position
                tab.customView?.customMainTabIcon?.setImageResource(iconSelected[tab.position])
                setTabTitle(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                println("onTabUnselected : ${tab.position}")
                resetTabIcons()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                settingsViewPager.currentItem = tab.position
                println("onTabReselected : ${tab.position}")
            }
        })
        settingsViewPager.currentItem = 0
        tabLayout.getTabAt(0)?.let {
            it.customView?.customMainTabIcon?.setImageResource(iconSelected[0])
        }
        setTabTitle(0)
    }

    fun setTabTitle(index: Int) {
        var screenTitle = ""
        when (index) {
            0 -> screenTitle = "Desk Mode"
            1 -> screenTitle = "Desk Controls"
            2 -> screenTitle = "Password Change"
            3 -> screenTitle = "Software Version"
        }

        print("tab index $index")
    }

    fun resetTabIcons() {
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.let {
                it.customView?.customMainTabIcon?.setImageResource(iconUnselected[i])
            }
        }
    }
}