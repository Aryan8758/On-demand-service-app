package com.example.foryou

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foryou.databinding.ActivityEmailVerifyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailVerify : AppCompatActivity() {
    private val binding: ActivityEmailVerifyBinding by lazy {
        ActivityEmailVerifyBinding.inflate(layoutInflater)
    }
    var otp=""
    var isOtpValid=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.otpEditText.visibility = View.INVISIBLE
        binding.Signinbtn.visibility = View.INVISIBLE
        binding.ResendOtp.visibility=View.INVISIBLE

        //otp send btn
        binding.otpsendbtn.setOnClickListener {
            //otp ganerate
            otp = (10000..99999).random().toString()
            val email = binding.emailEditText.text.toString().trim()
             if(isValidEmail(email) && email.isNotEmpty()){
                sendEmailInBackground(email, otp)
                binding.otpsendbtn.visibility = View.INVISIBLE
                binding.otpEditText.visibility = View.VISIBLE
                binding.Signinbtn.visibility = View.VISIBLE
                binding.ResendOtp.visibility = View.VISIBLE

                 // expire otp validation
                 isOtpValid=true
                 startOtpTimer()
            }
                else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
        //Resend otp Button
        binding.ResendOtp.setOnClickListener{
            otp=""
            //otp ganerate
            otp = (10000..99999).random().toString()
            isOtpValid=true
            startOtpTimer()
            val email = binding.emailEditText.text.toString().trim()
            sendEmailInBackground(email, otp)
            Toast.makeText(this, "Resend OTP!", Toast.LENGTH_SHORT).show()
        }

         //signing button navigate to SignupPage
        binding.Signinbtn.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (!isOtpValid) {
                Toast.makeText(
                    this,
                    "OTP has expired. Please request a new one.",
                    Toast.LENGTH_SHORT
                ).show()
            }
           else if (binding.otpEditText.text.toString() == otp) {
                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show()
                val intent=Intent(this,SignUp::class.java)
                intent.putExtra("Email",email)
                startActivity(intent)
                finish() // Navigate to the next activity
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }


    }


     //CoroutineScope background email send
    private fun sendEmailInBackground(email: String, otp: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = emailSender(email, otp)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@EmailVerify, "Email sent successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EmailVerify, "Failed to send email.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
 // email sender
    private fun emailSender(email: String, otp: String): Boolean {
        val username = "foryou9054@gmail.com" // Your Gmail
        val password = "mtfu ikyb zzra awsq" // App Password

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        return try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                subject = "Your OTP Code – Secure Verification"
                setContent(
                    """
                    <html>
                    <body>
                        <div style="background-color:#f7f7f7; padding:20px; font-family:Arial, sans-serif;">
                            <h2 style="color:#4CAF50;">Your OTP Code for Verification</h2>
                            <p style="font-size:16px;">Hello,</p>
                            <p style="font-size:16px;">
                                You have requested an OTP code to verify your email address. 
                                Your OTP code is <strong style="font-size:18px; color:#007BFF;">$otp</strong>.
                            </p>
                            <p style="font-size:16px;">
                                This OTP will expire in 2 minute. Please use it to complete your registration process.
                            </p>
                            <p style="font-size:16px;">
                                If you didn't request this, please ignore this email.
                            </p>
                            <p style="font-size:14px; color:#888888;">Regards,<br>The ForYou Team</p>
                        </div>
                    </body>
                    </html>
                """.trimIndent(), "text/html"
                )
            }

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun startOtpTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            isOtpValid = false // Mark OTP as expired
            Toast.makeText(this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show()
        }, 120000) // 2-minute delay
    }
    // email verification
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

