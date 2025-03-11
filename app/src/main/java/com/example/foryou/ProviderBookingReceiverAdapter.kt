package com.example.foryou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProviderBookingReceiverAdapter(
    private val bookings: List<ProviderBookingReceiverModel>,
    private val onAcceptClick: (String) -> Unit,
    private val onRejectClick: (String) -> Unit
) : RecyclerView.Adapter<ProviderBookingReceiverAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtServiceName: TextView = view.findViewById(R.id.txtServiceName)
        val txtCustomerName: TextView = view.findViewById(R.id.txtCustomerName)
        val txtBookingDate: TextView = view.findViewById(R.id.txtBookingDate)
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
        holder.txtBookingDate.text = "Date: ${booking.bookingDate}"

        holder.btnAccept.setOnClickListener { onAcceptClick(booking.bookingId) }
        holder.btnReject.setOnClickListener { onRejectClick(booking.bookingId) }
    }

    override fun getItemCount() = bookings.size
}
