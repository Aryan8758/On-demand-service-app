package com.example.foryou

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityProviderDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class Provider_Detail : AppCompatActivity() {
    private val binding:ActivityProviderDetailBinding by lazy {
        ActivityProviderDetailBinding.inflate(layoutInflater)
    }
    private val db=FirebaseFirestore.getInstance();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        val ProviderId = intent.getStringExtra("ProviderId") ?: "Unknown"
        var priceRate=""
        db.collection("providers").document(ProviderId).get()
            .addOnSuccessListener { document->
                if (document.exists()){
                    val name = document.getString("name") ?: "Unknown"
                    val service = document.getString("service") ?: "Unknown Service"
                    val exp = document.getString("experience") ?: "N/A"
                    val contact = document.getString("number") ?: "Not Available"
                    val address = document.getString("city") ?: "Not Available"
                    val description = document.getString("aboutBio") ?: "No description available."
                     priceRate = document.getString("priceRate") ?: "No description available."
                    val image = document.getString("profileImage") ?: ""
                    binding.providerName.text=name
                    binding.serviceName.text=service
                    binding.providerAddress.text="$exp Years Experience"
                    binding.providerContact.text="📞 $contact"
                    binding.providerAddress.text="📍 $address"
                    binding.serviceDescription.text=description
                    binding.servicePrice.text="Price:$priceRate"
                    binding.serviceImage.setImageBitmap(decodeBase64ToBitmap(image))
                    // Data Fetch Successful, Hide Shimmer and Show Content
                    binding.shimmerViewContainer.stopShimmer()
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.mainContent.visibility = View.VISIBLE
                }
            }
        val ServiceName = intent.getStringExtra("servicename") ?: "Unknown"
//        val image = intent.getStringExtra("image")
//        val Seviceimage= image?.let { decodeBase64ToBitmap(it) }
//        binding.serviceImage.setImageBitmap(Seviceimage)
        binding.bookServiceButton.setOnClickListener {
            val intent=Intent(this,BookingActivity::class.java)
            intent.putExtra("ServiceName",ServiceName)
            intent.putExtra("ProviderId",ProviderId)
            intent.putExtra("PricerRate",priceRate)
            startActivity(intent)


        }



    }
    private fun decodeBase64ToBitmap(base64String: String):Bitmap?{
     val decodeBytes=Base64.decode(base64String,Base64.DEFAULT)
     return BitmapFactory.decodeByteArray(decodeBytes,0,decodeBytes.size)
    }

}