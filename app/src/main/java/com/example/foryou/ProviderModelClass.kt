package com.example.foryou

data class ProviderModelClass(
    val providerId:String,
    val name: String = "",
    val service: String = "",
    val image: String ?,
    val bg: Int
//    val experience: String = "",  // Firestore stores experience as a String
//    val number: String = "",      // Changed 'contact' to 'number' to match Firestore
//    val city: String = "",        // Added 'city' field to match Firestore data
)
