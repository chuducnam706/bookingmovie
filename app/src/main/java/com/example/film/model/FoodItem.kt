package com.example.film.model

data class FoodItem(
    val id: Int,
    val name: String,
    val price: Long,
    val imageUrl: String,
    var quantity: Int = 0
)
