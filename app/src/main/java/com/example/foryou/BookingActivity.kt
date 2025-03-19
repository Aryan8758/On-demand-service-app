package com.example.foryou

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import com.google.auth.oauth2.GoogleCredentials
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityBookingactivityBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.SetOptions
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
        binding.timeSlotGrid.visibility = View.GONE // 🚀 Pehle hide rakho

        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                binding.tvSelectedDate.text = selectedDate

                if (selectedDate.isNotEmpty()) {
                    binding.timeSlotTv.visibility=View.VISIBLE
                    binding.timeSlotGrid.visibility = View.VISIBLE // 🔥 Jab date select ho, tab slots dikhai do
                    fetchAvailableSlots(ProviderId, selectedDate) // ⏰ Slots fetch karo
                }
            }, year, month, day)

            // 🔴 **Disable past dates**
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }


//        // ⏰ Time Slot Selection
//        val timeSlots = listOf("10:00 AM", "12:00 PM", "2:00 PM", "4:00 PM", "6:00 PM")
//        for (time in timeSlots) {
//            val button = Button(this).apply {
//                text = time
//                textSize = 15f
//                setPadding(10,10,10,10)
//               // setBackgroundResource(R.drawable.rounded_bg)
//                setOnClickListener {
//                    selectedTimeSlot = time
//                    Toast.makeText(this@BookingActivity, "Selected: $time", Toast.LENGTH_SHORT).show()
//                }
//            }
//            binding.timeSlotGrid.addView(button)
//        }

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


    fun bookService(serviceName: String, providerId: String, name: String, selectedDate: String, selectTimeslot: String, selectPaymentMethod: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")
        val bookingId = databaseRef.push().key // Generate Unique ID and push it

        if (bookingId != null) {
            val bookingData = mapOf(
                "CustomerId" to currentUser.uid,
                "ProviderId" to providerId,
                "customerName" to name,
                "service" to serviceName,
                "bookingDate" to selectedDate,
                "timeSlot" to selectTimeslot,
                "paymentMethod" to selectPaymentMethod,
                "status" to "Pending",
                "timestamp" to System.currentTimeMillis()
            )

            // ✅ 1️⃣ Realtime Database me booking save karo
            databaseRef.child(currentUser.uid).child(bookingId).setValue(bookingData)
                .addOnSuccessListener {
                    // ✅ 2️⃣ Firestore me Slot Status Update karo
                    updateFirestoreSlot(providerId, selectedDate, selectTimeslot, currentUser.uid)

                    // 🔥 Notification bhejo provider ko
                    sendNotificationToProvider(name, providerId)
                    showSnackbar("Booking confirmed")
                //    Toast.makeText(this, "Booking confirmed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    showSnackbar("Booking failed")
                   // Toast.makeText(this, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    fun fetchAvailableSlots(providerId: String, selectedDate: String) {
        val defaultTimeSlots = listOf("10:00 AM", "11:00 AM", "12:00 PM", "2:00 PM", "4:00 PM", "6:00 PM")
        val db = FirebaseFirestore.getInstance()
        val slotRef = db.collection("slots").document(providerId).collection(selectedDate)

        slotRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            binding.timeSlotGrid.removeAllViews() // 🗑 Purane slots clear karo

            val bookedSlots = mutableSetOf<String>() // ❌ Booked slots track karne ke liye
            snapshots?.forEach { document ->
                if (document.getString("status") == "booked") {
                    bookedSlots.add(document.id)
                }
            }

            var selectedButton: Button? = null // 🔹 Track karo pehle select kiya hua button

            for (time in defaultTimeSlots) {
                val button = Button(this@BookingActivity).apply {
                    text = time
                    textSize = 15f
                    setPadding(10, 10, 10, 10)

                    if (bookedSlots.contains(time)) {
                        setBackgroundColor(Color.RED) // ❌ Booked slot = RED
                        setOnClickListener {
                            Toast.makeText(this@BookingActivity, "Already booked!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        setBackgroundColor(Color.GREEN) // ✅ Available slot = GREEN
                        setOnClickListener {
                            selectedTimeSlot = time

                            // 🔹 Pehle wale selected button ko wapas GREEN karo
                            selectedButton?.setBackgroundColor(Color.GREEN)

                            // 🔹 Naya selected button BLUE karo
                            setBackgroundColor(Color.BLUE)
                            selectedButton = this // 🔥 Update selected button reference

                            Toast.makeText(this@BookingActivity, "Selected: $time", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                binding.timeSlotGrid.addView(button)
            }
        }
    }

    private fun updateFirestoreSlot(providerId: String, selectedDate: String, selectedTimeSlot: String, userId: String) {
        val db = FirebaseFirestore.getInstance()
        val slotRef = db.collection("slots")
            .document(providerId)
            .collection(selectedDate)
            .document(selectedTimeSlot)

        // Firestore slots path: slots/{ProviderId}/{selectedDate}/{selectedTimeSlot}
        slotRef.set(
            mapOf(
                "bookedBy" to userId,
                "status" to "booked"
            ), SetOptions.merge()
        ).addOnSuccessListener {
            Log.d("Firestore", "Slot booked successfully")
            fetchAvailableSlots(providerId, selectedDate) // 🔄 UI update
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error updating slot: ${e.message}")
        }
    }
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
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

    fun sendNotificationToProvider(serviceName: String, providerId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("providers").document(providerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fcmToken = document.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            sendFCMNotification(this@BookingActivity, fcmToken, serviceName)
                        }
                    } else {
                        Log.e("FCM", "Provider FCM Token is Empty")
                    }
                } else {
                    Log.e("FCM", "Provider Not Found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to fetch provider data: ${e.message}")
            }
    }

    suspend fun sendFCMNotification(context: Context, providerToken: String, serviceName: String) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/foryou-fa1d3/messages:send"

        val accessToken = TokenManager.getAccessToken(context) // Run on background thread
        if (accessToken == null) {
            Log.e("FCM", "Failed to generate access token")
            return
        }

        val jsonObject = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", providerToken)  // The provider's FCM token
                put("notification", JSONObject().apply {
                    put("title", "New Booking Request")
                    put("body", "You have a new booking for $serviceName")
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
