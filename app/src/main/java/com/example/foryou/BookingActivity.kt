package com.example.foryou

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityBookingactivityBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import com.google.firebase.auth.FirebaseAuth

class BookingActivity : AppCompatActivity() {
    private val binding: ActivityBookingactivityBinding by lazy {
        ActivityBookingactivityBinding.inflate(layoutInflater)
    }

    private var selectedDate: String = ""
    private var selectedTimeSlot: String = ""
    private var selectedPaymentMethod: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()

        // Firestore se user ka data fetch karo
        if (currentUser != null) {
            firestore.collection("customers").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val phone = document.getString("number") ?: ""
                        val address =document.getString("address") ?: ""

                        binding.etName.setText(name)
                        binding.etPhone.setText(phone)
                        binding.etAddress.setText(address)
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Back Button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Save Address Button
            binding.btnSaveAddress.setOnClickListener {
                val address = binding.etAddress.text.toString().trim()

                if (address.isEmpty()) {
                    binding.etAddress.error = "Address cannot be empty"
                    return@setOnClickListener
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                val firestore = FirebaseFirestore.getInstance()

                if (currentUser != null) {
                    val userRef = firestore.collection("customers").document(currentUser.uid)

                    userRef.update("address", address)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Address Saved Successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save address: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                }
            }

        // Date Picker
        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.tvSelectedDate.text = selectedDate
            }, year, month, day)

            datePicker.show()
        }

        // Time Slot Selection
        val timeSlots = listOf("10:00 AM", "12:00 PM", "2:00 PM", "4:00 PM", "6:00 PM")
        for (time in timeSlots) {
            val button = Button(this).apply {
                text = time
                textSize = 14f
                setPadding(10, 10, 10, 10)
                setBackgroundResource(R.drawable.rounded_bg)
                setOnClickListener {
                    selectedTimeSlot = time
                    Toast.makeText(this@BookingActivity, "Selected: $time", Toast.LENGTH_SHORT).show()
                }
            }
            binding.timeSlotGrid.addView(button)
        }

        // Payment Method Selection
        binding.rgPayment.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
                R.id.rbCash -> "Cash"
                R.id.rbOnline -> "Online Payment"
                else -> ""
            }
        }

        // Confirm Booking
        binding.btnConfirmBooking.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            if (name.isEmpty()) {
                binding.etName.error = "Name is required"
                return@setOnClickListener
            }
            if (phone.length != 10) {
                binding.etPhone.error = "Enter a valid 10-digit phone number"
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                binding.etAddress.error = "Address is required"
                return@setOnClickListener
            }
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a booking date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedTimeSlot.isEmpty()) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_LONG).show()
        }
    }
}