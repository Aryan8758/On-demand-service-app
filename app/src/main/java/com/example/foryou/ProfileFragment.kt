package com.example.foryou

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foryou.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPref

    private var selectBitmap: Bitmap? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.image.setImageURI(uri)
            selectBitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid
        sharedPreferences = SharedPref(requireActivity().applicationContext)

        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        loadUser()

        binding.editProfileBtn.setOnClickListener {
            enableEditing(true)
        }

        binding.logoutBtn.setOnClickListener {
            // Clear login state using SharedPreferences
            sharedPreferences.logoutUser()
            FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Logout", "✅ FCM Token deleted")
                    } else {
                        Log.e("Logout", "❌ Failed to delete FCM token")
                    }
                }

// Firestore se bhi token remove karo:
            userId?.let { it1 ->
                FirebaseFirestore.getInstance().collection("providers").document(it1)
                    .update("fcmToken", FieldValue.delete())
                    .addOnSuccessListener {
                        Log.d("Logout", "✅ FCM token removed from Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Logout", "❌ Failed to remove FCM token from Firestore: ${e.message}")
                    }
            }

            // Redirect to login activity or perform other actions
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish() // Close the current activity/fragment
        }

        binding.saveBtn.setOnClickListener {
            updateProfile()
        }

        binding.editimage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    @SuppressLint("SetTextI18n")
    private fun loadUser() {
        val userId = auth.currentUser?.uid ?: return
        val userType = sharedPreferences.getUserType()

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cacheDoc = db.collection(userType).document(userId).get(Source.CACHE).await()
                withContext(Dispatchers.Main) { if (cacheDoc.exists()) updateUI(cacheDoc) }

                val serverDoc = db.collection(userType).document(userId).get(Source.SERVER).await()
                withContext(Dispatchers.Main) { updateUI(serverDoc) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ProfileFragment", "Error fetching user data", e)
                }
            }
        }
    }

    private fun updateUI(document: com.google.firebase.firestore.DocumentSnapshot) {
        if (!document.exists()) return

        val userType = sharedPreferences.getUserType()
        val name = document.getString("name") ?: "No Name"
        val email = document.getString("email") ?: "No Email"
        val phone = document.getString("number") ?: "No Phone"
        val base64Image = document.getString("profileImage")
        val isProfileComplete = document.getBoolean("profileComplete") ?: false

        binding.userName.setText(name)
        binding.userEmail.setText(email)
        binding.userPhone.setText(phone)
        binding.image.setImageBitmap(base64Image?.let { decodeBase64ToBitmap(it) } ?: run { null })

        if (userType == "providers") {
            binding.userService.setText(document.getString("service") ?: "")
            binding.userExperience.setText(document.getString("experience") ?: "")
            binding.userCity.setText(document.getString("city") ?: "")
            binding.aboutBio.setText(document.getString("aboutBio") ?: "")
            binding.priceRate.setText(document.getString("priceRate") ?: "")

            binding.providerFields.visibility = View.VISIBLE
        } else {
            binding.providerFields.visibility = View.GONE
        }

        binding.progressBar.visibility = View.GONE
    }

    private fun enableEditing(enable: Boolean) {
        binding.userName.isEnabled = enable
        binding.userPhone.isEnabled = enable
        binding.userService.isEnabled = enable
        binding.userExperience.isEnabled = enable
        binding.userCity.isEnabled = enable
        binding.aboutBio.isEnabled = enable
        binding.priceRate.isEnabled = enable
        binding.editimage.visibility = if (enable) View.VISIBLE else View.GONE
        binding.saveBtn.visibility = if (enable) View.VISIBLE else View.GONE
        binding.logoutBtn.visibility = if (!enable) View.VISIBLE else View.GONE
        binding.editProfileBtn.visibility = if (!enable) View.VISIBLE else View.GONE

        if (sharedPreferences.getUserType() == "providers") {
            binding.providerFields.visibility = if (enable) View.VISIBLE else View.GONE
        }
    }

    private fun updateProfile() {
        val updatedName = binding.userName.text.toString().trim()
        val updatedPhone = binding.userPhone.text.toString().trim()

        if (updatedName.isEmpty()) {
            binding.userName.error = "Name can't be empty"
            return
        }

        if (updatedPhone.isEmpty()) {
            binding.userPhone.error = "Phone can't be empty"
            return
        }

        val userType = sharedPreferences.getUserType()
        val user = mutableMapOf<String, Any>(
            "name" to updatedName,
            "number" to updatedPhone
        )

        var isProfileComplete = true

        if (userType == "providers") {
            val updatedService = binding.userService.text.toString().trim()
            val updatedExperience = binding.userExperience.text.toString().trim()
            val updatedCity = binding.userCity.text.toString().trim()
            val updatedAboutBio = binding.aboutBio.text.toString().trim()
            val updatedPriceRate = "₹" + binding.priceRate.text.toString().trim()

            if (updatedService.isEmpty()) {
                binding.userService.error = "Service can't be empty"
                isProfileComplete = false
            }
            if (updatedExperience.isEmpty()) {
                binding.userExperience.error = "Experience can't be empty"
                isProfileComplete = false
            }
            if (updatedCity.isEmpty()) {
                binding.userCity.error = "City can't be empty"
                isProfileComplete = false
            }
            if (updatedAboutBio.isEmpty()) {
                binding.aboutBio.error = "Bio can't be empty"
                isProfileComplete = false
            }
            if (updatedPriceRate.isEmpty()) {
                binding.priceRate.error = "Price Rate can't be empty"
                isProfileComplete = false
            }

            user["service"] = updatedService
            user["experience"] = updatedExperience
            user["city"] = updatedCity
            user["aboutBio"] = updatedAboutBio
            user["priceRate"] = updatedPriceRate
        }

        // Agar profile complete nahi hai toh update nahi karega
        if (!isProfileComplete) {
            Toast.makeText(requireContext(), "Please fill all required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        selectBitmap?.let {
            val base64Image = encodeImageToBase64(it)
            user["profileImage"] = base64Image
        }

        user["profileComplete"] = isProfileComplete

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection(userType).document(auth.currentUser?.uid!!).update(user).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                    enableEditing(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ProfileFragment", "Error updating profile", e)
                    Toast.makeText(requireContext(), "Update failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
