package com.example.foryou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foryou.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!  // Safe binding reference

    private lateinit var bookingList: MutableList<Booking_model>
    private lateinit var adapter: HistoryAdapter
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.itemAnimator = DefaultItemAnimator()

        bookingList = mutableListOf()
        adapter = HistoryAdapter(bookingList)
        binding.recyclerViewHistory.adapter = adapter

        fetchBookings()
        return binding.root
    }

    private fun fetchBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        databaseRef = FirebaseDatabase.getInstance().getReference("booking").child(userId)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking_model::class.java)
                    if (booking != null) {
                        booking.bookingId = bookingSnapshot.key ?: ""
                    }
                    booking?.let { bookingList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
