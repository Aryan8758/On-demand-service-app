package com.example.foryou

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
       val imageView: ImageView = itemView.findViewById(R.id.providerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider, parent, false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.nameText.text = provider.name
        holder.serviceText.text = provider.service

        if(provider.image!=null){
            val img = decodeBase64ToBitmap(provider.image)
            holder.imageView.setImageBitmap(img)
        }else{
            holder.imageView.setImageResource(R.drawable.man)
        }
    }

    override fun getItemCount(): Int = providers.size
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
