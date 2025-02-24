package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    // Firebase Authentication and Firestore instances
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Retrieve email from intent if passed
        val email = intent.getStringExtra("Email")
        email?.let {
            binding.emailEditText.setText(it)
            binding.emailEditText.isEnabled = false
        }

        // Populate spinner with service categories
        val services = listOf(
            "Select Service", "Plumber", "Teacher", "Driver", "Electrician",
            "Mechanic", "Gardener", "Painter", "Chef", "Maid", "Babysitter"
        )
        val serviceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, services)
        binding.serviceCategorySpinner.adapter = serviceAdapter

        // Populate AutoCompleteTextView with places in Bharuch
        val bharuchPlaces = listOf(
            "Zadeshwar", "Bholav", "Maktampur", "GNFC Township", "Ankleshwar",
            "Sherpura", "GIDC Estate", "Shaktinath", "Jambusar", "Dahej"
        )
        val cityAdapter =
            ArrayAdapter(this, android.R.layout.select_dialog_item, bharuchPlaces)
        binding.cityAutoComplete.setAdapter(cityAdapter)

        // Show/Hide additional fields based on role selection
        binding.roleGroup.setOnCheckedChangeListener { _, checkedId ->
            val isProvider = checkedId == R.id.providerRadio
            binding.serviceCategorySpinner.visibility = if (isProvider) View.VISIBLE else View.GONE
            binding.experienceEditText.visibility = if (isProvider) View.VISIBLE else View.GONE
            binding.cityAutoComplete.visibility = if (isProvider) View.VISIBLE else View.GONE
        }

        binding.alreadyAccountText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.SubmitBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.username.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val number = binding.mobileEdittext.text.toString().trim()
        val experience = binding.experienceEditText.text.toString().trim()
        val city = binding.cityAutoComplete.text.toString().trim()
        val selectedRole = when (binding.roleGroup.checkedRadioButtonId) {
            R.id.customerRadio -> "Customer"
            R.id.providerRadio -> "Provider"
            else -> null
        }

        // Validation checks
        if (name.isEmpty()) {
            binding.username.error = "Please enter your name!"
            return
        }
        if (password.isEmpty()) {
            binding.password.error = "Please enter a password!"
            return
        }
        if (number.isEmpty() || number.length != 10) {
            binding.mobileEdittext.error = "Please enter a valid 10-digit mobile number!"
            return
        }
        if (selectedRole == null) {
            Toast.makeText(this, "Please select a user type!", Toast.LENGTH_SHORT).show()
            return
        }

        var selectedService: String? = null
        if (selectedRole == "Provider") {
            selectedService = binding.serviceCategorySpinner.selectedItem.toString()
            if (selectedService == "Select Service") {
                Toast.makeText(this, "Please select a valid service!", Toast.LENGTH_SHORT).show()
                return
            }
            if (experience.isEmpty()) {
                binding.experienceEditText.error = "Experience is required for providers!"
                return
            }
            if (city.isEmpty()) {
                binding.cityAutoComplete.error = "City is required for providers!"
                return
            }
        }

        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "number" to number,
                        "role" to selectedRole
                    )

                    if (selectedRole == "Provider") {
                        selectedService?.let { user["service"] = it }
                        user["experience"] = experience
                        user["city"] = city
                    }

                    val userCollection = if (selectedRole == "Provider") "providers" else "customers"

                    db.collection(userCollection).document(auth.currentUser?.uid ?: "")
                        .set(user)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registration failed: ${e.message}, Try Again!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Registration failed and cleanup unsuccessful: ${deleteTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            binding.progressBar.visibility = View.GONE
                        }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
