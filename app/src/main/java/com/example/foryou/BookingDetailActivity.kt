package com.example.foryou

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityBookingDetailBinding
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private var bookingId: String? = null
    private var customerId: String? = null
    //private var image: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Get Booking ID from Intent
         customerId = intent.getStringExtra("CUSTOMER_ID")
         bookingId = intent.getStringExtra("BOOKING_ID")
//image = intent.getStringExtra("image")



        // Firebase Initialization
        database = FirebaseDatabase.getInstance().reference.child("booking")
        firestore = FirebaseFirestore.getInstance()

        if (bookingId != null && customerId!=null) {
            loadBookingDetails(customerId!!,bookingId!!)
        } else {
            Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show()
        }

        // Accept Button Click
        binding.acceptBtn.setOnClickListener {
            updateBookingStatus("Accepted")
        }

        // Reject Button Click
        binding.rejectBtn.setOnClickListener {
            updateBookingStatus("Rejected")
        }
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    // Load Booking Details from Realtime Database
    private fun loadBookingDetails(customerId: String,bookingId: String) {
        database.child(customerId).child(bookingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val bookingDate = snapshot.child("bookingDate").value.toString()
                    val bookingTime = snapshot.child("timeSlot").value.toString()
                    val paymentType = snapshot.child("paymentMethod").value.toString()

                // Set Booking Data
                    binding.bookingDateTime.text = "Booking Date: $bookingDate | Time: $bookingTime"
                    binding.paymentType.text = "Payment Type: $paymentType"

                    // Load Customer Details
                    loadCustomerDetails(customerId)
                } else {
                    Toast.makeText(this@BookingDetailActivity, "Booking not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookingDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Load Customer Details from Firestore
    private fun loadCustomerDetails(customerId: String) {
        firestore.collection("customers").document(customerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val phone = document.getString("number")
                    val address = document.getString("address")
                    val image = document.getString("profileImage")

                    // Set Customer Data
                    binding.customerName.text = name
                    binding.customerEmail.text = email
                    binding.customerPhone.text = phone
                    binding.customerAddress.text = "Address: $address"
                    binding.customerImage.setImageBitmap(image?.let { decodeBase64ToBitmap(it) })
                } else {
                    Toast.makeText(this@BookingDetailActivity, "Customer not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@BookingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Update Booking Status in Realtime Database
    private fun updateBookingStatus(status: String) {
        if (bookingId != null) {
            database.child(customerId!!).child(bookingId!!).child("status").setValue(status)
                .addOnSuccessListener {
                    Toast.makeText(this, "Booking $status successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
