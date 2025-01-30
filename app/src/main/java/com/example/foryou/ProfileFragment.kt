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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
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
    private lateinit var sharedPreferences: SharedPref // Declare SharedPref

    private var selectBitmap:Bitmap?=null
    private val imagePickerLauncher=registerForActivityResult(ActivityResultContracts.GetContent()){uri ->
        uri?.let {
            binding.image.setImageURI(uri)
            selectBitmap=BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))

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

        // Initialize SharedPref with activity context
        sharedPreferences = SharedPref(requireActivity().applicationContext)


        // Enable Firestore offline caching
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        loadUser()
        binding.editProfileBtn.setOnClickListener {
//            binding.userName.isEnabled=true
//            binding.userPhone.isEnabled=true
//            binding.editProfileBtn.visibility=View.GONE
//            binding.logoutBtn.visibility=View.GONE
//            binding.saveBtn.visibility=View.VISIBLE
            enableEditing(true)
        }
        binding.logoutBtn.setOnClickListener {
            // Clear login state using SharedPreferences
            sharedPreferences.logoutUser()
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
    private fun encodeImageToBase64(bitmap:Bitmap):String{
        val byteArryOutputStream=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArryOutputStream)
        val byteArray=byteArryOutputStream.toByteArray()
        return Base64.encodeToString(byteArray,Base64.DEFAULT)
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    @SuppressLint("SetTextI18n")
    private fun loadUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("ProfileFragment", "User ID is null")
            return
        }

        // Show default values while loading
        binding.userName.setText("Loading...")
        binding.userEmail.setText("Loading...")
        binding.userPhone.setText("Loading...")
        binding.progressBar.visibility = View.VISIBLE

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
                    Log.e("ProfileFragment", "Error fetching user data", e)
                }
            }
        }
    }

    private fun updateUI(document: com.google.firebase.firestore.DocumentSnapshot) {
        if (!document.exists()) {
            Log.e("ProfileFragment", "User not found in Firestore")
            return
        }

        val name = document.getString("name") ?: "No Name"
        val email = document.getString("email") ?: "No Email"
        val phone = document.getString("number") ?: "No Phone"
        val base64Image = document.getString("profileImage")
        if (base64Image != null) {
            binding.image.setImageBitmap(decodeBase64ToBitmap(base64Image))
        }
        else   {
            binding.image.setImageResource(R.drawable.ak)
        }

        binding.userName.setText(name)
        binding.userEmail.setText(email)
        binding.userPhone.setText(phone)
        binding.progressBar.visibility = View.GONE

    }
    private fun enableEditing(enable: Boolean) {
        binding.userName.isEnabled = enable
        binding.userPhone.isEnabled = enable
        binding.editimage.visibility=if(enable) View.VISIBLE else View.GONE
        binding.saveBtn.visibility = if (enable) View.VISIBLE else View.GONE
        binding.logoutBtn.visibility = if (!enable) View.VISIBLE else View.GONE
        binding.editProfileBtn.visibility = if (!enable) View.VISIBLE else View.GONE
    }

    private  fun updateProfile() {
        val updatedName = binding.userName.text.toString().trim()
        val updatedPhone = binding.userPhone.text.toString().trim()
        if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(requireContext(), "Name & Phone can't be empty!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val user = mutableMapOf<String, Any>(
            "name" to updatedName,
            "number" to updatedPhone
        )
        selectBitmap?.let {
            val base64Image = encodeImageToBase64(it)
            user["profileImage"] = base64Image
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {

                db.collection("customers").document(auth.currentUser?.uid!!).update(user).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                    enableEditing(false)
                }
            }catch (e:Exception){
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
