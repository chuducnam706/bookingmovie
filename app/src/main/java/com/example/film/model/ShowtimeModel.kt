package com.example.film.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ShowtimeModel(
    var id: String = "",
    var showKey: String = "",
    var movieId: Int = 0,
    var movieName: String = "",
    var moviePoster: String = "",
    var cinemaName: String = "",
    var date: String = "",
    var dateKey: String = "",
    var time: String = "",
    var active: Boolean = true,
    var bookedSeats: List<String> = listOf(),
    var createdAt: Long = System.currentTimeMillis()
)
