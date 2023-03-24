package com.smartpods.android.pulseecho.Fragments

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.tabs.TabLayout
import com.smartpods.android.pulseecho.Adapters.StatisticTabPage
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.PulseApp
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.ViewModel.StatisticViewModel
import kotlinx.android.synthetic.main.custom_main_tab_icon.view.*
import kotlinx.android.synthetic.main.statistic_fragment.*

class StatisticFragment : BaseFragment() {


    lateinit var tabLayout: TabLayout
    var tabSelected: Int = 0
    var iconSelected = arrayOf(
        R.drawable.desk_stat_sub_click,
    )
    var iconUnselected = arrayOf(
        R.drawable.desk_stat_sub,
    )

    companion object {
        fun newInstance() = StatisticFragment()
    }

    private lateinit var viewModel: StatisticViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.statistic_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StatisticViewModel::class.java)
        // TODO: Use the ViewModel

        tabLayout = activity?.findViewById(R.id.statisticTablayout) ?: TabLayout(PulseApp.appContext)
        setTabView()
    }

    override fun onResume() {
        println("homeFragment onResume")

        if (tabLayout !== null && statisticViewPager !== null) {
            statisticViewPager.currentItem = tabSelected
        }

        super.onResume()
        //fragmentNavTitle("Home", true, false)
        //pulseMainActivity.navButtonToggle("asdasd",true, true, false)
    }

    private fun setTabView() {
        tabLayout.setSelectedTabIndicator(0)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.desk_stat_sub))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = activity?.let {
            StatisticTabPage(
                PulseApp.appContext, childFragmentManager,
                tabLayout.tabCount
            )
        }

        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.let {
                val imgView: View = layoutInflater.inflate(R.layout.custom_main_tab_icon, null)
                val imgIcon: ImageView = imgView.findViewById(R.id.customMainTabIcon)

                if (iconUnselected[i] !== null) {
                    imgIcon.setImageResource(iconUnselected[i])
                    it.setCustomView(imgIcon)
                }
            }
        }

        statisticViewPager.adapter = adapter
        statisticViewPager.isPagingEnabled = false
        statisticViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                println("onTabSelected : ${tab.position}")
                statisticViewPager.currentItem = tab.position
                tabSelected = tab.position
                tab.customView?.customMainTabIcon?.setImageResource(iconSelected[tab.position])
                setTabTitle(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                println("onTabUnselected : ${tab.position}")
                resetTabIcons()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                statisticViewPager.currentItem = tab.position
                println("onTabReselected : ${tab.position}")
            }
        })
        statisticViewPager.currentItem = 0
        tabLayout.getTabAt(0)?.let {
            it.customView?.customMainTabIcon?.setImageResource(iconSelected[0])
        }
        setTabTitle(0)
    }

    fun setTabTitle(index: Int) {
        var screenTitle = ""
        when (index) {
            0 -> screenTitle = "Desk Statistics"
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