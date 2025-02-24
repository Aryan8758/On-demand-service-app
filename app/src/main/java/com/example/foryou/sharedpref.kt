package com.example.foryou

import android.content.Context
import android.util.Log

class SharedPref(private val context: Context) {

    // Get SharedPreferences instance
    private val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val LOCATION_KEY = "user_location"
        private const val LOGIN_KEY = "isLoggedIn"
    }

    // Save login state
    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPref.edit().putBoolean(LOGIN_KEY, isLoggedIn).apply()
    }

    // Get login state
    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean(LOGIN_KEY, false)
    }

    // Logout user (clear login state)
    fun logoutUser() {
        sharedPref.edit().remove(LOGIN_KEY).remove(LOCATION_KEY).apply()
        Log.d("SharedPref", "✅ User logged out & location removed")
    }



    fun saveLocation(location: String) {
        sharedPref.edit().putString(LOCATION_KEY, location).apply()
        Log.d("Location", "Location saved: $location")
    }


    // Get location
    fun getLocation(): String? {
        return sharedPref.getString(LOCATION_KEY, null)
    }
    fun saveUserType(type: String) {
        sharedPref.edit().putString("userType", type).apply()
    }

    fun getUserType(): String {
        return sharedPref.getString("userType", "customer") ?: "customer"
    }

}
