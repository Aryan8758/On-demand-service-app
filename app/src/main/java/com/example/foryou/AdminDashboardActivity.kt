package com.example.foryou

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPref
    private lateinit var binding: ActivityAdminDashboardBinding
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = SharedPref(this)

        fetchCounts() // Fetch data from Firebase

        // Logout Button Click Listener
        binding.txtPendingRequests.setOnClickListener {
            startActivity(Intent(this, PendingRequestsActivity::class.java))
        }
        //CustomerList
        binding.totalCustomerLayout.setOnClickListener {
            val intent=Intent(this,CustomerListActivity::class.java)
            intent.putExtra("CustomerList or ProviderList or Booking Report",1)

            startActivity(intent)
        //    startActivity(Intent(this,CustomerListActivity::class.java))
        }
        //ProviderList
        binding.totalProviderLayout.setOnClickListener {
            val intent=Intent(this,CustomerListActivity::class.java)
            intent.putExtra("CustomerList or ProviderList or Booking Report",2)
            startActivity(intent)        }
        binding.btnLogout.setOnClickListener {
            showLogoutDialog() // Show confirmation dialog before logout
        }
        binding.BookingReportLayout.setOnClickListener {
            val intent=Intent(this,CustomerListActivity::class.java)
            intent.putExtra("CustomerList or ProviderList or Booking Report",3)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchCounts() {
        // ✅ Customers count real-time update
        db.collection("customers").addSnapshotListener { documents, e ->
            if (e == null && documents != null) {
                binding.valueTotalCustomers.text = documents.size().toString()
                println("✅ Customers Count Updated: ${documents.size()}")
            }
        }

        // ✅ Providers count real-time update
        db.collection("providers").addSnapshotListener { documents, e ->
            if (e == null && documents != null) {
                binding.valueTotalProviders.text = documents.size().toString()
                println("✅ Providers Count Updated: ${documents.size()}")
            }
        }

        // ✅ Pending requests real-time update
        db.collection("providers").whereEqualTo("status", "pending")
            .addSnapshotListener { documents, e ->
                if (e == null && documents != null) {
                    binding.valuePendingRequests.text = documents.size().toString()
                    println("✅ Pending Requests Updated: ${documents.size()}")
                }
            }

        // ✅ Approved providers real-time update
//        db.collection("providers").whereEqualTo("status", "approved")
//            .addSnapshotListener { documents, e ->
//                if (e == null && documents != null) {
//                    binding.valueApprovedProviders.text = documents.size().toString()
//                    println("✅ Approved Providers Updated: ${documents.size()}")
//                }
//            }
        val bookingRef = FirebaseDatabase.getInstance().getReference("booking")

        bookingRef.get().addOnSuccessListener { snapshot ->
            var totalBookings = 0

            if (snapshot.exists()) {
                for (customer in snapshot.children) {
                    totalBookings += customer.childrenCount.toInt() // ✅ Har customer ki bookings add karo
                }
            }

            Log.d("Firebase", "Total Bookings: $totalBookings")
            binding.valueApprovedProviders.text=totalBookings.toString()
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error fetching bookings: ${e.message}")
        }
    }


    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                sharedPreferences.logoutUser()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.setOnShowListener {
            // Set button colors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(Color.RED) // ✅ Correct usage
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)// No button
        }

        dialog.show()
    }

    private var backPressedTime: Long = 0

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed() // Exit the app
            return
        } else {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis() // Reset timer
    }


}
