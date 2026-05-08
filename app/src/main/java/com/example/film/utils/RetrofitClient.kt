package com.example.film.utils

import com.example.film.database.MyService
import com.example.film.database.VnPayService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val BASE_URL_VN_PAY = "https://vn-pay-a6i2.onrender.com/"

    val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    val okHttp = OkHttpClient.Builder().apply {
        addInterceptor(logging)
    }

    val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(
            GsonConverterFactory.create()
        ).client(okHttp.build())
        .build()
        .create(MyService::class.java)

    val retrofitVnPay = Retrofit.Builder().baseUrl(BASE_URL_VN_PAY)
        .addConverterFactory(GsonConverterFactory.create()).client(okHttp.build())
        .build()
        .create(VnPayService::class.java)
}