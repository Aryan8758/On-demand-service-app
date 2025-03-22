package com.example.foryou

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CustomerListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerListAdapter
    private var customerList = mutableListOf<CustomerListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        recyclerView = findViewById(R.id.customerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadCustomers()
    }

    private fun loadCustomers() {
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

                    val customer = CustomerListModel(id, name, email, phone, image)
                    customerList.add(customer)
                }
                adapter = CustomerListAdapter(customerList)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching customers", exception)
            }
    }
}
