package com.example.film.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.film.database.FilmDTO
import com.example.film.repository.FilmRepository
import kotlinx.coroutines.launch

class FilmViewModel : ViewModel() {
    private val repository = FilmRepository()

    private var currentCategory: Int = 0

    private val _movies = MutableLiveData<List<FilmDTO>>()
    val movies: LiveData<List<FilmDTO>> = _movies

    private val _hotMovies = MutableLiveData<List<FilmDTO>>()
    val hotMovies: LiveData<List<FilmDTO>> = _hotMovies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _movieDetails = MutableLiveData<FilmDTO>()
    val movieDetails: LiveData<FilmDTO> = _movieDetails

    fun loadMovies(category: Int) {
        currentCategory = category
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = when (category) {
                    0 -> repository.getNowPlayingMovies(1)
                    1 -> repository.getUpcomingMovies(1)
                    2 -> repository.getPopularMovies(1)
                    else -> emptyList()
                }
                _movies.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHotMovies() {
        viewModelScope.launch {
            try {
                val result = repository.getPopularMovies(1)
                _hotMovies.value = result.take(5)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading hot movies"
            }
        }
    }

    fun searchMovies(query: String) {
        if (query.isEmpty()) {
            loadMovies(currentCategory)
            return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.searchMovies(query, 1)
                _movies.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMovieDetails(movieId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getMovieDetails(movieId)
                _movieDetails.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading movie details"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
