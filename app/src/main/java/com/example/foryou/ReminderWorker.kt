package com.example.foryou

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("WorkManager", "ReminderWorker Started")  // ✅ Check yeh log aarha hai ya nahi
        val customerTitle = "Your Booking is Near!"
        val customerToken = inputData.getString("customerToken") ?: return Result.failure()
        val providerTitle = "Your Service is Near!"
        val providerToken = inputData.getString("providerToken") ?: return Result.failure()

        val customerBodyMessage = "Your service provider will reach you in 30 minutes. Get ready!"
        val providerBodyMessage = "Reminder: You have an upcoming service in 30 minutes. Please be prepared!"
        CoroutineScope(Dispatchers.IO).launch {
            sendFCMNotification(applicationContext, customerToken, customerBodyMessage,customerTitle)
            sendFCMNotification(applicationContext, providerToken, providerBodyMessage,providerTitle)
        }

        return Result.success()
    }
    object TokenManager {
        private var cachedToken: String? = null
        private var tokenExpiryTime: Long = 0L

        suspend fun getAccessToken(context: Context): String? {
            val currentTime = System.currentTimeMillis()

            // ✅ Pehle se token hai aur expire nahi hua, to wahi return karo
            if (cachedToken != null && currentTime < tokenExpiryTime) {
                Log.d("FCM", "Using Cached Token: $cachedToken")
                return cachedToken
            }

            return withContext(Dispatchers.IO) {
                try {
                    val jsonStream = context.assets.open("service-account.json")
                    val googleCredentials = GoogleCredentials.fromStream(jsonStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                    googleCredentials.refreshIfExpired()
                    val newToken = googleCredentials.accessToken?.tokenValue
                    val expiresIn =
                        googleCredentials.accessToken?.expirationTime?.time ?: 0L // ✅ Fixed

                    if (newToken != null) {
                        cachedToken = newToken
                        tokenExpiryTime = expiresIn
                    }

                    Log.d("FCM", "Generated New Token: $newToken")
                    newToken
                } catch (e: IOException) {
                    Log.e("FCM", "Error getting access token: ${e.message}")
                    null
                }
            }
        }
    }

    suspend fun sendFCMNotification(context: Context, userToken: String, messageBody: String,title:String) {
        Log.d("FCM", "Sending Notification to: $userToken")  // ✅ Token Print karo

        val fcmUrl = "https://fcm.googleapis.com/v1/projects/foryou-fa1d3/messages:send"

        val accessToken = TokenManager.getAccessToken(context)
        if (accessToken == null) {
            Log.e("FCM", "Access Token Generation Failed")
            return
        }

        val jsonObject = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", userToken)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", messageBody)
                })
            })
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(fcmUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = OkHttpClient().newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("FCM", "Notification Response: $responseBody")  // ✅ Response Print karo
            } catch (e: IOException) {
                Log.e("FCM", "Error sending notification: ${e.message}")
            }
        }
    }

}
