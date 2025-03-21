package com.example.foryou


import StatusAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ProviderStatus : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private val statusModelClassList = mutableListOf<StatusModelClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_provider_status, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        statusAdapter = StatusAdapter(statusModelClassList) { booking ->
            markWorkComplete(booking)
        }
        recyclerView.adapter = statusAdapter

        loadBookings()

        return view
    }

    private fun loadBookings() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statusModelClassList.clear()

                for (customerSnapshot in snapshot.children) { // Loop through customers
                    for (bookingSnapshot in customerSnapshot.children) { // Loop through bookings
                        val statusModelClass = bookingSnapshot.getValue(StatusModelClass::class.java)
                        if (statusModelClass != null) {
                            statusModelClass.id = bookingSnapshot.key ?: "" // Set booking ID
                            if (statusModelClass.status in listOf("Accepted", "Rejected", "Order Cancel","Completed")) {
                                statusModelClassList.add(statusModelClass)
                            }
                        }
                    }
                }
                statusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markWorkComplete(statusModelClass: StatusModelClass) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")
            .child(statusModelClass.CustomerId) // ✅ Customer ke andar booking store hai
            .child(statusModelClass.id)  // ✅ Specific booking update karni hai

        // Status ko "Completed" update karo
        val updates = mapOf("status" to "Completed")

        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                val db = FirebaseFirestore.getInstance()

                val slotRef = db.collection("slots")
                    .document(statusModelClass.ProviderId)  // ✅ Provider ID Document
                    .collection(statusModelClass.bookingDate) // ✅ Booking Date Collection
                    .document(statusModelClass.timeSlot) // ✅ Time Slot Document

                val updates = mapOf(
                    "status" to "available",  // ✅ Slot available ho jaye
                    "bookedBy" to ""
                )

                slotRef.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Work marked as complete!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(requireContext(), "Work marked as complete!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
