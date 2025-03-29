package com.example.foryou

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class boarding_screen_2 : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_boarding_screen2)

        val nextButton : Button = findViewById(R.id.nextButton2)
        val subtitleText : TextView = findViewById(R.id.subtitleText2)

        nextButton.setOnClickListener {
            val intent = Intent(this,boarding_screen_3::class.java)
            startActivity(intent)
        }
    }
}