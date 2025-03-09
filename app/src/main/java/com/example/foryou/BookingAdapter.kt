package com.example.foryou

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BookingAdapter(private val bookingModelList: List<Booking_model>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceName: TextView = view.findViewById(R.id.tv_service_name)
        val providerName: TextView = view.findViewById(R.id.tv_provider_name)
        val bookingTime: TextView = view.findViewById(R.id.tv_booking_time)
        val servicePrice: TextView = view.findViewById(R.id.tv_service_price)
        val serviceStatus: TextView = view.findViewById(R.id.service_status)
        val serviceImage: ImageView = view.findViewById(R.id.iv_service_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_design, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingModelList[position]

        holder.serviceName.text = booking.service
        holder.providerName.text = booking.ProviderId
        holder.bookingTime.text = "${booking.bookingDate}, ${booking.timeSlot}"
        holder.servicePrice.text = "$50.00"  // You can change this dynamically if price exists
        holder.serviceStatus.text = booking.status

        // Change status color dynamically
        when (booking.status) {
            "Pending" -> holder.serviceStatus.setTextColor(Color.parseColor("#FFA500"))
            "Approved" -> holder.serviceStatus.setTextColor(Color.parseColor("#008000"))
            "Rejected" -> holder.serviceStatus.setTextColor(Color.parseColor("#FF0000"))
        }
        fetchProviderDetails(booking.ProviderId,holder)

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
                else{
                    holder.serviceImage.setImageResource(R.drawable.man)
                }


            }
        }
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
