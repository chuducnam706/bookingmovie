package com.example.film.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.film.RetrofitClient
import com.example.film.database.FilmDTO
import com.example.film.databinding.ActivityDetailBinding
import com.example.moneymanagement.presentation.view.base.BaseActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailActivity : BaseActivity<ActivityDetailBinding>(ActivityDetailBinding::inflate) {

    private val API_KEY = "5a6545a40e13bd2dc08a0792ba8a1ba9"
    private var data: FilmDTO? = null


    override fun initializeComponent() {
        super.initializeComponent()
        val json = intent.getStringExtra("FILM")
        if (json != null) {
            data = Gson().fromJson(json, FilmDTO::class.java)
        }

        data?.let { movie ->
            // Initial UI update with available data
            updateBasicUI(movie)

            // Fetch extra details
            fetchMovieDetails(movie.id)
        }
    }

    override fun initializeEvents() {
        super.initializeEvents()
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnBuyTicket.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("movie_name", data?.original_title)
            intent.putExtra("movie_poster", data?.poster_path)
            startActivity(intent)
        }

    }

    private fun updateBasicUI(movie: FilmDTO) {
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780${movie.poster_path}") // Using higher res for premium look
            .centerCrop()
            .into(binding.imaFilm)

        binding.tvMovieTitle.text = movie.original_title
        binding.overview.text = movie.overview
        binding.tvReleaseDate.text = movie.release_date ?: "N/A"
    }

    private fun fetchMovieDetails(movieId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fullDetails = RetrofitClient.retrofit.getMovieDetails(movieId, API_KEY)
                withContext(Dispatchers.Main) {
                    bindFullDetails(fullDetails)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bindFullDetails(movie: FilmDTO) {
        binding.tvDuration.text = "${movie.runtime ?: 0} mins"

        val genres = movie.genres?.joinToString(", ") { it.name } ?: "N/A"
        binding.tvGenres.text = genres

        val director = movie.credits?.crew?.find { it.job == "Director" }?.name ?: "N/A"
        binding.tvDirector.text = getString(com.example.film.R.string.label_director) + director

        val country = movie.production_countries?.joinToString(", ") { it.name } ?: "N/A"
        binding.tvCountry.text = getString(com.example.film.R.string.label_country) + country

        val cast = movie.credits?.cast?.take(5)?.joinToString(", ") { it.name } ?: "N/A"
        binding.tvCast.text = "Cast: $cast"

        // Trailer logic
        val trailer = movie.videos?.results?.find { it.site == "YouTube" && it.type == "Trailer" }
        if (trailer != null) {
            binding.btnTrailer.visibility = View.VISIBLE
            binding.btnTrailer.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${trailer.key}"))
                startActivity(intent)
            }
        } else {
            binding.btnTrailer.visibility = View.GONE
        }
    }

}
