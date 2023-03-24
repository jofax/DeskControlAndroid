package com.smartpods.android.pulseecho.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.smartpods.android.pulseecho.Fragments.Settings.DeskModeFragment
import com.smartpods.android.pulseecho.Fragments.Settings.DeskSensorFragment
import com.smartpods.android.pulseecho.Fragments.Settings.PasswordChangeFragment
import com.smartpods.android.pulseecho.Fragments.Settings.AppVersionFragment

internal class SettingsTabPage(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                DeskModeFragment()
            }
            1 -> {
                DeskSensorFragment()
            }
            2 -> {
                PasswordChangeFragment()
            }
            3 -> {
                AppVersionFragment()
            }
            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}