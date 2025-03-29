package com.example.foryou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class boarding_screen_1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_boarding_screen1)
        val nextButton : Button = findViewById(R.id.nextButton)
        val subtitleText : TextView = findViewById(R.id.subtitleText)

        nextButton.setOnClickListener {
            val intent = Intent(this,boarding_screen_2::class.java)
            startActivity(intent)
        }

    }
}