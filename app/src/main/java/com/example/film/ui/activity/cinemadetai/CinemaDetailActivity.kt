package com.example.film.ui.activity.cinemadetai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.film.database.FilmDTO
import com.example.film.databinding.ActivityCinemaDetailBinding
import com.example.film.model.ShowtimeModel
import com.example.film.ui.activity.chooseseate.SeatCinemaActivity
import com.example.film.ui.adapter.CinemaMovieAdapter
import com.example.film.ui.adapter.DateAdapter
import com.example.film.ui.adapter.TimeAdapter
import com.example.film.utils.Common
import com.example.film.viewmodel.FilmViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val db = FirebaseFirestore.getInstance()
    private var showtimeListener: ListenerRegistration? = null
    private var allMovies: List<FilmDTO> = emptyList()
    private var availableShowtimes: List<ShowtimeModel> = emptyList()

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
        listenToShowtimes()

        // Load phim đang chiếu
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadMovies(0)
    }

    private fun setupDateList() {
        val dates = Common.initDate()
        selectedDate = dates[0]

        dateAdapter = DateAdapter(dates.toMutableList()) { date ->
            selectedDate = date
            updateMovieList()
        }
        binding.lstDate.adapter = dateAdapter
        binding.lstDate.itemAnimator = null
        binding.lstDate.setHasFixedSize(true)
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
        binding.lstTimeFilter.itemAnimator = null
        binding.lstTimeFilter.setHasFixedSize(true)
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
        binding.lstMovies.itemAnimator = null
        binding.lstMovies.setHasFixedSize(true)
    }

    private fun observeViewModel() {
        viewModel.movies.observe(this) { movies ->
            binding.progressBar.visibility = View.GONE
            allMovies = movies.orEmpty()
            updateMovieList()
        }

        viewModel.error.observe(this) { error ->
            binding.progressBar.visibility = View.GONE
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenToShowtimes() {
        showtimeListener = db.collection("showtimes")
            .whereEqualTo("cinemaName", cinemaName)
            .addSnapshotListener { snapshot, e ->
                binding.progressBar.visibility = View.GONE

                if (e != null) {
                    Toast.makeText(this, "Lỗi tải suất chiếu: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents.orEmpty()
                lifecycleScope.launch {
                    availableShowtimes = withContext(Dispatchers.Default) {
                        documents.mapNotNull { doc ->
                            doc.toObject(ShowtimeModel::class.java)?.apply {
                                id = doc.id
                                if (showKey.isBlank()) showKey = doc.id
                            }
                        }.filter { it.active }
                    }

                    updateMovieList()
                }
            }
    }

    private fun updateMovieList() {
        val showtimesForDate = availableShowtimes.filter {
            Common.isSameDateKey(it.dateKey, selectedDate)
        }

        val moviesById = allMovies.associateBy { it.id }
        val displayMovies = showtimesForDate
            .distinctBy { if (it.movieId != 0) "id_${it.movieId}" else "name_${it.movieName}" }
            .map { showtime ->
                moviesById[showtime.movieId] ?: FilmDTO(
                    id = showtime.movieId,
                    poster_path = showtime.moviePoster,
                    overview = null,
                    vote_count = 0,
                    original_title = showtime.movieName
                )
            }

        binding.lstMovies.visibility = if (displayMovies.isEmpty()) View.GONE else View.VISIBLE
        movieAdapter.updateData(displayMovies, selectedDate, showtimesForDate)
    }

    override fun onDestroy() {
        super.onDestroy()
        showtimeListener?.remove()
        showtimeListener = null
    }
}
