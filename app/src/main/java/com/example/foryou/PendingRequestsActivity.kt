package com.example.foryou

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foryou.databinding.ActivityPendingRequestsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PendingRequestsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPendingRequestsBinding
    private lateinit var adapter: PendingRequestsAdapter
    private val providerList = mutableListOf<AdminProviderModel>()
    private lateinit var db: FirebaseFirestore
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // RecyclerView Setup
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PendingRequestsAdapter(this,providerList)
        binding.recyclerView.adapter = adapter

        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        // 🔥 Real-time snapshot listener
        listener = db.collection("providers")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("❌ Error fetching pending requests: ${e.message}")
                    return@addSnapshotListener
                }

                providerList.clear()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot) {
                        val provider = document.toObject(AdminProviderModel::class.java)
                        provider.id = document.id
                        providerList.add(provider)
                    }

                    // ✅ Show data
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.textNoRequests.visibility = View.GONE
                } else {
                    // ❌ No pending requests
                    binding.recyclerView.visibility = View.GONE
                    binding.textNoRequests.visibility = View.VISIBLE
                }

                // Stop Shimmer Effect
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE

                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove() // 🔴 Remove Firestore listener to prevent memory leaks
    }
}
