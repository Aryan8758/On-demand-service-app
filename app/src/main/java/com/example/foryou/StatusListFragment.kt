package com.example.foryou

import StatusAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class StatusListFragment(private val filterStatus: String) : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private val statusModelClassList = mutableListOf<StatusModelClass>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status_list, container, false)

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

                for (customerSnapshot in snapshot.children) {
                    for (bookingSnapshot in customerSnapshot.children) {
                        val statusModelClass =
                            bookingSnapshot.getValue(StatusModelClass::class.java)
                        if (statusModelClass != null) {
                            statusModelClass.id = bookingSnapshot.key ?: ""
                            if (statusModelClass.status == filterStatus) {
                                statusModelClassList.add(statusModelClass)
                            }
                        }
                    }
                }
                statusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun markWorkComplete(statusModelClass: StatusModelClass) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")
            .child(statusModelClass.CustomerId)
            .child(statusModelClass.id)

        val updates = mapOf("status" to "Completed")

        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                val db = FirebaseFirestore.getInstance()
                val slotRef = db.collection("slots")
                    .document(statusModelClass.ProviderId)
                    .collection(statusModelClass.bookingDate)
                    .document(statusModelClass.timeSlot)

                val updates = mapOf("status" to "available", "bookedBy" to "")

                slotRef.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Work marked as complete!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            requireContext(),
                            "Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
