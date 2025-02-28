package com.example.foryou
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foryou.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var doubleBackPressed = false  // Track back press

    // Fragment references
    private val homeFragment = HomeFragment()
    private val categoryFragment = CategoryFragment()
    private val historyFragment = HistoryFragment()
    private val profileFragment = ProfileFragment()

    private var activeFragment: Fragment = homeFragment  // Track active fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize fragments once
        supportFragmentManager.beginTransaction()
            .add(R.id.fview, homeFragment, "Home")
            .add(R.id.fview, categoryFragment, "Category").hide(categoryFragment)
            .add(R.id.fview, historyFragment, "History").hide(historyFragment)
            .add(R.id.fview, profileFragment, "Profile").hide(profileFragment)
            .commit()

        // Set up BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> showFragment(homeFragment)
                R.id.nav_cat -> showFragment(categoryFragment)
                R.id.nav_his -> showFragment(historyFragment)
                R.id.nav_per -> showFragment(profileFragment)
            }
            true
        }

        // Handle back button using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    // Function to show fragments without recreating them
    private fun showFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit()
            activeFragment = fragment
        }
    }

    // Custom back button handling
    private fun handleBackPress() {
        if (activeFragment is HomeFragment) {
            if (doubleBackPressed) {
                finish() // Exit app
            } else {
                doubleBackPressed = true
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackPressed = false
                }, 2000)
            }
        } else {
            binding.bottomNavigation.selectedItemId = R.id.nav_home  // Switch to HomeFragment
            showFragment(homeFragment)
        }
    }
}
