package com.example.film.model

data class VnPayModel(
    val status : String,
    val orderId : String,
    val amount : Long,
    val orderInfo : String,
    val paymentUrl : String


)