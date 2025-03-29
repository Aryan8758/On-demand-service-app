package com.example.foryou

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class CustomerListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backbutton: ImageView
    private lateinit var title: TextView
    private lateinit var adapter: CustomerListAdapter
    private lateinit var BookingReportAdapter: BookingReportAdapter
    private var customerList = mutableListOf<CustomerListModel>()
    private var providerList = mutableListOf<CustomerListModel>() // Provider list

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        recyclerView = findViewById(R.id.customerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
         backbutton  = findViewById(R.id.backButton)
         title  = findViewById(R.id.title)



        val listType = intent.getIntExtra("CustomerList or ProviderList or Booking Report", 0)

        when (listType) {
            1 -> loadCustomers()
            2 -> loadProviders()
            3->loadBookingReport()
        }
    }

    private fun loadCustomers() {
        title.text="Customers"
        val db = FirebaseFirestore.getInstance()
        db.collection("customers")
            .get()
            .addOnSuccessListener { documents ->
                customerList.clear()
                for (document in documents) {
                    val id = document.id
                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "No Email"
                    val phone = document.getString("number") ?: "No Phone"
                    val image = document.getString("profileImage")

                    val customer = CustomerListModel(id, name, email, phone, "Unknown",image)
                    customerList.add(customer)
                }
                adapter = CustomerListAdapter(customerList,"customers")
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching customers", exception)
            }
    }

    private fun loadProviders() {
        title.text="Providers"

        val db = FirebaseFirestore.getInstance()
        db.collection("providers") // Providers collection se data fetch kar raha hai
            .get()
            .addOnSuccessListener { documents ->
                providerList.clear()
                for (document in documents) {
                    val id = document.id
                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "No Email"
                    val phone = document.getString("number") ?: "No Phone"
                    val serviceName = document.getString("service") ?: "No service"
                    val image = document.getString("profileImage")

                    val provider = CustomerListModel(id, name, email, phone,serviceName,image)
                    providerList.add(provider)
                }
                adapter = CustomerListAdapter(providerList,"providers") // Same adapter use kar sakte ho
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching providers", exception)
            }
    }
    private fun loadBookingReport() {
        title.text="Bookings"

        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")

        databaseRef.get().addOnSuccessListener { snapshot ->
            val bookingList = mutableListOf<Booking_model>()

            for (customerSnapshot in snapshot.children) { // 🔹 Ye customerId level loop karega
                for (bookingSnapshot in customerSnapshot.children) { // 🔹 Har customer ki saari bookings loop karega
                    val bookingId = bookingSnapshot.key ?: "Unknown"
                    val customerId = bookingSnapshot.child("CustomerId").getValue(String::class.java) ?: "Unknown"
                    val providerId = bookingSnapshot.child("ProviderId").getValue(String::class.java) ?: "Unknown"
                    val customerName = bookingSnapshot.child("customerName").getValue(String::class.java) ?: "Unknown"
                    val service = bookingSnapshot.child("service").getValue(String::class.java) ?: "No Service"
                    val bookingDate = bookingSnapshot.child("bookingDate").getValue(String::class.java) ?: "No Date"
                    val timeSlot = bookingSnapshot.child("timeSlot").getValue(String::class.java) ?: "No Time"
                    val status = bookingSnapshot.child("status").getValue(String::class.java) ?: "Pending"

                    val booking = Booking_model(bookingId, customerId, providerId, customerName, service, bookingDate, timeSlot, status)
                    bookingList.add(booking)
                }
            }

            BookingReportAdapter = BookingReportAdapter(bookingList)
            recyclerView.adapter = BookingReportAdapter
        }.addOnFailureListener { exception ->
            Log.e("RealtimeDBError", "Error fetching bookings", exception)
        }
    }

}
