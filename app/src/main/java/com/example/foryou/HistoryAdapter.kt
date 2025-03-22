package com.example.foryou

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class HistoryAdapter(private val bookingModelList: List<Booking_model>) :
    RecyclerView.Adapter<HistoryAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceName: TextView = view.findViewById(R.id.tv_service_name)
        val providerName: TextView = view.findViewById(R.id.tv_provider)
        val bookingTime: TextView = view.findViewById(R.id.tv_date_time)
        val servicePrice: TextView = view.findViewById(R.id.tv_price)
        val serviceStatus: TextView = view.findViewById(R.id.tv_status)
        val serviceImage: ImageView = view.findViewById(R.id.iv_service_image)
        val menu_options: ImageView = view.findViewById(R.id.menu_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_design, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingModelList[position]

        holder.serviceName.text = booking.service
        holder.bookingTime.text = "${booking.bookingDate}, ${booking.timeSlot}"
        holder.servicePrice.text = booking.PricerRate // You can change this dynamically if price exists
        holder.serviceStatus.text = booking.status


        // Change status color dynamically
        when (booking.status) {
            "Pending" -> {
                holder.serviceStatus.setTextColor(Color.parseColor("#FFA500"))
                holder.menu_options.visibility = View.VISIBLE  // Show menu for Pending
            }
            "Accepted" -> {
                holder.serviceStatus.setTextColor(Color.parseColor("#008000"))
                holder.menu_options.visibility = View.VISIBLE  // Show menu for Accepted
            }
            "Rejected" -> {
                holder.serviceStatus.setTextColor(Color.parseColor("#FF0000"))
                holder.menu_options.visibility = View.GONE  // Hide menu for Rejected
            }
            "Order Cancel" ->{
                holder.serviceStatus.setTextColor(Color.parseColor("#FF0000"))
            }
            else ->{
            holder.serviceStatus.setTextColor(Color.parseColor("#FF7518"))
        }
        }
        fetchProviderDetails(booking.ProviderId, holder)

        holder.menu_options.setOnClickListener { view ->
            val popup = PopupMenu(view.context, holder.menu_options)
            popup.menuInflater.inflate(R.menu.order_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.cancel_order -> {
                        cancelBooking(
                            holder.itemView.context,
                            booking.CustomerId,
                            booking.ProviderId,
                            booking.bookingId,
                            booking.bookingDate,
                            booking.timeSlot
                        )
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }



        // Set service image dynamically (You can change this logic)
        //holder.serviceImage.setImageResource(R.drawable.man)

    }

    override fun getItemCount() = bookingModelList.size
    private fun fetchProviderDetails(providerId: String, holder: BookingViewHolder) {
        val firestore = FirebaseFirestore.getInstance()
        val providerRef = firestore.collection("providers").document(providerId)

        providerRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val providerName = document.getString("name") ?: "Unknown Provider"
                val providerImage = document.getString("profileImage")

                holder.providerName.text = providerName // Set provider name
                if (providerImage!=null){
                    holder.serviceImage.setImageBitmap(decodeBase64ToBitmap(providerImage))
                }
            }
        }
    }
    // **Cancel Booking function**
    private fun cancelBooking(
        context: Context,
        customerId: String,
        providerId: String,  // 🔥 Provider ID bhi chahiye slot update karne ke liye
        bookingId: String,
        selectedDate: String,
        selectedTimeSlot: String // 🔥 Cancel hone wale slot ka time bhi chahiye
    ) {
        val db = FirebaseDatabase.getInstance().reference

        // **🔥 1️⃣ Booking status "Order Cancel" karo**
        db.child("booking").child(customerId).child(bookingId).child("status")
            .setValue("Order Cancel")
            .addOnSuccessListener {
                // **🔥 2️⃣ Firestore me slot ko "available" karo**
                val slotRef = FirebaseFirestore.getInstance()
                    .collection("slots").document(providerId)
                    .collection(selectedDate).document(selectedTimeSlot)
                val updates = mapOf(
                    "status" to "available",   // ✅ Slot available ho gaya
                    "bookedBy" to ""           // ✅ BookedBy field empty ho gaya
                )

                slotRef.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show()

                        // ✅ **Adapter notify karo**
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update slot!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to cancel booking!", Toast.LENGTH_SHORT).show()
            }
    }


    // **View Booking Details function**
//    private fun viewBookingDetails(context: Context, booking: Booking_model) {
//        val intent = Intent(context, BookingDetailActivity::class.java)
//        intent.putExtra("bookingId", booking.bookingId)
//        context.startActivity(intent)
//    }
//
//    // **Report Issue function**
//    private fun reportIssue(context: Context, bookingId: String) {
//        val intent = Intent(context, ReportIssueActivity::class.java)
//        intent.putExtra("bookingId", bookingId)
//        context.startActivity(intent)
//    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
