package com.example.foryou

data class ProviderBookingReceiverModel(
    val ProviderId:String="",
    var bookingId: String = "",
   var CustomerId:String="",
    val timeSlot:String="",
    val customerName: String = "",
    val service: String = "",
    val bookingDate: String = "",
    val status: String = ""
)
