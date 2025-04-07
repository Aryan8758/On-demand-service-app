package com.example.foryou

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CateroryAdpater2(private val categories: List<CategoriesItemModel>) : RecyclerView.Adapter<CateroryAdpater2.CategoryViewHolder>() {
    private lateinit var sharedPreferences: SharedPref
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       // val icon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val card : CardView = itemView.findViewById(R.id.card)
        val title: TextView = itemView.findViewById(R.id.tv_service_name)
       // val expandButton: ImageView = itemView.findViewById(R.id.expandButton)
        val image :ImageView = itemView.findViewById(R.id.serviceImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        var selectedArea: String? = null
        val context=holder.itemView.context
        sharedPreferences = SharedPref(context)
        val category = categories[position]
        holder.title.text = category.title
        holder.image.setImageResource(category.imageResId)
        //holder.icon.setImageResource(category.icon)
//        holder.expandButton.setOnClickListener {
//            // Toggle expand/collapse logic
//        }

        holder.itemView.setOnClickListener {
            val savedLocation = sharedPreferences.getLocation()

            if (!savedLocation.isNullOrEmpty()) {
                val area = savedLocation.split(",").firstOrNull()?.trim()
                Log.d("Location", "Loaded saved location: $savedLocation,$area")
                selectedArea=if (area!=null) area else null
            } else {
                Log.d("Location", "No saved location found")
                selectedArea = null
            }
          //  Toast.makeText(context, "Clicked: $selectedArea", Toast.LENGTH_SHORT).show()
            val intent = Intent(holder.itemView.context,ProviderList::class.java)
            intent.putExtra("categoryname",category.title)
            intent.putExtra("location",selectedArea)
            context.startActivity(intent)
        }

//    holder.card.setBackgroundResource(category.bgColor)
}

    override fun getItemCount(): Int {
        return categories.size
    }
}
