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
                // If the user is already logged in, navigate to the main activity or dashboard
                startActivity(Intent(this, MainActivity::class.java)) // Replace MainActivity with the actual main screen activity
            } else {
                // If the user is not logged in, navigate to the login screen
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // Close the splash screen activity
        }, 2050)
    }
}
