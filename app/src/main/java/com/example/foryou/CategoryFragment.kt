package com.example.foryou

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            CategoriesItemModel(R.drawable.plumber1, "Plumber", R.drawable.ex),
            CategoriesItemModel(R.drawable.teacher, "Teacher", R.drawable.ex),
            CategoriesItemModel(R.drawable.driver1, "Driver", R.drawable.ex),
            CategoriesItemModel(R.drawable.electrician, "Electrician", R.drawable.ex),
            CategoriesItemModel(R.drawable.mechanic, "Mechanic", R.drawable.ex),
            CategoriesItemModel(R.drawable.gardner, "Gardener", R.drawable.ex),
            CategoriesItemModel(R.drawable.painter, "Painter", R.drawable.ex),
            CategoriesItemModel(R.drawable.chef, "Chef", R.drawable.ex),
            CategoriesItemModel(R.drawable.maid, "Maid", R.drawable.ex),
            CategoriesItemModel(R.drawable.babysitter, "Babysitter", R.drawable.ex)
        )

        val adapter = CateroryAdpater2(categoryList)
        categoryRecyclerView.adapter = adapter
        return view
    }
}
