package com.example.foryou

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class CustomerListAdapter(
    private var customerList: MutableList<CustomerListModel>,
    private val userType: String // "customer" or "provider"
) : RecyclerView.Adapter<CustomerListAdapter.CustomerViewHolder>() {

    class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerImage: ImageView = view.findViewById(R.id.customerImage)
        val customerName: TextView = view.findViewById(R.id.customerName)
        val customerEmail: TextView = view.findViewById(R.id.customerEmail)
        val customerPhone: TextView = view.findViewById(R.id.customerPhone)
        val serviceName: TextView = view.findViewById(R.id.serviceName)
        val optionsMenu: ImageButton = view.findViewById(R.id.optionsMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customerList[position]

        holder.customerName.text = customer.name
        holder.customerEmail.text = customer.email
        holder.customerPhone.text = customer.phone
        if (userType=="providers"){
            holder.serviceName.visibility=View.VISIBLE
            holder.serviceName.text=customer.serviceName
        }

        // ✅ Optimized image loading
        if (!customer.photoUrl.isNullOrEmpty()) {
            holder.customerImage.setImageBitmap(decodeBase64ToBitmap(customer.photoUrl))
        } else {
            holder.customerImage.setImageResource(R.drawable.tioger) // Default image
        }

        // Handling popup menu
        holder.optionsMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, holder.optionsMenu)
            popup.menuInflater.inflate(R.menu.delete_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.deleteCustomer) {
                    if (userType == "customer") {
                        deleteUser(view, position, customer.id, "customers")
                    } else {
                        deleteUser(view, position, customer.id, "providers")
                    }
                    true
                } else {
                    false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = customerList.size

    private fun deleteUser(view: View, position: Int, userId: String, collection: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection(collection).document(userId)
            .delete()
            .addOnSuccessListener {
                val bookingRef = FirebaseDatabase.getInstance().getReference("booking")

                if (collection == "customers") {
                    // ✅ Customer delete ho raha hai, to "booking -> customerId" remove karna hai
                    bookingRef.child(userId).removeValue()
                } else if (collection == "providers") {
                    // ✅ Provider delete ho raha hai, to uske saare linked bookings delete karni hai
                    removeProviderBookings(userId)
                }

                Toast.makeText(view.context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                customerList.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener {
                Toast.makeText(view.context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }
    private fun removeProviderBookings(providerId: String) {
        val bookingRef = FirebaseDatabase.getInstance().getReference("booking")
        val db=FirebaseFirestore.getInstance()
        db.collection("slots").document(providerId).delete()

        bookingRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (customer in snapshot.children) {
                    for (booking in customer.children) {
                        val currentProviderId = booking.child("ProviderId").value.toString()
                        if (currentProviderId == providerId) {
                            booking.ref.removeValue()  // ✅ Sirf wo bookings delete jo is provider ke saath hain
                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error deleting provider's bookings: ${e.message}")
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
