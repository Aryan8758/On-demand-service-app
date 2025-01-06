package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Retrieve email from intent
        val email = intent.getStringExtra("Email")
        if (!email.isNullOrEmpty()) {
            binding.emailEditText.setText(email)
            binding.emailEditText.isEnabled = false
        }

        // Populate spinner with service categories
        val services = listOf("Select Service","Plumber", "Teacher", "Driver", "Electrician", "Mechanic", "Gardener", "Painter", "Chef", "Maid", "Babysitter")
        val serviceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, services)
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.serviceCategorySpinner.adapter = serviceAdapter

        // Populate AutoCompleteTextView with places in Bharuch
        val bharuchPlaces = listOf(
            "Zadeshwar", "Bholav", "Maktampur", "GNFC Township", "Ankleshwar",
            "Sherpura", "GIDC Estate", "Shaktinath", "Jambusar", "Dahej"
        )
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bharuchPlaces)
        binding.cityAutoComplete.setAdapter(cityAdapter)

        // Show/Hide additional fields based on role selection
        binding.roleGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.providerRadio) {
                // Show fields for providers
                binding.serviceCategorySpinner.visibility = View.VISIBLE
                binding.experienceEditText.visibility = View.VISIBLE
                binding.cityAutoComplete.visibility = View.VISIBLE
            } else {
                // Hide fields for customers
                binding.serviceCategorySpinner.visibility = View.GONE
                binding.experienceEditText.visibility = View.GONE
                binding.cityAutoComplete.visibility = View.GONE
            }
        }
        // Submit button click listener

        binding.SubmitBtn.setOnClickListener {
    val name = binding.username.text.toString().trim()
    val password = binding.password.text.toString().trim()
    val experience = binding.experienceEditText.text.toString().trim()
    val city = binding.cityAutoComplete.text.toString().trim()
    val selectedRole = when (binding.roleGroup.checkedRadioButtonId) {
        R.id.customerRadio -> "Customer"
        R.id.providerRadio -> "Provider"
        else -> null
    }

    // Validation
    if (name.isEmpty()) {
        binding.username.error = "Name is required!"
        return@setOnClickListener
    }
    if (password.isEmpty()) {
        binding.password.error = "Password is required!"
        return@setOnClickListener
    }
    if (selectedRole == "Provider") {
        if (experience.isEmpty()) {
            binding.experienceEditText.error = "Experience is required for providers!"
            return@setOnClickListener
        }
        if (city.isEmpty()) {
            binding.cityAutoComplete.error = "City is required for providers!"
            return@setOnClickListener
        }
    }

    val selectedService = if (selectedRole == "Provider") {
        binding.serviceCategorySpinner.selectedItem.toString()
    } else null

    // Handle registration logic
    Toast.makeText(
        this,
        "Registered as $selectedRole${if (selectedRole == "Provider") " in $city with $experience years experience for $selectedService" else ""}",
        Toast.LENGTH_SHORT
    ).show()
    startActivity(Intent(this,LoginActivity::class.java))
    finish()
}

    }
}
