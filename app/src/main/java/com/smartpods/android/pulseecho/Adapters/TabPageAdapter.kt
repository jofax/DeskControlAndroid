package com.smartpods.android.pulseecho.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.smartpods.android.pulseecho.Fragments.HeightSettingsFragment
import com.smartpods.android.pulseecho.Fragments.HomeFragment
import com.smartpods.android.pulseecho.Fragments.SettingsFragment
import com.smartpods.android.pulseecho.Fragments.StatisticFragment

internal class TabPageAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) :
    FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                HomeFragment.newInstance()
            }
            1 -> {
                HeightSettingsFragment.newInstance()
            }
            2 -> {
                StatisticFragment.newInstance()
            }
            3 -> {
                SettingsFragment.newInstance()
            }
            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}