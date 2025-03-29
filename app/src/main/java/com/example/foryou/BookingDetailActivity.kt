package com.example.foryou

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityBookingDetailBinding
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import androidx.activity.OnBackPressedCallback

class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private var bookingId: String? = null
    private var customerId: String? = null
    private var providerId: String? = null
    private var bookingDate: String? = null
    private var timeSlot: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
            binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        customerId = intent.getStringExtra("CUSTOMER_ID")
        bookingId = intent.getStringExtra("BOOKING_ID")
        val statusPageRequestIntent = intent.getIntExtra("statusFragment", 0)
        if (statusPageRequestIntent == 1) {
            binding.acceptBtn.visibility = View.GONE
            binding.rejectBtn.visibility = View.GONE
        }

        database = FirebaseDatabase.getInstance().reference.child("booking")
        firestore = FirebaseFirestore.getInstance()

        if (bookingId != null && customerId != null) {
            binding.shimmerLayout.startShimmer() // Shimmer Start
            loadBookingDetails(customerId!!, bookingId!!)
        } else {
            Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show()
        }

        binding.acceptBtn.setOnClickListener {
            updateBookingStatus("Accepted")
        }

        binding.rejectBtn.setOnClickListener {
            updateBookingStatus("Rejected")
        }
    }

    private fun loadBookingDetails(customerId: String, bookingId: String) {
        database.child(customerId).child(bookingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    bookingDate = snapshot.child("bookingDate").value.toString()
                    timeSlot = snapshot.child("timeSlot").value.toString()
                    val paymentType = snapshot.child("paymentMethod").value.toString()
                    val BookingStatus = snapshot.child("status").value.toString()
                    providerId = snapshot.child("ProviderId").value.toString()
                    binding.bookingDateTime.text = "Booking Date: $bookingDate | Time: $timeSlot"
                    binding.paymentType.text = "Payment Type: $paymentType"
                    binding.BookingStatus.text = "Booking Status: $BookingStatus"
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

                    binding.customerName.text = name
                    binding.customerEmail.text = email
                    binding.customerPhone.text = phone
                    binding.customerAddress.text = "Address: $address"
                   // binding.customerImage.setImageBitmap(image?.let { decodeBase64ToBitmap(it) })
                    if(image!=null){
                        val img = decodeBase64ToBitmap(image)
                        binding.customerImage.setImageBitmap(img)
                    }else{
                        binding.customerImage.setImageResource(R.drawable.tioger)
                    }
                    binding.customerPhone.setOnClickListener {
                        phone?.let { number ->
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$number")
                            startActivity(intent)
                        }
                    }
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.headerContainer.visibility=View.VISIBLE
                    binding.customerName.visibility=View.VISIBLE
                    binding.detailsContainer.visibility=View.VISIBLE
                    binding.buttonContainer.visibility=View.VISIBLE

                } else {
                    Toast.makeText(this@BookingDetailActivity, "Customer not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@BookingDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    private fun updateBookingStatus(status: String) {
        if (bookingId != null && customerId != null) {
            database.child(customerId!!).child(bookingId!!).child("status").setValue(status)
                .addOnSuccessListener {
                    Toast.makeText(this, "Booking $status successfully", Toast.LENGTH_SHORT).show()

                    // 🛠️ **Agar Rejected ho toh slot available karo**
                    if (status == "Rejected" && providerId != null && bookingDate != null && timeSlot != null) {
                        updateSlotStatus(providerId!!, bookingDate!!, timeSlot!!)
                    }

                    // 🛑 **Customer ko Notification Send karo**
                    fetchUserFCMToken(customerId!!, status)

                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateSlotStatus(providerId: String, date: String, timeSlot: String) {
        val slotRef = firestore.collection("slots").document(providerId)
            .collection(date).document(timeSlot)

        val updates = mapOf(
            "status" to "available",
            "bookedBy" to ""
        )

        slotRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Slot marked as available", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update slot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    object TokenManager {
        private var cachedToken: String? = null
        private var tokenExpiryTime: Long = 0L

        suspend fun getAccessToken(context: Context): String? {
            val currentTime = System.currentTimeMillis()

            // ✅ Pehle se token hai aur expire nahi hua, to wahi return karo
            if (cachedToken != null && currentTime < tokenExpiryTime) {
                Log.d("FCM", "Using Cached Token: $cachedToken")
                return cachedToken
            }

            return withContext(Dispatchers.IO) {
                try {
                    val jsonStream = context.assets.open("service-account.json")
                    val googleCredentials = GoogleCredentials.fromStream(jsonStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                    googleCredentials.refreshIfExpired()
                    val newToken = googleCredentials.accessToken?.tokenValue
                    val expiresIn = googleCredentials.accessToken?.expirationTime?.time ?: 0L // ✅ Fixed

                    if (newToken != null) {
                        cachedToken = newToken
                        tokenExpiryTime = expiresIn
                    }

                    Log.d("FCM", "Generated New Token: $newToken")
                    newToken
                } catch (e: IOException) {
                    Log.e("FCM", "Error getting access token: ${e.message}")
                    null
                }
            }
        }
    }


    private fun fetchUserFCMToken(userId: String, status: String) {
        firestore.collection("customers").document(userId).get()
            .addOnSuccessListener { doc ->
                val fcmToken = doc.getString("fcmToken") ?: ""
                if (fcmToken.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendFCMNotification(this@BookingDetailActivity, fcmToken, status)
                    }
                } else {
                    Log.e("FCM", "User FCM Token is Empty")
                }
            }
    }
    suspend fun sendFCMNotification(context: Context, userToken: String, status: String) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/foryou-fa1d3/messages:send"

        val accessToken = TokenManager.getAccessToken(context)  // Token fetch karein
        if (accessToken == null) {
            Log.e("FCM", "Failed to generate access token")
            return
        }

        val messageBody = if (status == "Accepted") {
            "Your booking request has been accepted!"
        } else {
            "Your booking request has been rejected."
        }

        val jsonObject = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", userToken)
                put("notification", JSONObject().apply {
                    put("title", "Booking Update")
                    put("body", messageBody)
                })
            })
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(fcmUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = OkHttpClient().newCall(request).execute()
                Log.d("FCM", "Notification Response: ${response.body?.string()}")
            } catch (e: IOException) {
                Log.e("FCM", "Error sending notification: ${e.message}")
            }
        }
    }

}
