package com.example.foryou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProviderAdapter(private val providers: List<ProviderModelClass>) :
    RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.nameTextView)
        val serviceText: TextView = itemView.findViewById(R.id.serviceTextView)
       // val imageView: ImageView = itemView.findViewById(R.id.providerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.nameText.text = provider.name
        holder.serviceText.text = provider.service
      //  Glide.with(holder.itemView.context).load(provider.imageUrl).into(holder.imageView)
    }

    override fun getItemCount(): Int = providers.size
}
