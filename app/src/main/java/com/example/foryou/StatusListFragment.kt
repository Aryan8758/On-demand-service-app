package com.example.foryou

import StatusAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class StatusListFragment(private val filterStatus: String) : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noRequestText: TextView
    private lateinit var noRequestText2: TextView
    private lateinit var Notiani: LottieAnimationView
    private lateinit var statusAdapter: StatusAdapter
    private val statusModelClassList = mutableListOf<StatusModelClass>()
    private val auth=FirebaseAuth.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        noRequestText = view.findViewById(R.id.noRequestText)
        noRequestText2 = view.findViewById(R.id.noRequestText2)
        Notiani = view.findViewById(R.id.notiani)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        statusAdapter = StatusAdapter(statusModelClassList) { booking ->
            markWorkComplete(booking)
        }
        recyclerView.adapter = statusAdapter

        auth.currentUser?.let { loadBookings(it.uid) }

        return view
    }

    private fun loadBookings(providerId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statusModelClassList.clear()

                for (customerSnapshot in snapshot.children) { // Loop through customers
                    for (bookingSnapshot in customerSnapshot.children) { // Loop through bookings
                        val statusModelClass = bookingSnapshot.getValue(StatusModelClass::class.java)
                        if (statusModelClass != null) {
                            statusModelClass.id = bookingSnapshot.key ?: ""

                            // ✅ Sirf wahi bookings add karo jisme providerId match kare
                            if (statusModelClass.ProviderId == providerId && statusModelClass.status == filterStatus) {
                                statusModelClassList.add(statusModelClass)
                            }
                        }
                    }
                }
                if (statusModelClassList.isEmpty()){
                    recyclerView.visibility=View.GONE
                    noRequestText.visibility=View.VISIBLE
                    noRequestText2.visibility=View.VISIBLE
                    Notiani.visibility=View.VISIBLE
                }
                else{
                    recyclerView.visibility=View.VISIBLE
                    noRequestText.visibility=View.GONE
                    noRequestText2.visibility=View.GONE
                    Notiani.visibility=View.GONE
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
