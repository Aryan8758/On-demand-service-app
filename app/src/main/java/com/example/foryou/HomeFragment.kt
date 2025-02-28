package com.example.foryou


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foryou.CategoriesAdapter
import com.example.foryou.CategoriesItem
import com.example.foryou.ProviderAdapter
import com.example.foryou.ProviderModelClass
import com.example.foryou.R
import com.example.foryou.SharedPref
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
//categories adapter set manually
        val categoryList = listOf(
            CategoriesItem(R.drawable.ak, "Plumbing Services", R.drawable.category_background),
            CategoriesItem(R.drawable.ak, "Electrician Services", R.drawable.blue_bg),
            CategoriesItem(R.drawable.ak, "Laundry Service", R.drawable.red_bg),
            CategoriesItem(R.drawable.ak, "Chef Services", R.drawable.green_bg),
            CategoriesItem(R.drawable.ak, "Washing & Cleaning", R.drawable.purple_bg),
            CategoriesItem(R.drawable.ak, "Maid Services", R.drawable.yellow_bg),
            CategoriesItem(R.drawable.ak, "Carpenter Services", R.drawable.blue_bg),  // Duplicate color
            CategoriesItem(R.drawable.ak, "Mechanic Services", R.drawable.red_bg),    // Duplicate color
            CategoriesItem(R.drawable.ak, "Gardening Services", R.drawable.green_bg), // Duplicate color
            CategoriesItem(R.drawable.ak, "Painting Services", R.drawable.purple_bg)  // Duplicate color
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
        binding.recyclerViewCategories.adapter = CategoriesAdapter(categoryList)



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
    // Step 1: Fetch Providers & Categorize Them
    private fun fetchProviders() {
        if (!isAdded || view == null) return // Prevents crash if fragment is not attached

        db.collection("providers").get()
            .addOnSuccessListener { documents ->
                if (!isAdded || view == null) return@addOnSuccessListener // Ensures fragment is still active

                providersMap.clear() // Clear previous data to avoid duplication

                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val service = doc.getString("service") ?: "Other"
                    val image = doc.getString("profileImage")

                    // Add provider to respective category list
                    if (!providersMap.containsKey(service)) {
                        providersMap[service] = mutableListOf()
                    }
                    providersMap[service]?.add(ProviderModelClass(name, service, image))
                }

                displayProvidersByCategory() // Generate dynamic RecyclerViews safely
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting providers", exception)
            }
    }

    private fun displayProvidersByCategory() {
        if (!isAdded || view == null) return // Prevents crash if fragment is not attached

        binding?.categoryContainer?.removeAllViews() // Null-check to prevent crashes

        for ((category, providerList) in providersMap) {
            val categoryTitle = TextView(requireContext()).apply {
                text = category
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 16, 8, 8)
            }

            val recyclerView = RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = ProviderAdapter(providerList)
            }

            binding?.categoryContainer?.addView(categoryTitle)
            binding?.categoryContainer?.addView(recyclerView)
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
