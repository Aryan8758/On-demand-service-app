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

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Spinner for user type selection (Customer or Provider)
        val userTypes = listOf("Select User Type", "customers", "providers")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        binding.userTypeSpinner.adapter = adapter

        // Login button click listener
        binding.loginbtn.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val selectedUserType = binding.userTypeSpinner.selectedItem.toString()

            // Input validation
            if (email.isEmpty()) {
                binding.emailEditText.error = "Please enter your email!"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordEditText.error = "Please enter your password!"
                return@setOnClickListener
            }
            if (selectedUserType == "Select User Type") {
                Toast.makeText(this, "Please select a valid user type!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show progress bar before authentication
            binding.progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            Log.d("LoginActivity", "User ID: $userId") // Log the UID to verify
                            db.collection(selectedUserType).document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val role = document.getString("role")
                                        Log.d("LoginActivity", "User role: $role") // Log the role for verification
                                        when (role) {
                                            "Customer" -> {
                                                // Hide progress bar after task completion
                                                binding.progressBar.visibility = View.GONE
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            }
                                            "Provider" -> {
                                                // Hide progress bar after task completion
                                                binding.progressBar.visibility = View.GONE
                                                startActivity(Intent(this, SignUp::class.java))
                                                finish()
                                            }
                                            else -> {
                                                // Hide progress bar after task completion
                                                binding.progressBar.visibility = View.GONE
                                                Toast.makeText(
                                                    this,
                                                    "Incorrect user type selected. Please select the correct user type.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        // Hide progress bar after task completion
                                        binding.progressBar.visibility = View.GONE
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                                        Log.e("LoginActivity", "User document not found for userId: $userId")
                                    }
                                }
                                .addOnFailureListener { exception ->

                                    Log.e("LoginActivity", "Error fetching user data: ${exception.message}")
                                }
                        }
                    } else {
                        // Hide progress bar after task completion
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Please check valid email and password", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Sign-up navigation
        binding.signintxt.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // Forgot password logic (if needed)
        binding.frgpass.setOnClickListener {
            // Add forgot password logic if needed
        }
    }
}
