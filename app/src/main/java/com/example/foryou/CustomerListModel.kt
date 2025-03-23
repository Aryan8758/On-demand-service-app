package com.example.foryou

data class CustomerListModel(
    val id:String="",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val serviceName: String ="",
    val photoUrl: String ?
)
