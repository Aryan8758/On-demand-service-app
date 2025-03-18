package com.example.foryou

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PendingRequestsAdapter(private val context: Context,private val providerList: List<AdminProviderModel>) :
    RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val provider = providerList[position]
        holder.name.text = provider.name
        holder.email.text = provider.email
        holder.service.text = provider.role

        holder.btnApprove.setOnClickListener {
            updateProviderStatus(provider.id, "approved")
            sendNotificationToProvider(provider.id, "Account Approved", "Your provider account has been approved!")
        }

        holder.btnReject.setOnClickListener {
            deleteProvider(provider.id)
            sendNotificationToProvider(provider.id, "Account Rejected", "Your provider account request was rejected.")
        }
    }

    override fun getItemCount(): Int {
        return providerList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.providerName)
        val email: TextView = itemView.findViewById(R.id.providerEmail)
        val service: TextView = itemView.findViewById(R.id.providerService)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    private fun updateProviderStatus(providerId: String, status: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("providers").document(providerId)
            .update("status", status)
            .addOnSuccessListener {
                Log.d("Provider", "Updated to $status")
            }
    }

    private fun deleteProvider(providerId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("providers").document(providerId)
            .delete()
            .addOnSuccessListener {
                Log.d("Provider", "Deleted")
            }
    }

    private fun sendNotificationToProvider(providerId: String, title: String, message: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("providers").document(providerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fcmToken = document.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            sendFCMNotification(fcmToken, title, message)
                        }
                    } else {
                        Log.e("FCM", "Provider FCM Token is Empty")
                    }
                } else {
                    Log.e("FCM", "Provider Not Found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to fetch provider data: ${e.message}")
            }
    }

    private suspend fun sendFCMNotification(providerToken: String, title: String, body: String) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/foryou-fa1d3/messages:send"

        val accessToken = TokenManager.getAccessToken(context) // Get Firebase Access Token
        if (accessToken == null) {
            Log.e("FCM", "Failed to generate access token")
            return
        }

        val jsonObject = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", providerToken)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
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
                Log.d("FCM", "Notification Response: ${response.body?.string()}")
            } catch (e: Exception) {
                Log.e("FCM", "Error sending notification: ${e.message}")
            }
        }
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
                    val expiresIn = googleCredentials.accessToken?.expirationTime?.time ?: 0L // ✅ Fixed

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
}