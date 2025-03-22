package com.example.foryou

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatusViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4  // 4 tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StatusListFragment("Accepted")
            1 -> StatusListFragment("Rejected")
            2 -> StatusListFragment("Order Cancel")
            3 -> StatusListFragment("Completed")
            else -> throw IllegalStateException("Invalid position")
        }
    }
}
