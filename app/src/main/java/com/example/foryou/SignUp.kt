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
            if (checkedId == R.id.providerRadio) {
                binding.serviceCategorySpinner.visibility = View.VISIBLE
                binding.experienceEditText.visibility = View.VISIBLE
                binding.cityAutoComplete.visibility = View.VISIBLE
            } else {
                binding.serviceCategorySpinner.visibility = View.GONE
                binding.experienceEditText.visibility = View.GONE
                binding.cityAutoComplete.visibility = View.GONE
            }
        }

        binding.SubmitBtn.setOnClickListener {
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
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.password.error = "Please enter a password!"
                return@setOnClickListener
            }
            if (number.isEmpty()) {
                binding.mobileEdittext.error = "Please enter a Mobile number!"
                return@setOnClickListener
            }
            if (number.length != 10) {
                binding.mobileEdittext.error = "Please enter a 10-digit Mobile number!"
                return@setOnClickListener
            }
            if (selectedRole == null) {
                Toast.makeText(this, "Please select a user type!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var selectedService: String? = null
            if (selectedRole == "Provider") {
                selectedService = binding.serviceCategorySpinner.selectedItem.toString()
                if (selectedService == "Select Service") {
                    Toast.makeText(this, "Please select a valid service!", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                if (experience.isEmpty()) {
                    binding.experienceEditText.error = "Experience is required for providers!"
                    return@setOnClickListener
                }
                if (city.isEmpty()) {
                    binding.cityAutoComplete.error = "City is required for providers!"
                    return@setOnClickListener
                }
            }

            //navigate to login page
            binding.alreadyAccountText.setOnClickListener{
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }

            // Show progress bar
            binding.progressBar.visibility = View.VISIBLE

            // Create user in Firebase Authentication

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Prepare user data for Firestore
                        val user = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "number" to number,
                            "role" to selectedRole
                        )

                        // Add provider-specific data
                        if (selectedRole == "Provider") {
                            selectedService?.let { user["service"] = it }
                            experience.takeIf { it.isNotEmpty() }?.let { user["experience"] = it }
                            city.takeIf { it.isNotEmpty() }?.let { user["city"] = it }
                        }

                        // Save data based on role
                        val userCollection = if (selectedRole == "Provider") {
                            "providers"
                        } else {
                            "customers"
                        }

                        db.collection(userCollection).document(auth.currentUser?.uid ?: "")
                            .set(user)
                            .addOnSuccessListener {
                                // Hide progress bar
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Hide progress bar
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    } else {
                        // Hide progress bar
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
}
