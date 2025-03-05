package com.example.foryou

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityBookingactivityBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

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
        val database = FirebaseDatabase.getInstance().getReference("Bookings")
        val ServiceName = intent.getStringExtra("ServiceName") ?: "Unknown"
        val ProviderId = intent.getStringExtra("ProviderId") ?: "Unknown"

        // 🔥 Firestore se user ka data fetch karo
        if (currentUser != null) {
            firestore.collection("customers").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val phone = document.getString("number") ?: ""
                        val address = document.getString("address") ?: ""

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

        // 🔙 Back Button
        binding.backButton.setOnClickListener {
            finish()
        }

        // 📌 Save Address Button
        binding.btnSaveAddress.setOnClickListener {
            val address = binding.etAddress.text.toString().trim()

            if (address.isEmpty()) {
                binding.etAddress.error = "Address cannot be empty"
                return@setOnClickListener
            }

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

        // 📅 Date Picker
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

        // ⏰ Time Slot Selection
        val timeSlots = listOf("10:00 AM", "12:00 PM", "2:00 PM", "4:00 PM", "6:00 PM")
        for (time in timeSlots) {
            val button = Button(this).apply {
                text = time
                textSize = 15f
                setPadding(10,10,10,10)
               // setBackgroundResource(R.drawable.rounded_bg)
                setOnClickListener {
                    selectedTimeSlot = time
                    Toast.makeText(this@BookingActivity, "Selected: $time", Toast.LENGTH_SHORT).show()
                }
            }
            binding.timeSlotGrid.addView(button)
        }

        // 💳 Payment Method Selection
        binding.rgPayment.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
                R.id.rbCash -> "Cash"
                R.id.rbOnline -> "Online Payment"
                else -> ""
            }
        }

        // ✅ Confirm Booking (🔥 Realtime Database me store karega)
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
            bookService(ServiceName,ProviderId,name,selectedDate,selectedTimeSlot,selectedPaymentMethod)
        }
    }
    fun bookService(serviceName:String,providerId:String,name:String,selectedDate:String,selectTimeslot:String,selectPaymentMethod:String){
        val currentUser=FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "userid not found ", Toast.LENGTH_SHORT).show()
            return
        }
        val databaseRef=FirebaseDatabase.getInstance().getReference("booking")
        val bookingId=databaseRef.push().key// generate Unique id and push it
        if (bookingId!=null){
            val bookingData= mapOf(
                "CustomerId" to currentUser.uid,
                "ProviderId" to providerId,
                "customerName" to name,
                "service" to serviceName,
                "bookingDate" to selectedDate,
                "timeSlot" to selectedTimeSlot,
                "paymentMethod" to selectedPaymentMethod,
                "status" to "Pending",
                "timestamp" to System.currentTimeMillis()

            )
            databaseRef.child(currentUser.uid).child(bookingId).setValue(bookingData).addOnSuccessListener {
                Toast.makeText(this, "Booking confirm", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e->
                Toast.makeText(this, "Booking fail:{$e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
