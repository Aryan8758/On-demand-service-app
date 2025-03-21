package com.example.foryou

data class StatusModelClass(
    var id: String = "",
    var CustomerId: String = "",
    var ProviderId: String = "",
    val customerName: String = "",
    val service: String = "",
    val bookingDate: String = "",
    val timeSlot: String = "",
    val status: String = ""
)
