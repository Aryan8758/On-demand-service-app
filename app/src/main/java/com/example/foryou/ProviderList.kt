package com.example.foryou

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foryou.databinding.ActivityProviderListBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProviderList : AppCompatActivity() {
    private val binding:ActivityProviderListBinding by lazy {
        ActivityProviderListBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: ProviderAdapter
    private val providerList = mutableListOf<ProviderModelClass>()
    private val db = FirebaseFirestore.getInstance() // Firestore instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


            // Get category name from Intent
            val category = intent.getStringExtra("categoryname") ?: "Unknown"

            // Set category title
            binding.ProviderTitle.text = category



            // Setup RecyclerView
        binding.recyclerViewCategories.layoutManager = GridLayoutManager(this, 2)

        adapter = ProviderAdapter(providerList)
            binding.recyclerViewCategories.adapter = adapter
        // Fetch data from Firestore
        fetchProvidersFromFirestore(category)
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }



    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchProvidersFromFirestore(servicetype: String) {
        db.collection("providers").whereEqualTo("service",servicetype)
            .get()
            .addOnSuccessListener { documents ->
                providerList.clear()
                for (document in documents){
                    val name = document.getString("name") ?: "" // Null-safe getString
                    val service = document.getString("service") ?: ""
                    val image = document.getString("profileImage")

                    val provider = ProviderModelClass(name, service, image)
                    providerList.add(provider)
                }
                adapter.notifyDataSetChanged() // Refresh RecyclerView
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
    }

}