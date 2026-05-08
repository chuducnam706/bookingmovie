package com.example.film.database

import com.example.film.model.VnPayModel
import retrofit2.http.GET
import retrofit2.http.Query

interface VnPayService {

    @GET("api/payment/create-url")
    suspend fun createPayment(

        @Query("amount")
        amount: Long,

        @Query("orderInfo")
        orderInfo: String,

        @Query("orderId")
        orderId : String

    ): VnPayModel


}