@file:Suppress("DEPRECATION")

package com.example.foryou

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foryou.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding:ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Set the default fragment to HomeFragment
        loadFragment(HomeFragment())

        // Set up BottomNavigationView
       binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_cat -> {
                    loadFragment(CategoryFragment())
                    true
                }
                R.id.nav_his -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_per -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Function to load fragments
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fview, fragment)
        transaction.addToBackStack(null)  // Optional: to allow back navigation
        transaction.commit()
    }
}
