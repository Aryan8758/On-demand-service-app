package com.example.foryou

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foryou.databinding.FragmentProviderBookingRecieveBinding
import com.google.android.material.snackbar.Snackbar
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
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

class ProviderBookingReciever : Fragment() {
    private var _binding: FragmentProviderBookingRecieveBinding? = null
    private val binding get() = _binding

    private lateinit var adapter: ProviderBookingReceiverAdapter
    private var bookingList = mutableListOf<ProviderBookingReceiverModel>()
    private var deletedBooking: ProviderBookingReceiverModel? = null // Undo ke liye

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderBookingRecieveBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.notificationRecycler?.layoutManager = LinearLayoutManager(requireContext())

        fetchBookings()
        setupSwipeGestures()
    }

    private fun fetchBookings() {
        val providerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")

        // Step 1: Show shimmer & hide everything else
        binding!!.shimmerViewContainer.visibility = View.VISIBLE
        binding!!.notificationRecycler.visibility = View.GONE
        binding!!.noBookingText.visibility = View.GONE

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()

                for (customerSnapshot in snapshot.children) {
                    for (bookingSnapshot in customerSnapshot.children) {
                        val booking =
                            bookingSnapshot.getValue(ProviderBookingReceiverModel::class.java)

                        if (booking != null) {
                            booking.bookingId = bookingSnapshot.key ?: ""

                            if (booking.ProviderId == providerId && booking.status == "Pending") {
                                bookingList.add(booking)
                            }
                        }
                    }
                }

                // **📌 Step 2: Sort bookings by booking timestamp (latest first)**
                bookingList.sortByDescending { it.timestamp }

                // Step 3: Hide shimmer layout
                binding?.shimmerViewContainer?.stopShimmer()
                binding?.shimmerViewContainer?.visibility = View.GONE

                // Step 4: Show either RecyclerView or No Booking Message
                if (bookingList.isEmpty()) {
                    binding!!.noBookingText.visibility = View.VISIBLE
                    binding!!.notificationRecycler.visibility = View.GONE
                } else {
                    binding!!.notificationRecycler.visibility = View.VISIBLE
                    binding!!.noBookingText.visibility = View.GONE
                }

                adapter =
                    ProviderBookingReceiverAdapter(bookingList, ::acceptBooking, ::rejectBooking)
                binding!!.notificationRecycler.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun acceptBooking(bookingId: String) {
        Log.d("Fragment", "Accepting booking: $bookingId")
        updateBookingStatus(bookingId, "Accepted")
    }

    private fun rejectBooking(bookingId: String) {
        Log.d("Fragment", "Rejecting booking: $bookingId")
        updateBookingStatus(bookingId, "Rejected")
    }

    private fun updateBookingStatus(bookingId: String, status: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (customerSnapshot in snapshot.children) {
                    for (bookingSnapshot in customerSnapshot.children) {
                        if (bookingSnapshot.key == bookingId) {
                            val userId = customerSnapshot.key  // 🔹 User ID extract karein
                            val providerId = bookingSnapshot.child("ProviderId").value.toString()
                            val selectedDate = bookingSnapshot.child("bookingDate").value.toString()
                            val selectedTimeSlot =
                                bookingSnapshot.child("timeSlot").value.toString()
                            Log.d("detail", "$providerId,$selectedDate,$selectedTimeSlot")

                            // 🔥 **1️⃣ Firebase me status update karo**
                            bookingSnapshot.ref.child("status").setValue(status)
                                .addOnSuccessListener {
                                    if (status == "Accepted") {
                                        Toast.makeText(
                                            requireContext(),
                                            "Booking $status",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    fetchBookings()
                                    userId?.let {
                                        fetchUserFCMToken(
                                            it,
                                            status
                                        )
                                    } // ✅ FCM notification bhejne ke liye

                                    // 🔥 **2️⃣ Sirf cancel/reject hone pe slot available karo**
                                    if (status == "Rejected") {
                                        val slotRef = FirebaseFirestore.getInstance()
                                            .collection("slots").document(providerId)
                                            .collection(selectedDate).document(selectedTimeSlot)

                                        val updates = mapOf(
                                            "status" to "available",  // ✅ Slot available ho jaye
                                            "bookedBy" to ""          // ✅ BookedBy field empty ho jaye
                                        )

                                        slotRef.update(updates)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Booking $status",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Failed to update slot!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to update status",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
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
                    val expiresIn =
                        googleCredentials.accessToken?.expirationTime?.time ?: 0L // ✅ Fixed

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

    private fun setupSwipeGestures() {
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val booking = bookingList[position]

                if (direction == ItemTouchHelper.RIGHT) {
                    acceptBooking(booking.bookingId)
                } else if (direction == ItemTouchHelper.LEFT) {
                    deletedBooking = booking
                    rejectBooking(booking.bookingId)

                    // Snackbar with Undo Option
                    binding?.let {
                        Snackbar.make(it.root, "Booking Rejected", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                undoRejectBooking(booking)
                            }
                            .show()
                    }
                }
                adapter.notifyItemRemoved(position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()

                if (dX > 0) { // Right Swipe (Accept)
                    paint.color = Color.GREEN
                    c.drawRect(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                    )
                } else if (dX < 0) { // Left Swipe (Reject)
                    paint.color = Color.RED
                    c.drawRect(
                        itemView.right.toFloat() + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                    )
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding?.notificationRecycler)
    }

    private fun undoRejectBooking(booking: ProviderBookingReceiverModel) {
        updateBookingStatus(booking.bookingId, "Pending")
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")
        val slotRef = FirebaseFirestore.getInstance()
            .collection("slots").document(booking.ProviderId)
            .collection(booking.bookingDate).document(booking.timeSlot)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (customerSnapshot in snapshot.children) {
                    for (bookingSnapshot in customerSnapshot.children) {
                        if (bookingSnapshot.key == booking.bookingId) {
                            // 🔹 Booking Status "Pending" karo
                            bookingSnapshot.ref.child("status").setValue("Pending")
                                .addOnSuccessListener {

                                    // 🔹 Slot Status "booked" karo
                                    val slotUpdates = mapOf(
                                        "status" to "booked",  // 🔄 Slot ko wapas booked karo
                                        "bookedBy" to booking.CustomerId  // 🔄 User ID wapas set karo
                                    )
                                    slotRef.update(slotUpdates)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Booking Restored",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Failed to restore slot!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to restore booking",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    //notification code
    private fun fetchUserFCMToken(userId: String, status: String) {
        FirebaseFirestore.getInstance().collection("customers").document(userId).get()
            .addOnSuccessListener { doc ->
                val fcmToken = doc.getString("fcmToken") ?: "other"
                if (!fcmToken.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendFCMNotification(requireContext(), fcmToken, status)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
