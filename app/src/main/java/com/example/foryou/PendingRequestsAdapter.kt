package com.example.foryou

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PendingRequestsAdapter(private val providerList: List<AdminProviderModel>) :
    RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val provider = providerList[position]
        holder.name.text = provider.name
        holder.email.text = provider.email
        holder.service.text = provider.role

        holder.btnApprove.setOnClickListener {
            updateProviderStatus(provider.id, "approved")
        }

        holder.btnReject.setOnClickListener {
            deleteProvider(provider.id)
        }
    }

    override fun getItemCount(): Int {
        return providerList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.providerName)
        val email: TextView = itemView.findViewById(R.id.providerEmail)
        val service: TextView = itemView.findViewById(R.id.providerService)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    private fun updateProviderStatus(providerId: String, status: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("providers").document(providerId)
            .update("status", status)
            .addOnSuccessListener {
                Log.d("Provider", "Updated to $status")
            }
    }

    private fun deleteProvider(providerId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("providers").document(providerId)
            .delete()
            .addOnSuccessListener {
                Log.d("Provider", "Deleted")
            }
    }
}
