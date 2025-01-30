package com.example.foryou

import android.content.Context

class SharedPref(private val context: Context) {  // Change from ProfileFragment to Context
    // Get SharedPreferences instance
    private val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)  // Corrected the method name

    // Save login state
    fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    // Get login state
    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    // Logout user (clear login state)
    fun logoutUser() {
        val editor = sharedPref.edit()
        editor.remove("isLoggedIn") // Removes the login state
        editor.apply()
    }
}
