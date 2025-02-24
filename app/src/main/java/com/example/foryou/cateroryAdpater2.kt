package com.example.foryou

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class cateroryAdpater2(private val categories: List<CategoriesItem2>) : RecyclerView.Adapter<cateroryAdpater2.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       // val icon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val title: TextView = itemView.findViewById(R.id.categoryTitle)
        val expandButton: ImageView = itemView.findViewById(R.id.expandButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.caterorydesign2, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.title.text = category.title
        //holder.icon.setImageResource(category.icon)
        holder.expandButton.setOnClickListener {
            // Toggle expand/collapse logic
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}
