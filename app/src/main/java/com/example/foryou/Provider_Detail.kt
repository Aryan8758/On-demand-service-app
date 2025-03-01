package com.example.foryou

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        val image = intent.getIntExtra("image",0)
        binding.serviceImage.setImageResource(image)
        binding.serviceName.text=ServiceName
        binding.ProviderName.text=ProviderName



    }
}