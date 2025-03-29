package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPref // Declare SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Initialize SharedPref
        sharedPreferences = SharedPref(applicationContext)

        // Check login status using SharedPreferences
        Handler(Looper.getMainLooper()).postDelayed({
            if (sharedPreferences.isLoggedIn()) {
                when (sharedPreferences.getUserType()) {
                    "customers" -> {
                        startActivity(Intent(this, MainActivity::class.java)) // Redirect to Customer screen
                    }
                    "providers" -> {
                        startActivity(Intent(this, ServiceMainActivity::class.java)) // Redirect to Provider screen
                    }
                    "admin"->{
                        startActivity(Intent(this, AdminDashboardActivity::class.java)) // Redirect to admin screen

                    }
                    else -> {
                        startActivity(Intent(this, LoginActivity::class.java)) // If userType is null, go to login
                    }
                }
            } else {
                startActivity(Intent(this, boarding_screen_1::class.java)) // Redirect to login if not logged in
            }
            finish() // Close splash screen
        }, 2050)
    }
}
