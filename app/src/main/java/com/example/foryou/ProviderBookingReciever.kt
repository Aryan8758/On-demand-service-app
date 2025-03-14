package com.example.foryou

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foryou.databinding.FragmentProviderBookingRecieveBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProviderBookingReciever : Fragment() {
    private var _binding: FragmentProviderBookingRecieveBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProviderBookingReceiverAdapter
    private var bookingList = mutableListOf<ProviderBookingReceiverModel>()
    private var deletedBooking: ProviderBookingReceiverModel? = null // Undo ke liye

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderBookingRecieveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notificationRecycler.layoutManager = LinearLayoutManager(requireContext())

        fetchBookings()
        setupSwipeGestures()
    }

    private fun fetchBookings() {
        val providerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("booking")

        // Step 1: Show shimmer & hide everything else
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.notificationRecycler.visibility = View.GONE
        binding.noBookingText.visibility = View.GONE

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()

                for (customerSnapshot in snapshot.children) {
                    for (bookingSnapshot in customerSnapshot.children) {
                        val booking = bookingSnapshot.getValue(ProviderBookingReceiverModel::class.java)

                        if (booking != null) {
                            booking.bookingId = bookingSnapshot.key ?: ""

                            if (booking.ProviderId == providerId && booking.status == "Pending") {
                                bookingList.add(booking)
                            }
                        }
                    }
                }

                // Step 2: Hide shimmer layout
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE

                // Step 3: Show either RecyclerView or No Booking Message
                if (bookingList.isEmpty()) {
                    binding.noBookingText.visibility = View.VISIBLE
                    binding.notificationRecycler.visibility = View.GONE
                } else {
                    binding.notificationRecycler.visibility = View.VISIBLE
                    binding.noBookingText.visibility = View.GONE
                }

                adapter = ProviderBookingReceiverAdapter(bookingList, ::acceptBooking, ::rejectBooking)
                binding.notificationRecycler.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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
                            bookingSnapshot.ref.child("status").setValue(status)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Booking $status", Toast.LENGTH_SHORT).show()
                                    fetchBookings()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show()
                                }
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSwipeGestures() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

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
                    Snackbar.make(binding.root, "Booking Rejected", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            undoRejectBooking(booking)
                        }
                        .show()
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
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.notificationRecycler)
    }

    private fun undoRejectBooking(booking: ProviderBookingReceiverModel) {
        updateBookingStatus(booking.bookingId, "Pending")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
