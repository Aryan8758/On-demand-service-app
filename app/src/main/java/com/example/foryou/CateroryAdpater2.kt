package com.example.foryou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CateroryAdpater2(private val categories: List<CategoriesItem>) : RecyclerView.Adapter<CateroryAdpater2.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       // val icon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val card : CardView = itemView.findViewById(R.id.card)
        val title: TextView = itemView.findViewById(R.id.categoryTitle)
        val expandButton: ImageView = itemView.findViewById(R.id.expandButton)
        val image :ImageView = itemView.findViewById(R.id.categoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.caterorydesign2, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.title.text = category.title
        holder.image.setImageResource(category.imageResId)
        //holder.icon.setImageResource(category.icon)
        holder.expandButton.setOnClickListener {
            // Toggle expand/collapse logic
        }
    holder.card.setBackgroundResource(category.bgColor)
}

    override fun getItemCount(): Int {
        return categories.size
    }
}
