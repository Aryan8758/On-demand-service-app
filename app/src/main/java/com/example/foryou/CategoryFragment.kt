package com.example.foryou

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val categoryList = listOf(
            CategoriesItem2("Car Services"),
            CategoriesItem2("Medical Services"),
            CategoriesItem2("Laundry Service"),
            CategoriesItem2("Beauty & Hair Cuts"),
            CategoriesItem2("Washing & Cleaning"),
            CategoriesItem2("Media & Photography")
        )

        val adapter = cateroryAdpater2(categoryList)
        categoryRecyclerView.adapter = adapter

        return view
    }
}
