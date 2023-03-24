package com.smartpods.android.pulseecho.Fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.tabs.TabLayout
import com.smartpods.android.pulseecho.Adapters.HeightTabPage
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.ViewModel.HeightSettingsViewModel
import kotlinx.android.synthetic.main.custom_main_tab_icon.view.*
import kotlinx.android.synthetic.main.height_settings_fragment.*


class HeightSettingsFragment : BaseFragment() {

    lateinit var tabLayout: TabLayout
    var tabSelected: Int = 0
    var iconSelected = arrayOf(
        R.drawable.height_sub_click,
        R.drawable.activity_sub_click
    )
    var iconUnselected = arrayOf(R.drawable.height_sub, R.drawable.activity_sub)

    companion object {
        fun newInstance() = HeightSettingsFragment()
    }

    private lateinit var viewModel: HeightSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.height_settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HeightSettingsViewModel::class.java)
        // TODO: Use the ViewModel
        tabLayout = activity?.findViewById(R.id.heightTablayout) ?: TabLayout(PulseApp.appContext)
        setTabView()
    }

    override fun onResume() {
        println("HeightSettingsFragment onResume")

        if (tabLayout !== null && heightViewPager !== null) {
            heightViewPager.currentItem = tabSelected

        }

        super.onResume()
        fragmentNavTitle("Home2", true, false)
    }

    private fun setTabView() {
        tabLayout.setSelectedTabIndicator(0)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.height_sub))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile_sub))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = activity?.let {
            HeightTabPage(
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

        heightViewPager.adapter = adapter
        heightViewPager.isPagingEnabled = false
        heightViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                println("onTabSelected : ${tab.position}")
                heightViewPager.currentItem = tab.position
                tabSelected = tab.position
                tab.customView?.customMainTabIcon?.setImageResource(iconSelected[tab.position])
                setTabTitle(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                println("onTabUnselected : ${tab.position}")
                resetTabIcons()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                heightViewPager.currentItem = tab.position
                println("onTabReselected : ${tab.position}")
            }
        })
        heightViewPager.currentItem = 0
        tabLayout.getTabAt(0)?.let {
            it.customView?.customMainTabIcon?.setImageResource(iconSelected[0])
        }
        setTabTitle(0)
    }

    fun setTabTitle(index: Int) {
        var screenTitle = ""
        when (index) {
            0 -> screenTitle = "Height Settings"
            1 -> screenTitle = "Activity Profile"
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