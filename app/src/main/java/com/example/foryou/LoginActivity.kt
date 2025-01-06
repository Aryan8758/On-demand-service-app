package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foryou.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val binding : ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        val Signingbtn:TextView=findViewById(R.id.signintxt)
        Signingbtn.setOnClickListener {
            startActivity(Intent(this,EmailVerify::class.java))
            finish()
        }
        binding.loginbtn.setOnClickListener {
            startActivity(Intent(this,SignUp::class.java))
            finish()
        }
        binding.frgpass.setOnClickListener {

        }

    }
}