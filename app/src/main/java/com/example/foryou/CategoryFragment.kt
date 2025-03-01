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
            CategoriesItem(R.drawable.ak, "Plumber", R.drawable.blue_bg),
            CategoriesItem(R.drawable.ak, "Teacher", R.drawable.red_bg),
            CategoriesItem(R.drawable.ak, "Driver", R.drawable.green_bg),
            CategoriesItem(R.drawable.ak, "Electrician", R.drawable.purple_bg),
            CategoriesItem(R.drawable.ak, "Mechanic", R.drawable.yellow_bg),
            CategoriesItem(R.drawable.ak, "Gardener", R.drawable.blue_bg),
            CategoriesItem(R.drawable.ak, "Painter", R.drawable.red_bg),
            CategoriesItem(R.drawable.ak, "Chef", R.drawable.green_bg),
            CategoriesItem(R.drawable.ak, "Maid", R.drawable.purple_bg),
            CategoriesItem(R.drawable.ak, "Babysitter", R.drawable.yellow_bg)
        )

        val adapter = CateroryAdpater2(categoryList)
        categoryRecyclerView.adapter = adapter
        return view
    }
}
