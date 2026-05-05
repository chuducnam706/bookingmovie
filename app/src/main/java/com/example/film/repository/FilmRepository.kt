package com.example.film.repository

import com.example.film.RetrofitClient
import com.example.film.database.FilmDTO
import com.example.film.database.ViewPageDTO

class FilmRepository {
    private val API_KEY = "5a6545a40e13bd2dc08a0792ba8a1ba9"

    suspend fun getNowPlayingMovies(page: Int): List<FilmDTO> {
        return RetrofitClient.retrofit.getNowPlayingMovies(API_KEY, page).results
    }

    suspend fun getUpcomingMovies(page: Int): List<FilmDTO> {
        return RetrofitClient.retrofit.getUpcomingMovies(API_KEY, page).results
    }

    suspend fun getPopularMovies(page: Int): List<FilmDTO> {
        return RetrofitClient.retrofit.getPopularMovies(API_KEY, page).results
    }

    suspend fun getMovieDetails(movieId: Int): FilmDTO {
        return RetrofitClient.retrofit.getMovieDetails(movieId, API_KEY)
    }
}
