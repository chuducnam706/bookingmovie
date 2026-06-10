package com.example.film.utils

import android.util.Log
import com.example.film.database.MyService
import com.example.film.database.VnPayService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.IOException
import okio.buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val BASE_URL_VN_PAY = "https://vn-pay-a6i2.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    private val safeBodyInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val body = response.body ?: return@Interceptor response

        val safeSource = object : ForwardingSource(body.source()) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                return try {
                    super.read(sink, byteCount)
                } catch (e: IOException) {
                    val message = e.message.orEmpty()

                    if (
                        message.contains("gzip", ignoreCase = true) ||
                        message.contains("exhausted", ignoreCase = true) ||
                        message.contains("unexpected end of stream", ignoreCase = true)
                    ) {
                        Log.e(
                            "SAFE_GZIP",
                            "Gzip response bị lỗi ở cuối stream, ép kết thúc body",
                            e
                        )
                        -1L
                    } else {
                        throw e
                    }
                }
            }
        }.buffer()

        val safeBody = safeSource.asResponseBody(
            body.contentType(),
            -1L
        )

        response.newBuilder()
            .body(safeBody)
            .build()
    }

    private val movieOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(safeBodyInterceptor)
        .addInterceptor(logging)
        .build()

    private val vnPayOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(logging)
        .build()

    val retrofit: MyService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(movieOkHttpClient)
        .build()
        .create(MyService::class.java)

    val retrofitVnPay: VnPayService = Retrofit.Builder()
        .baseUrl(BASE_URL_VN_PAY)
        .addConverterFactory(GsonConverterFactory.create())
        .client(vnPayOkHttpClient)
        .build()
        .create(VnPayService::class.java)
}
