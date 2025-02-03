package com.example.foryou

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foryou.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.mail.FetchProfile

class HomeFragment : Fragment() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
    }


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // Non-nullable binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        // Enable Firestore offline caching
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(requireActivity())

        loadUser()
        // Load the stored location when the app restarts
        loadLocationFromPreferences()
        binding.editimage.setOnClickListener {
            checkLocationPermission()
        }
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    private fun loadUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("ProfileFragment", "User ID is null")
            return
        }

        // Show default values while loading
        binding.txtUser.text="Loading..."
        binding.txtLoc.text="Loading..."

        // Fetch from cache first, then from server
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cacheDoc = db.collection("customers").document(userId).get(Source.CACHE).await()
                withContext(Dispatchers.Main) {
                    if (cacheDoc.exists()) updateUI(cacheDoc)
                }

                val serverDoc = db.collection("customers").document(userId).get(Source.SERVER).await()
                withContext(Dispatchers.Main) {
                    updateUI(serverDoc)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("home", "Error fetching user data", e)

                }
            }
        }
    }

    private fun updateUI(Documnet:com.google.firebase.firestore.DocumentSnapshot) {
      if(!Documnet.exists()){
          Log.e("Home fragment","User not found in firestore")
          return
      }
       val name = Documnet.getString("name")
        val image=Documnet.getString("profileImage")
        if(image == null){
            binding.image.setImageResource(R.drawable.ak)
        }
        else{
            binding.image.setImageBitmap(decodeBase64ToBitmap(image))
        }
        binding.txtUser.text=name
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        // If permission granted, fetch location
        fetchUserLocation()
    }

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                getLocationAreaAndCity(latitude,longitude)
            }else {
                binding.txtLoc.text = "Location not available"
            }
        }.addOnFailureListener {
            // Handle failure
            binding.txtLoc.text = "Failed to get location"
            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getLocationAreaAndCity(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val area = address.subLocality // Area name
                val city = address.locality // City name

                // Set area and city
                if (area != null && city != null) {
                    binding.txtLoc.text = "$area, $city"
                } else {
                    binding.txtLoc.text = "Area and City not found"
                }
            } else {
                binding.txtLoc.text = "Address not found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.txtLoc.text = "Error in getting address"
        }
    }
    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission granted, fetch location
                fetchUserLocation()
            } else {
                // If permission denied, show message
                Toast.makeText(requireContext(), "Permission denied to access location", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Save location to SharedPreferences
    private fun saveLocationToPreferences(latitude: Double, longitude: Double) {
        val editor = sharedPreferences.edit()
        editor.putFloat("latitude", latitude.toFloat())
        editor.putFloat("longitude", longitude.toFloat())
        editor.apply()
    }
    // Load location from SharedPreferences
    private fun loadLocationFromPreferences() {
        val latitude = sharedPreferences.getFloat("latitude", Float.NaN)
        val longitude = sharedPreferences.getFloat("longitude", Float.NaN)

        if (!latitude.isNaN() && !longitude.isNaN()) {
            // If location is saved, display it
            getLocationAreaAndCity(latitude.toDouble(), longitude.toDouble())
        } else {
            binding.txtLoc.text = "Location not available"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Avoid memory leaks
    }
}
