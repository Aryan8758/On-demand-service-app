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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Set default fragment to HomeFragment
        loadFragment(HomeFragment())

        // Set up BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_cat -> loadFragment(CategoryFragment())
                R.id.nav_his -> loadFragment(HistoryFragment())
                R.id.nav_per -> loadFragment(ProfileFragment())
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

    // Function to load fragments
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fview, fragment)
        transaction.commit()
    }

    // Custom back button handling
    private fun handleBackPress() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fview)

        if (currentFragment is HomeFragment) {
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
        }
    }
}
