package com.example.film.database

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MyService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): ViewPageDTO

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): ViewPageDTO

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): ViewPageDTO

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "videos,credits"
    ): FilmDTO

}