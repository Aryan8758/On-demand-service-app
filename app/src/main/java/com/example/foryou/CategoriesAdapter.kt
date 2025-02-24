package com.example.foryou

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(private val itemList:List<CategoriesItem>):RecyclerView.Adapter<CategoriesAdapter.ItemViewHolder>() {
    class ItemViewHolder(view:View):RecyclerView.ViewHolder(view){
        val ServiceImage:ImageView=view.findViewById(R.id.serviceimage)
        val ServiceName:TextView=view.findViewById(R.id.servicename)
        val cardView: CardView = itemView.findViewById(R.id.cardCategory)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.categoriesdesign,parent,false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
       return itemList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item=itemList[position]
        holder.ServiceName.text = item.title
        holder.ServiceImage.setImageResource(item.imageResId)
        holder.cardView.setCardBackgroundColor(Color.parseColor(item.bgColor))
    }
}