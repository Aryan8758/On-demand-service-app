package com.example.foryou


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foryou.databinding.ActivityServiceMainBinding

class ServiceMainActivity : AppCompatActivity() {
    private val binding: ActivityServiceMainBinding by lazy {
        ActivityServiceMainBinding.inflate(layoutInflater)
    }

    private var doubleBackPressed = false  // Variable to track back press

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Set the default fragment to HomeFragment
        loadFragment(HomeFragment())

        // Set up BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_his -> loadFragment(HistoryFragment())
                R.id.nav_per -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    // Function to load fragments
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fview, fragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fview)

        if (currentFragment is HomeFragment) {
            if (doubleBackPressed) {
                super.onBackPressed() // Exit app
            } else {
                doubleBackPressed = true
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

                // Reset back press after 2 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackPressed = false
                }, 2000)
            }
        } else {
            binding.bottomNavigation.selectedItemId = R.id.nav_home  // Switch to HomeFragment
        }
    }
}
