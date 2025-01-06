package com.example.foryou

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
        val services = listOf("Plumber", "Teacher", "Driver", "Electrician", "Mechanic",
            "Gardener", "Painter", "Chef", "Maid", "Babysitter")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, services)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.serviceCategorySpinner.adapter = adapter

        // Show/Hide spinner based on role selection
        binding.roleGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.providerRadio) {
                binding.serviceCategorySpinner.visibility = View.VISIBLE
            } else {
                binding.serviceCategorySpinner.visibility = View.GONE
            }
        }

        // Submit button click listener
        binding.submitButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val selectedRole = when (binding.roleGroup.checkedRadioButtonId) {
                R.id.customerRadio -> "Customer"
                R.id.providerRadio -> "Provider"
                else -> null
            }

            if (name.isEmpty() || password.isEmpty() || selectedRole == null) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedService = if (selectedRole == "Provider") {
                binding.serviceCategorySpinner.selectedItem.toString()
            } else null

            // Handle registration logic here
            Toast.makeText(this, "Registered as $selectedRole ($selectedService)", Toast.LENGTH_SHORT).show()
        }
    }
}
