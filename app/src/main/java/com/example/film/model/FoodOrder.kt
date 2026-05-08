package com.example.film.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class FoodOrder(
    val orderId: String = "",
    val userId: String = "",
    val cinemaName: String = "",
    val items: List<FoodOrderItem> = listOf(),
    val totalPrice: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class FoodOrderItem(
    val name: String = "",
    val price: Long = 0,
    val quantity: Int = 0
) : Serializable
