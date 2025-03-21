package com.example.foryou

data class Booking_model (
    val ProviderId: String = "",
    val CustomerId: String = "",
    var bookingId: String = "",
    val bookingDate: String = "",
    val paymentMethod: String = "",
    val PricerRate:String="",
    val service: String = "",
    var status: String = "",
    val timeSlot: String = "",
)