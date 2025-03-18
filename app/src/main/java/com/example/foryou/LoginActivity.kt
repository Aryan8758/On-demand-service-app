package com.example.foryou

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.foryou.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var sharedPreferences: SharedPref
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedUserType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // 🔹 Spinner for user type selection
        val userTypes = listOf("Select User Type", "customers", "providers")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        binding.userTypeSpinner.adapter = adapter

        sharedPreferences = SharedPref(applicationContext)
//
//        // 🔹 Load saved email/password (Remember Me)
//        binding.emailEditText.setText(sharedPreferences.getSavedEmail())
//        binding.passwordEditText.setText(sharedPreferences.getSavedPassword())

        // 🔹 Login button click listener
        binding.loginbtn.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            selectedUserType = binding.userTypeSpinner.selectedItem.toString()
            // ✅ Hardcoded Admin Check
            if (email == "admin@gmail.com" && password == "shreya_24") {
                binding.progressBar.visibility = View.GONE
                binding.loginbtn.isEnabled = true
                sharedPreferences.saveLoginState(true) // Save login session
                sharedPreferences.saveUserType("admin") // Set user type as admin
                Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (validateInputs(email, password, selectedUserType)) {
                binding.loginbtn.isEnabled = false  // Disable button during login
                binding.progressBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.progressBar.visibility = View.GONE
                        binding.loginbtn.isEnabled = true  // Re-enable button

                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {

                                when (selectedUserType) {
                                    "customers" -> handleCustomerLogin(userId)
                                    "providers" -> handleProviderLogin(userId)
                                }
                            }
                        } else {
                            handleFirebaseAuthError(task.exception)
                        }
                    }
            }
        }
        binding.signUpText.setOnClickListener {
            startActivity(Intent(this,SignUp::class.java))
        }

        // 🔹 Forgot password logic
        binding.forgotPasswordText.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                binding.emailEditText.error = "Enter your email first!"
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun validateInputs(email: String, password: String, userType: String): Boolean {
        // ✅ Email format validation using Regex
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (email.isEmpty()) {
            binding.emailEditText.error = "Please enter your email!"
            return false
        }
        if (!email.matches(emailPattern.toRegex())) {
            binding.emailEditText.error = "Invalid email format! (e.g., example@gmail.com)"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordEditText.error = "Please enter your password!"
            return false
        }
        if (userType == "Select User Type") {
            Toast.makeText(this, "⚠️ Please select a valid user type!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    private fun handleCustomerLogin(userId: String) {
        db.collection("customers").document(userId).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document.exists()) {
                    updateFCMToken(userId)
                    sharedPreferences.saveLoginState(true)
                    sharedPreferences.saveUserType("customers")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "❌ Customer data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                showError("Error loading customer data. Please check your network.")
            }
    }

    private fun handleProviderLogin(userId: String) {
        db.collection("providers").document(userId).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document.exists()) {
                    val isApproved = document.getString("status") ?: "pending"
                    if (isApproved!="pending") {
                        updateFCMToken(userId)
                        sharedPreferences.saveLoginState(true)
                        sharedPreferences.saveUserType("providers")
                        startActivity(Intent(this, ServiceMainActivity::class.java))
                        finish()
                    } else {
                        showApprovalDialog()
                        Toast.makeText(this, "⚠️ Admin approval pending!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "❌ Provider account not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                showError("Error loading provider data. Please check your network.")
            }
    }

    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("LoginActivity", "❌ FCM token generation failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            val userRef = db.collection(selectedUserType).document(userId)
            userRef.update("fcmToken", fcmToken)
                .addOnSuccessListener {
                    Log.d("LoginActivity", "✅ FCM token updated in Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("LoginActivity", "❌ Failed to update FCM token: ${e.message}")
                }
        }
    }

    private fun handleFirebaseAuthError(exception: Exception?) {
        val errorMessage = when (exception?.message) {
            "The password is invalid or the user does not have a password." -> "⚠️ Incorrect password. Please try again."
            "There is no user record corresponding to this email." -> "⚠️ No account found. Please sign up first."
            else -> "Authentication failed. Please check your credentials."
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    //check provider is approved or pending
    private fun showApprovalDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Approval Pending")
            .setMessage("Your account is pending approval. Please wait for 2-3 hours.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false) // 👈 User cannot dismiss without clicking OK

        val dialog = builder.create() // ✅ Store the dialog instance

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED) // ✅ Correct usage
        }

        dialog.show() // ✅ Show the dialog
    }

}
