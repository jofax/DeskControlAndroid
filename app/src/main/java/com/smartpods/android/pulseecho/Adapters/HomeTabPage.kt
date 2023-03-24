package com.smartpods.android.pulseecho.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.smartpods.android.pulseecho.Fragments.Home.HomeHeartFragment
import com.smartpods.android.pulseecho.Fragments.Home.UserProfile
import com.smartpods.android.pulseecho.Fragments.Home.Survey
import com.smartpods.android.pulseecho.Fragments.Home.HomeNavigationFragment


internal class HomeTabPage(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                //HomeHeartFragment()
                HomeNavigationFragment()
            }
            1 -> {
                UserProfile()
            }
            2 -> {
                Survey()
            }
            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}