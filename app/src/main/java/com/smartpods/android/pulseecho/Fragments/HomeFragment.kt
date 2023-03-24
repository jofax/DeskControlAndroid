package com.smartpods.android.pulseecho.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.smartpods.android.pulseecho.Adapters.HomeTabPage
import com.smartpods.android.pulseecho.BaseFragment
import com.smartpods.android.pulseecho.PulseApp.Companion.appContext
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.and
import com.smartpods.android.pulseecho.ViewModel.HomeViewModel
import kotlinx.android.synthetic.main.custom_main_tab_icon.view.*
import kotlinx.android.synthetic.main.home_fragment.*

class HomeFragment : BaseFragment() {

    lateinit var tabLayout: TabLayout
    var tabSelected: Int = 0
    var iconSelected = arrayOf(
        R.drawable.home_sub_click,
        R.drawable.profile_sub_click,
        R.drawable.survey_sub_click
    )
    var iconUnselected = arrayOf(R.drawable.home_sub, R.drawable.profile_sub, R.drawable.survey_sub)

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
        tabLayout = activity?.findViewById(R.id.homeTablayout) ?: TabLayout(appContext)
        setTabView()
    }

    override fun onPause() {
        super.onPause()
        println("homeFragment onRefresh")
    }

    fun onRefresh(v: View) {
        println("homeFragment onRefresh")
    }

    override fun onResume() {
        println("homeFragment onResume")

    if (tabLayout !== null && homeViewPager !== null) {
        homeViewPager.currentItem = tabSelected
        //tabLayout.setSelectedTabIndicator(tabSelected)
    }
        super.onResume()


        //fragmentNavTitle("Home 3", true, false)
    }

    private fun setTabView() {
        tabLayout.setSelectedTabIndicator(0)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.home_sub))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile_sub))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.survey_sub))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = activity?.let {
            HomeTabPage(
                appContext, childFragmentManager,
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

        homeViewPager.adapter = adapter
        homeViewPager.isPagingEnabled = false
        homeViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                println("onTabSelected : ${tab.position}")
                homeViewPager.currentItem = tab.position
                tabSelected = tab.position
                tab.customView?.customMainTabIcon?.setImageResource(iconSelected[tab.position])
                setTabTitle(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
//                homeViewPager.currentItem = tab.position
                //              tab.customView?.customMainTabIcon?.setImageResource(iconUnselected[tab.position])
                //             setTabTitle(tab.position)
                println("onTabUnselected : ${tab.position}")
                resetTabIcons()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                homeViewPager.currentItem = tab.position
                println("onTabReselected : ${tab.position}")
            }
        })
        homeViewPager.currentItem = 0
        tabLayout.getTabAt(0)?.let {
            it.customView?.customMainTabIcon?.setImageResource(iconSelected[0])
        }
        setTabTitle(0)
    }

    fun setTabTitle(index: Int) {
        var screenTitle = ""
        when (index) {
            0 -> screenTitle = "Home"
            1 -> screenTitle = "User Profile"
            2 -> screenTitle = "Survey"
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


