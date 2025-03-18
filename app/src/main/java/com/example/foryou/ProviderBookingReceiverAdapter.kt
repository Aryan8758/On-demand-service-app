package com.example.foryou

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ProviderBookingReceiverAdapter(
    private val bookings: List<ProviderBookingReceiverModel>,
    private val onAcceptClick: (String) -> Unit,
    private val onRejectClick: (String) -> Unit
) : RecyclerView.Adapter<ProviderBookingReceiverAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val txtCustomerName: TextView = view.findViewById(R.id.tvUserName)
        val txtBookingDate: TextView = view.findViewById(R.id.txtBookingDate)
        val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_design, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        holder.txtServiceName.text = booking.service
        holder.txtCustomerName.text = "Customer: ${booking.customerName}"
        holder.txtBookingDate.text = "Date: ${booking.bookingDate} | Time: ${booking.timeSlot}"
        val Customerid =booking.CustomerId
        val db=FirebaseFirestore.getInstance()
        db.collection("customers").document(Customerid).addSnapshotListener { documentSnapshot, error ->
            if (documentSnapshot != null) {
                val image=documentSnapshot.getString("profileImage")
                holder.imgProfile.setImageBitmap(image?.let { decodeBase64ToBitmap(it) })
            }

        }

        holder.btnAccept.setOnClickListener {
            Log.d("Adapter", "Accept clicked for ID: ${booking.bookingId}")  // Debugging ke liye
            onAcceptClick(booking.bookingId)  // Accept button click hone par function call
        }

        holder.btnReject.setOnClickListener {
            Log.d("Adapter", "Reject clicked for ID: ${booking.bookingId}")  // Debugging ke liye
            onRejectClick(booking.bookingId)  // Reject button click hone par function call
        }
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun getItemCount() = bookings.size
}
