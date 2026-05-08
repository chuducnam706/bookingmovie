package com.example.film.ui.activity.cinemadetai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.film.databinding.ActivityCinemaDetailBinding
import com.example.film.ui.activity.chooseseate.SeatCinemaActivity
import com.example.film.ui.adapter.CinemaMovieAdapter
import com.example.film.ui.adapter.DateAdapter
import com.example.film.ui.adapter.TimeAdapter
import com.example.film.utils.Common
import com.example.film.viewmodel.FilmViewModel

class CinemaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCinemaDetailBinding
    private lateinit var viewModel: FilmViewModel
    private lateinit var movieAdapter: CinemaMovieAdapter
    private lateinit var dateAdapter: DateAdapter
    private lateinit var timeFilterAdapter: TimeAdapter

    private var cinemaName = ""
    private var cinemaAddress = ""
    private var selectedDate = ""
    private var selectedTimeFilter = "Tất cả"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCinemaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cinemaName = intent.getStringExtra("cinema_name") ?: ""
        cinemaAddress = intent.getStringExtra("cinema_address") ?: ""

        binding.toolbar.title = cinemaName
        binding.toolbar.setNavigationOnClickListener { finish() }


        viewModel = ViewModelProvider(this)[FilmViewModel::class.java]

        setupDateList()
        setupTimeFilter()
        setupMovieList()
        observeViewModel()

        // Load phim đang chiếu
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadMovies(0)
    }

    private fun setupDateList() {
        val dates = Common.initDate()
        selectedDate = dates[0]

        dateAdapter = DateAdapter(dates.toMutableList()) { date ->
            selectedDate = date
            movieAdapter.updateDate(date)
        }
        binding.lstDate.adapter = dateAdapter
    }

    private fun setupTimeFilter() {
        val filters = mutableListOf("Tất cả", "09:00 - 12:00", "12:00 - 15:00", "15:00 - 18:00", "18:00+")
        selectedTimeFilter = filters[0]

        timeFilterAdapter = TimeAdapter(filters) { filter ->
            selectedTimeFilter = filter
            // Lọc suất chiếu thực tế trong adapter
            movieAdapter.updateTimeFilter(filter)
        }
        binding.lstTimeFilter.adapter = timeFilterAdapter
    }

    private fun setupMovieList() {
        movieAdapter = CinemaMovieAdapter(
            movies = emptyList(),
            selectedDate = selectedDate
        ) { film, time ->
            // Navigate sang SeatCinemaActivity để chọn ghế
            val intent = Intent(this, SeatCinemaActivity::class.java)
            intent.putExtra("movie_name", film.original_title ?: "")
            intent.putExtra("movie_poster", film.poster_path ?: "")
            intent.putExtra("selected_cinema", cinemaName)
            intent.putExtra("selected_date", selectedDate)
            intent.putExtra("selected_time", time)
            startActivity(intent)
        }
        binding.lstMovies.adapter = movieAdapter
    }

    private fun observeViewModel() {
        viewModel.movies.observe(this) { movies ->
            binding.progressBar.visibility = View.GONE

            if (movies.isNullOrEmpty()) {
                binding.lstMovies.visibility = View.GONE
            } else {
                binding.lstMovies.visibility = View.VISIBLE
                movieAdapter.updateData(movies, selectedDate)
            }
        }

        viewModel.error.observe(this) { error ->
            binding.progressBar.visibility = View.GONE
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}