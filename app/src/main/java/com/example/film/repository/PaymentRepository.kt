package com.example.film.repository

import com.example.film.model.VnPayModel
import com.example.film.utils.RetrofitClient

class PaymentRepository {

    suspend fun createPayment(
        amount: Long,
        orderInfo: String,
        orderId : String
    ): VnPayModel {

        return RetrofitClient.retrofitVnPay.createPayment(amount, orderInfo, orderId)

    }



}