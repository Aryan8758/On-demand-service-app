package com.example.foryou

//data class Booking_model(
//    val ProviderId: String = "",
//    val CustomerId: String = "",
//    val customerName: String = "",
//    var bookingId: String = "",
//    val bookingDate: String = "",
//    val paymentMethod: String = "",
//    val PricerRate: String = "",
//    val service: String = "",
//    var status: String = "",
//    val timeSlot: String = "",
//)
data class Booking_model(
    var bookingId: String = "",
    val CustomerId: String = "",
    val ProviderId: String = "",
    val customerName: String = "",
    val service: String = "",
    val bookingDate: String = "",
    val timeSlot: String = "",
    var status: String = "",
    val paymentMethod: String = "",
    val PricerRate: String = "",
)
