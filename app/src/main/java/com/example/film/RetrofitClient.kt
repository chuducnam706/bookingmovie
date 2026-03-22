package com.example.film

import com.example.film.database.MyService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    private const val BASE_URL = "https://api.themoviedb.org/3/"

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


}