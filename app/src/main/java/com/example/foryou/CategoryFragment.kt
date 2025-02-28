package com.example.foryou

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        val categoryRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewCategories)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val backButton:ImageView =view.findViewById( R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }



        val categoryList = listOf(
            CategoriesItem(R.drawable.ak, "Plumbing Services", R.drawable.category_background),
            CategoriesItem(R.drawable.ak, "Electrician Services", R.drawable.blue_bg),
            CategoriesItem(R.drawable.laundary, "Laundry Service", R.drawable.red_bg),
            CategoriesItem(R.drawable.ak, "Chef Services", R.drawable.green_bg),
            CategoriesItem(R.drawable.wash, "Washing & Cleaning", R.drawable.purple_bg),
            CategoriesItem(R.drawable.ak, "Maid Services", R.drawable.yellow_bg),
            CategoriesItem(R.drawable.hair, "Carpenter Services", R.drawable.blue_bg),  // Duplicate color
            CategoriesItem(R.drawable.car, "Mechanic Services", R.drawable.red_bg),    // Duplicate color
            CategoriesItem(R.drawable.ak, "Gardening Services", R.drawable.green_bg), // Duplicate color
            CategoriesItem(R.drawable.ak, "Painting Services", R.drawable.purple_bg)  // Duplicate color
        )
        val adapter = CateroryAdpater2(categoryList)
        categoryRecyclerView.adapter = adapter
        return view
    }
}
