package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foryou.databinding.ActivityEmailVerifyBinding
import com.example.foryou.databinding.ActivityLoginBinding

class EmailVerify : AppCompatActivity() {
    private val binding : ActivityEmailVerifyBinding by lazy {
        ActivityEmailVerifyBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.otpEditText.visibility = View.INVISIBLE
        binding.Signinbtn.visibility = View.INVISIBLE
        binding.otpsendbtn.setOnClickListener {
            binding.otpsendbtn.visibility = View.INVISIBLE
            binding.otpEditText.visibility = View.VISIBLE
            binding.Signinbtn.visibility = View.VISIBLE
        }
        binding.Signinbtn.setOnClickListener{
            startActivity(Intent(this,SignUp::class.java))
        }
    }
}