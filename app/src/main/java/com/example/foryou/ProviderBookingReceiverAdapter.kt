package com.example.foryou

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
        val design_layout: LinearLayout = view.findViewById(R.id.linerLayout)
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

        val customerId = booking.CustomerId
        val db = FirebaseFirestore.getInstance()
        var image=""
        db.collection("customers").document(customerId).addSnapshotListener { documentSnapshot, error ->
            if (documentSnapshot != null) {
                 image = documentSnapshot.getString("profileImage").toString()
                holder.imgProfile.setImageBitmap(image?.let { decodeBase64ToBitmap(it) })
            }
        }

        holder.btnAccept.setOnClickListener {
            Log.d("Adapter", "Accept clicked for ID: ${booking.bookingId}")
            onAcceptClick(booking.bookingId)
        }

        holder.btnReject.setOnClickListener {
            Log.d("Adapter", "Reject clicked for ID: ${booking.bookingId}")
            onRejectClick(booking.bookingId)
        }

        holder.design_layout.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BookingDetailActivity::class.java).apply {
                putExtra("CUSTOMER_ID", booking.CustomerId)
                putExtra("BOOKING_ID", booking.bookingId)
                putExtra("IMAGE",image)
            }
            context.startActivity(intent)
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun getItemCount() = bookings.size
}
