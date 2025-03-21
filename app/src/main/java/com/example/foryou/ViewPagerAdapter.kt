package com.example.foryou


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int = 4  // 4 Tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryTabFragment("Pending")
            1 -> HistoryTabFragment("Accepted")
            2 -> HistoryTabFragment("Rejected")
            3 -> HistoryTabFragment("Order Cancel")
            else -> HistoryTabFragment("Pending")
        }
    }
}
