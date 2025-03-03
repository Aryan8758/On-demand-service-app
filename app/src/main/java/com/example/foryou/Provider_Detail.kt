package com.example.foryou

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityProviderDetailBinding

class Provider_Detail : AppCompatActivity() {
    private val binding:ActivityProviderDetailBinding by lazy {
        ActivityProviderDetailBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        val ProviderName = intent.getStringExtra("providername") ?: "Unknown"
        val ServiceName = intent.getStringExtra("servicename") ?: "Unknown"
        val image = intent.getStringExtra("image")
        val Seviceimage= image?.let { decodeBase64ToBitmap(it) }
        binding.serviceImage.setImageBitmap(Seviceimage)
        binding.serviceName.text=ServiceName
        binding.ProviderName.text=ProviderName
        binding.bookServiceButton.setOnClickListener {
            startActivity(Intent(this,BookingActivity::class.java))
//            val bottomSheet = BookingBottomSheet()
//            bottomSheet.show(supportFragmentManager, "BookingBottomSheet")

        }



    }
    private fun decodeBase64ToBitmap(base64String: String):Bitmap?{
     val decodeBytes=Base64.decode(base64String,Base64.DEFAULT)
     return BitmapFactory.decodeByteArray(decodeBytes,0,decodeBytes.size)
    }

}