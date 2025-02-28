package com.example.foryou

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foryou.databinding.ActivityProviderListBinding

class ProviderList : AppCompatActivity() {
    private val binding:ActivityProviderListBinding by lazy {
        ActivityProviderListBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

    }
}