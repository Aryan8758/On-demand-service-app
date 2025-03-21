package com.example.foryou

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foryou.databinding.FragmentHistoryTabBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryTabFragment(private val status: String) : Fragment() {

    private var _binding: FragmentHistoryTabBinding? = null
    private val binding get() = _binding
    private lateinit var adapter: HistoryAdapter
    private var bookingList = mutableListOf<Booking_model>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryTabBinding.inflate(inflater, container, false)

        binding!!.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding!!.recyclerView.itemAnimator = DefaultItemAnimator()
        adapter = HistoryAdapter(bookingList)
        binding!!.recyclerView.adapter = adapter

        fetchBookings()

        return binding!!.root
    }

    private fun fetchBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking").child(userId)
        databaseRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking_model::class.java)
                    if (booking != null) {
                        booking.bookingId = bookingSnapshot.key ?: ""
                        if (booking.status == status)  // ✅ Sirf selected status wali bookings add karo
                            bookingList.add(booking)
                    }
                }
                if (bookingList.isEmpty()) {
                    binding?.recyclerView?.visibility = View.GONE  // ❌ Hide list
                    binding?.noRequestText?.visibility = View.VISIBLE  // ✅ Show message
                    binding?.noRequestText?.text = "No $status Requests Found"
                } else {
                    binding?.recyclerView?.visibility = View.VISIBLE  // ✅ Show list
                    binding?.noRequestText?.visibility = View.GONE  // ❌ Hide message
                }

                // ✅ Reverse list to show latest bookings first
                bookingList.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
