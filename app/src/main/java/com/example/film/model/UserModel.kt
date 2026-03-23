package com.example.film.model

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
