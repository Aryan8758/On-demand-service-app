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
        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // 🔹 Forgot password logic (if needed)
        binding.forgotPasswordText.setOnClickListener {
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
                    sharedPreferences.saveUserType("customers")
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

    private fun handleProviderLogin(userId: String) {
        db.collection("providers").document(userId).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE

                if (document.exists()) {
                    sharedPreferences.saveLoginState(true)
                    sharedPreferences.saveUserType("providers")
                    startActivity(Intent(this, ServiceMainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "❌ Provider data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading user data. Please check your network.", Toast.LENGTH_LONG).show()
            }
    }


}
