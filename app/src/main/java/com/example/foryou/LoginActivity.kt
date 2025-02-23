package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var sharedPreferences: SharedPref
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 🔹 Spinner for user type selection
        val userTypes = listOf("Select User Type", "customers", "providers")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        binding.userTypeSpinner.adapter = adapter

        sharedPreferences = SharedPref(applicationContext)

        // 🔹 Login button click listener
        binding.loginbtn.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val selectedUserType = binding.userTypeSpinner.selectedItem.toString()

            // 🔹 Input validation
            if (email.isEmpty()) {
                binding.emailEditText.error = "Please enter your email!"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordEditText.error = "Please enter your password!"
                return@setOnClickListener
            }
            if (selectedUserType == "Select User Type") {
                Toast.makeText(this, "⚠️ Please select a valid user type!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Show progress bar before authentication
            binding.progressBar.visibility = View.VISIBLE

            // 🔹 Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE // Hide progress bar after completion

                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            Log.d("LoginActivity", "✅ User ID: $userId")

                            if (selectedUserType == "customers") {
                                handleCustomerLogin(userId)
                            } else if (selectedUserType == "providers") {
                                handleProviderLogin(userId)
                            }
                        } else {
                            Log.e("LoginActivity", "❌ User ID is null after login.")
                            Toast.makeText(this, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = task.exception?.localizedMessage ?: "Invalid email or password. Please try again."
                        Log.e("LoginActivity", "❌ Authentication failed: $errorMessage")
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 🔹 Sign-up navigation
        binding.signintxt.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // 🔹 Forgot password logic (if needed)
        binding.frgpass.setOnClickListener {
            // TODO: Implement forgot password feature
        }
    }

    // 🔹 Handle customer login
    private fun handleCustomerLogin(userId: String) {
        db.collection("customers").document(userId).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE

                if (document.exists()) {
                    sharedPreferences.saveLoginState(true)
                    Log.d("LoginActivity", "✅ Customer login successful.")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "❌ Customer data not found.", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "❌ No customer document for userId: $userId")
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Log.e("LoginActivity", "❌ Error fetching customer data: ${exception.message}")
                Toast.makeText(this, "Error loading user data. Please check your network.", Toast.LENGTH_LONG).show()
            }
    }

    // 🔹 Handle provider login (searching in all categories)
    private fun handleProviderLogin(userId: String) {
      //  val db = FirebaseFirestore.getInstance()

        db.collection("Providers")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        Log.d("Firestore", "Category: ${document.id}")  // Logs categories like Babysitter, Maid
                    }
                } else {
                    Log.e("Firestore", "❌ No provider categories found.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Error fetching provider categories: ${e.message}")
            }

//        db.collection("Providers") // Start from "Providers" collection
//            .get()
//            .addOnSuccessListener { categories ->
//                if (categories.isEmpty) {
//                    Log.e("LoginActivity", "❌ No provider categories found.")
//                    Toast.makeText(this, "No provider data found.", Toast.LENGTH_SHORT).show()
//                    return@addOnSuccessListener
//                }
//
//                var providerFound = false // Flag to check if provider exists
//
//                for (category in categories) {
//                    val categoryName = category.id // "Babysitter", "Maid", etc.
//                    db.collection("Providers").document(categoryName).collection("Users")
//                        .document(userId)
//                        .get()
//                        .addOnSuccessListener { document ->
//                            if (document.exists()) {
//                                providerFound = true
//                                sharedPreferences.saveLoginState(true)
//                                Log.d("LoginActivity", "✅ Provider login successful in category: $categoryName")
//
//                                startActivity(Intent(this, MainActivity::class.java))
//                                finish()
//                                return@addOnSuccessListener // Exit loop after successful login
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            Log.e("LoginActivity", "❌ Error checking provider data in $categoryName: ${exception.message}")
//                        }
//                }
//
//                // If no provider is found, show error
//                if (!providerFound) {
//                    Toast.makeText(this, "❌ Provider data not found.", Toast.LENGTH_SHORT).show()
//                    Log.e("LoginActivity", "❌ No provider document found for userId: $userId")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("LoginActivity", "❌ Error loading provider categories: ${exception.message}")
//                Toast.makeText(this, "Error loading provider data. Please check your network.", Toast.LENGTH_LONG).show()
//            }
    }

}
