package com.example.foryou

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(private val itemList:List<CategoriesItemModel>):RecyclerView.Adapter<CategoriesAdapter.ItemViewHolder>() {
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
        val context=holder.itemView.context
        holder.ServiceName.text = item.title
        holder.ServiceImage.setImageResource(item.imageResId)
       // holder.cardView.setBackgroundResource(item.bgColor)
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
            val intent = Intent(holder.itemView.context,ProviderList::class.java)
            intent.putExtra("categoryname",item.title)
           context.startActivity(intent)
            // Navigate to a new screen or show full list of providers
             }
    }
}