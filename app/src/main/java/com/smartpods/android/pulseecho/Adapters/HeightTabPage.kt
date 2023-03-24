package com.smartpods.android.pulseecho.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.smartpods.android.pulseecho.Fragments.Height.HeightProfileFragment
import com.smartpods.android.pulseecho.Fragments.Height.ActivityProfileFragment

internal class HeightTabPage(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                HeightProfileFragment()
            }
            1 -> {
                ActivityProfileFragment()
            }
            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}