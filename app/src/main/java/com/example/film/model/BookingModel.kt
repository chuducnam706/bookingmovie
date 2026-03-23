package com.example.film.model

import com.google.firebase.firestore.IgnoreExtraProperties


@IgnoreExtraProperties
data class BookingModel(
    var bookingId: String = "",
    var userId: String = "",
    var movieName: String = "",
    var moviePoster: String = "",
    var cinemaName: String = "",
    var date: String = "",
    var time: String = "",
    var showKey: String = "",
    var seats: List<String> = listOf(),
    var totalPrice: Long = 0,
    var timestamp: Long = System.currentTimeMillis()
)

