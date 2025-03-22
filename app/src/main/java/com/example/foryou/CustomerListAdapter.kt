package com.example.foryou

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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

class CustomerListAdapter(private var customerList: MutableList<CustomerListModel>) :
    RecyclerView.Adapter<CustomerListAdapter.CustomerViewHolder>() {

    class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerImage: ImageView = view.findViewById(R.id.customerImage)
        val customerName: TextView = view.findViewById(R.id.customerName)
        val customerEmail: TextView = view.findViewById(R.id.customerEmail)
        val customerPhone: TextView = view.findViewById(R.id.customerPhone)
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
                    deleteCustomer(view, position, customer.id)
                    true
                } else {
                    false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = customerList.size

    private fun deleteCustomer(view: View, position: Int, customerId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("customers").document(customerId)
            .delete()
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("booking").child(customerId).removeValue()
                Toast.makeText(view.context, "Customer deleted", Toast.LENGTH_SHORT).show()
                customerList.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener {
                Toast.makeText(view.context, "Failed to delete customer", Toast.LENGTH_SHORT).show()
            }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
