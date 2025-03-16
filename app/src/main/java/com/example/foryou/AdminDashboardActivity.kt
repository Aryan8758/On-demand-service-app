package com.example.foryou

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var valueTotalCustomers: TextView
    private lateinit var valueTotalProviders: TextView
    private lateinit var valuePendingRequests: TextView
    private lateinit var valueApprovedProviders: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        valueTotalCustomers = findViewById(R.id.value_total_customers)
        valueTotalProviders = findViewById(R.id.value_total_providers)
        valuePendingRequests = findViewById(R.id.value_pending_requests)
        valueApprovedProviders = findViewById(R.id.value_approved_providers)

        fetchCounts()
    }

    private fun fetchCounts() {
        // Fetch total customers
        db.collection("customers").get()
            .addOnSuccessListener { documents ->
                valueTotalCustomers.text = documents.size().toString()
            }

        // Fetch total providers
        db.collection("providers").get()
            .addOnSuccessListener { documents ->
                valueTotalProviders.text = documents.size().toString()
            }

        // Fetch pending provider requests
        db.collection("providers").whereEqualTo("status", "pending").get()
            .addOnSuccessListener { documents ->
                valuePendingRequests.text = documents.size().toString()
            }

        // Fetch approved providers
        db.collection("providers").whereEqualTo("status", "approved").get()
            .addOnSuccessListener { documents ->
                valueApprovedProviders.text = documents.size().toString()
            }
    }
}
