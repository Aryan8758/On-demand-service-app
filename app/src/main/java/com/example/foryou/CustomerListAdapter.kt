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
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customerList[position]

        holder.customerName.text = customer.name
        holder.customerEmail.text = customer.email
        holder.customerPhone.text = customer.phone
        if (customer.photoUrl != null) {
            val img = decodeBase64ToBitmap(customer.photoUrl)
            holder.customerImage.setImageBitmap(img)
        } else {
            holder.customerImage.setImageResource(R.drawable.tioger)
        }
        // Glide.with(holder.itemView.context).load(customer.photoUrl).into(holder.customerImage)

        holder.optionsMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, holder.optionsMenu)
            popup.menuInflater.inflate(R.menu.delete_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.deleteCustomer) {
                    val db = FirebaseFirestore.getInstance()
                    val customerId = customerList[position].id  // Directly get ID from Model

                    db.collection("customers").document(customerId)
                        .delete()
                        .addOnSuccessListener {
                            FirebaseDatabase.getInstance().getReference("booking").child(customerId)
                                .removeValue()
                            Toast.makeText(view.context, "Customer deleted", Toast.LENGTH_SHORT)
                                .show()
                            customerList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                view.context,
                                "Failed to delete customer",
                                Toast.LENGTH_SHORT
                            ).show()
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
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
