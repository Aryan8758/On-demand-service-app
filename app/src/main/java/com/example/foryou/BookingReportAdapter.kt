package com.example.foryou

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BookingReportAdapter(private val bookingList: List<Booking_model>) :
    RecyclerView.Adapter<BookingReportAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val serviceProvider: TextView = itemView.findViewById(R.id.tvServiceProvider)
        val dateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val AlldetailLayout:LinearLayout=itemView.findViewById(R.id.AlldetailLayout)
        val BookingReportLayout:LinearLayout=itemView.findViewById(R.id.BookingReportLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_customer, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]

        // Default UI Setup
        holder.AlldetailLayout.visibility = View.GONE
        holder.BookingReportLayout.visibility = View.VISIBLE
        holder.customerName.text = booking.customerName
        holder.serviceProvider.text = "Service: ${booking.service} | Provider: Loading..."
        holder.dateTime.text = "Date: ${booking.bookingDate} | Time: ${booking.timeSlot}"
        holder.status.text = "Status: ${booking.status}"

        // Status color change
        // 🔹 Status ke colors define kar diye
        val statusColor = when (booking.status) {
            "Accepted" -> Color.GREEN
            "Pending" -> Color.YELLOW
            "Order Cancel" -> Color.GRAY
            "Rejected" -> Color.RED
            "Completed" -> Color.BLUE
            else -> Color.BLACK
        }

// 🔹 Status ka text color set karo
        holder.status.setTextColor(statusColor)


        // **Asynchronously Provider Name fetch karo**
        FirebaseFirestore.getInstance().collection("providers") // 🔹 "providers" collection ka naam sahi karo
            .document(booking.ProviderId)
            .get()
            .addOnSuccessListener { task ->
                val providerName = task.getString("name") ?: "Unknown"

                // ✅ **Ab UI ko yahan update karo**
                holder.serviceProvider.text = "Service: ${booking.service} | Provider: $providerName"
            }
    }

    override fun getItemCount(): Int = bookingList.size
}
