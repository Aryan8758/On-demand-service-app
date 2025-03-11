package com.example.foryou


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foryou.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPref
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val providersMap = mutableMapOf<String, MutableList<ProviderModelClass>>() // Categorized providers


    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): android.view.View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchProviders()
        }





//categories adapter set manually
        val categoryList = listOf(
            CategoriesItemModel(R.drawable.ak, "Plumber", R.drawable.blue_bg),
            CategoriesItemModel(R.drawable.ak, "Teacher", R.drawable.red_bg),
            CategoriesItemModel(R.drawable.ak, "Driver", R.drawable.green_bg),
            CategoriesItemModel(R.drawable.ak, "Electrician", R.drawable.purple_bg),
            CategoriesItemModel(R.drawable.ak, "Mechanic", R.drawable.yellow_bg),
            CategoriesItemModel(R.drawable.ak, "Gardener", R.drawable.blue_bg),
            CategoriesItemModel(R.drawable.ak, "Painter", R.drawable.red_bg),
            CategoriesItemModel(R.drawable.ak, "Chef", R.drawable.green_bg),
            CategoriesItemModel(R.drawable.ak, "Maid", R.drawable.purple_bg),
            CategoriesItemModel(R.drawable.ak, "Babysitter", R.drawable.yellow_bg)
        )

        binding.profileLayout.setOnClickListener {
            (requireActivity() as MainActivity).updateBottomNavSelection(R.id.nav_per)
        }
        binding.viewAllButton.setOnClickListener {

            // ✅ Bottom Navigation ka selected item update karo (MainActivity se access karna padega)
            (requireActivity() as MainActivity).updateBottomNavSelection(R.id.nav_cat)
        }

        //   val layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        val layoutManager = GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCategories.layoutManager = layoutManager
        val adapter=CategoriesAdapter(categoryList)
        binding.recyclerViewCategories.adapter = adapter



        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Enable Firestore offline caching
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        sharedPreferences = SharedPref(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        loadUser()
        loadSavedLocation()

        binding.editimage.setOnClickListener {
            fetchLocation()
        }
        fetchProviders()
    }
    private fun fetchProviders() {
        if (!isAdded || view == null) return // Prevents crash if fragment is not attached

        binding?.categoryContainer?.removeAllViews()

        db.collection("providers").get()
            .addOnSuccessListener { documents ->
                if (!isAdded || view == null) return@addOnSuccessListener // Ensures fragment is still active

                providersMap.clear() // Clear previous data to avoid duplication

                for (doc in documents) {
                    val providerId=doc.id
                    val name = doc.getString("name") ?: "Unknown"
                    val service = doc.getString("service") ?: "Other"
                    val image = doc.getString("profileImage")
                    val bg = R.drawable.blue_bg

                    if (!providersMap.containsKey(service)) {
                        providersMap[service] = mutableListOf()
                    }
                    providersMap[service]?.add(ProviderModelClass(providerId,name, service, image, bg))
                }

                // 🟢 **Step 2: Show Categories with Shimmer**
                displayProvidersByCategory()

            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting providers", exception)
                binding.swipeRefreshLayout.isRefreshing = false

            }
    }

    private fun displayProvidersByCategory() {
        if (!isAdded || view == null) return

        binding?.categoryContainer?.removeAllViews()

        for ((category, providerList) in providersMap) {
            val categoryView = LayoutInflater.from(requireContext()).inflate(
                R.layout.provider_home_list_design, binding?.categoryContainer, false
            )

            val categoryTitle = categoryView.findViewById<TextView>(R.id.categoryTitle)
            val viewAllButton = categoryView.findViewById<Button>(R.id.viewAllButton)
            val recyclerView = categoryView.findViewById<RecyclerView>(R.id.providerRecyclerView)

            categoryTitle.text = category
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // 🔥 **Shimmer View Dynamically Add Karein**
            val shimmerView = LayoutInflater.from(requireContext()).inflate(
                R.layout.shimmer_layout, binding?.categoryContainer, false
            )

            binding?.categoryContainer?.addView(shimmerView)

            // 🔥 **Show Shimmer Before Data Loads**
            shimmerView.visibility = View.VISIBLE
            viewAllButton.visibility=View.GONE
            recyclerView.visibility = View.GONE

            // 🔥 **Load Data with Delay**
            Handler(Looper.getMainLooper()).postDelayed({
                shimmerView.visibility = View.GONE
                viewAllButton.visibility=View.VISIBLE
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = ProviderAdapter(providerList)
                binding.swipeRefreshLayout.isRefreshing = false
            }, 2000) // 2 seconds shimmer effect

            viewAllButton.setOnClickListener {
                val intent = Intent(requireContext(), ProviderList::class.java)
                intent.putExtra("categoryname", category)
                startActivity(intent)
            }

            binding?.categoryContainer?.addView(categoryView)
        }
    }



    @SuppressLint("SetTextI18n")
    private fun loadSavedLocation() {
        val savedLocation = sharedPreferences.getLocation()

        if (!savedLocation.isNullOrEmpty()) {
            binding.txtLoc.text = savedLocation
            Log.d("Location", "Loaded saved location: $savedLocation")
        } else {
            binding.txtLoc.text = "Fetching Location..."
            Log.d("Location", "No saved location found")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("HomeFragment", "User ID is null")
            return
        }

        // Show default values while loading
        binding.txtUser.text = "Loading..."
        binding.txtLoc.text = "Loading..."

        // Fetch from cache first, then from server
        lifecycleScope.launch(Dispatchers.IO) {
            try { val collection =sharedPreferences.getUserType()
                val cacheDoc = db.collection(collection).document(userId).get(Source.CACHE).await()
                withContext(Dispatchers.Main) {
                    if (cacheDoc.exists()) updateUI(cacheDoc)
                }

                val serverDoc = db.collection(collection).document(userId).get(Source.SERVER).await()
                withContext(Dispatchers.Main) {
                    updateUI(serverDoc)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HomeFragment", "Error fetching user data", e)
                }
            }
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun updateUI(document: com.google.firebase.firestore.DocumentSnapshot) {
        if (!document.exists()) {
            Log.e("HomeFragment", "User not found in Firestore")
            return
        }

        val name = document.getString("name")
        val image = document.getString("profileImage")

        if (image == null) {
            binding.image.setImageResource(R.drawable.ak)
        } else {
            binding.image.setImageBitmap(decodeBase64ToBitmap(image))
        }
        binding.txtUser.text = name
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    fetchCityAndAreaFromGeocoder(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this) // Stop updates after getting location
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun fetchCityAndAreaFromGeocoder(lat: Double, lon: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(lat, lon, 5) // Get multiple nearby addresses
            if (!addresses.isNullOrEmpty()) {
                val mainAddress = addresses[0] // First address (most accurate)

                var city = mainAddress.locality ?: mainAddress.subAdminArea ?: "Unknown City"
                var area = mainAddress.subLocality ?: mainAddress.thoroughfare

                if (area == null) {
                    // If no exact area found, search nearby addresses within ~3-4km
                    val userLocation = Location("").apply {
                        latitude = lat
                        longitude = lon
                    }

                    for (address in addresses) {
                        val tempLocation = Location("").apply {
                            latitude = address.latitude
                            longitude = address.longitude
                        }

                        val distance = userLocation.distanceTo(tempLocation) // Distance in meters

                        if (distance <= 4000 && address.subLocality != null) { // Within 4km and has a valid area name
                            area = address.subLocality
                            break
                        }
                    }
                }

                val finalLocation = if (area != null) "$area, $city" else city

                // Save to SharedPreferences
                sharedPreferences.saveLocation(finalLocation)
                Log.d("Location", "Geocoder Location: $finalLocation")

                requireActivity().runOnUiThread {
                    binding.txtLoc.text = finalLocation
                }
            } else {
                Log.e("Geocoder", "No location found")
                requireActivity().runOnUiThread {
                    binding.txtLoc.text = "Unable to fetch location"
                }
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Error fetching location", e)
            requireActivity().runOnUiThread {
                binding.txtLoc.text = "Error fetching location"
            }
        }
    }

}
