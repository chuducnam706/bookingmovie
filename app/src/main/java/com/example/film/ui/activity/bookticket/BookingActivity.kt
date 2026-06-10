package com.example.film.ui.activity.bookticket

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.film.base.BaseActivity
import com.example.film.databinding.ActivityBookingBinding
import com.example.film.model.ShowtimeModel
import com.example.film.ui.activity.chooseseate.SeatCinemaActivity
import com.example.film.ui.adapter.CinemaAdapter
import com.example.film.ui.adapter.DateAdapter
import com.example.film.ui.adapter.TimeAdapter
import com.example.film.utils.Common
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class BookingActivity : BaseActivity<ActivityBookingBinding>(ActivityBookingBinding::inflate) {

    private lateinit var adapterDate: DateAdapter
    private lateinit var adapterTime: TimeAdapter
    private lateinit var adapterCinema: CinemaAdapter
    private val db = FirebaseFirestore.getInstance()
    private var showtimeListener: ListenerRegistration? = null
    private var availableShowtimes: List<ShowtimeModel> = emptyList()
    private var showtimeTimesByDateCinema: Map<String, Map<String, List<String>>> = emptyMap()
    private var selectedCinema: String = ""
    private var selectedDate: String = ""
    private var movieName: String = ""
    private var moviePoster: String = ""

    override fun initializeComponent() {
        super.initializeComponent()

        val dates = Common.initDate()
        selectedDate = dates[0]
        adapterDate = DateAdapter(dates.toMutableList()) { date ->
            selectedDate = date
            refreshCinemaAndTimeOptions()
        }
        binding.lstDate.adapter = adapterDate
        binding.lstDate.itemAnimator = null
        binding.lstDate.setHasFixedSize(true)

        adapterCinema = CinemaAdapter(mutableListOf()) { cinema ->
            selectedCinema = cinema
            refreshTimeOptions()

            binding.root.post {
                val scrollY = binding.lblTime.top
                binding.nestedScrollView.smoothScrollTo(0, scrollY)
            }
        }
        binding.lstCinema.adapter = adapterCinema
        binding.lstCinema.itemAnimator = null
        binding.lstCinema.setHasFixedSize(true)

        movieName = intent.getStringExtra("movie_name") ?: ""
        moviePoster = intent.getStringExtra("movie_poster") ?: ""

        binding.tvMovieTitle.text = movieName
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780$moviePoster")
            .centerCrop()
            .into(binding.imgPoster)

        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780$moviePoster")
            .centerCrop()
            .into(binding.imgBackdrop)

        binding.btnBack.setOnClickListener { finish() }

        adapterTime = TimeAdapter(mutableListOf()) { time ->
            val intent = Intent(this, SeatCinemaActivity::class.java)
            intent.putExtra("movie_name", movieName)
            intent.putExtra("movie_poster", moviePoster)
            intent.putExtra("selected_cinema", selectedCinema)
            intent.putExtra("selected_date", selectedDate)
            intent.putExtra("selected_time", time)
            startActivity(intent)
        }
        binding.lstTime.adapter = adapterTime
        binding.lstTime.itemAnimator = null
        binding.lstTime.setHasFixedSize(true)

        listenToShowtimes()
    }

    private fun listenToShowtimes() {
        if (movieName.isBlank()) {
            refreshCinemaAndTimeOptions()
            return
        }

        showtimeListener = db.collection("showtimes")
            .whereEqualTo("movieName", movieName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Lỗi tải suất chiếu: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents.orEmpty()
                lifecycleScope.launch {
                    val parsedShowtimes = withContext(Dispatchers.Default) {
                        documents.mapNotNull { doc ->
                            doc.toObject(ShowtimeModel::class.java)?.apply {
                                id = doc.id
                                if (showKey.isBlank()) showKey = doc.id
                            }
                        }.filter { it.active && !it.isExpired() }
                    }

                    availableShowtimes = parsedShowtimes
                    showtimeTimesByDateCinema = buildShowtimeIndex(parsedShowtimes)
                    refreshCinemaAndTimeOptions()
                }
            }
    }

    private fun refreshCinemaAndTimeOptions() {
        val cinemas = Common.initCinema()

        if (cinemas.isEmpty()) {
            selectedCinema = ""
            adapterCinema.updateData(emptyList())
            adapterTime.updateData(emptyList())
            binding.lblCinema.visibility = View.GONE
            binding.lstCinema.visibility = View.GONE
            binding.lblTime.visibility = View.GONE
            binding.lstTime.visibility = View.GONE
            return
        }

        if (!cinemas.contains(selectedCinema)) {
            selectedCinema = cinemas.first()
        }

        adapterCinema.updateData(cinemas, selectedCinema)
        binding.lblCinema.visibility = View.VISIBLE
        binding.lstCinema.visibility = View.VISIBLE
        refreshTimeOptions()
    }

    private fun refreshTimeOptions() {
        val times = showtimeTimesByDateCinema[Common.extractDateKey(selectedDate)]
            ?.get(selectedCinema)
            .orEmpty()

        adapterTime.updateData(times)
        binding.lblTime.visibility = if (times.isEmpty()) View.GONE else View.VISIBLE
        binding.lstTime.visibility = if (times.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun buildShowtimeIndex(showtimes: List<ShowtimeModel>): Map<String, Map<String, List<String>>> {
        return showtimes
            .filter { it.cinemaName.isNotBlank() && it.time.isNotBlank() }
            .groupBy { showtimeDateKey(it) }
            .mapValues { (_, dayShowtimes) ->
                dayShowtimes
                    .groupBy { it.cinemaName }
                    .mapValues { (_, cinemaShowtimes) ->
                        cinemaShowtimes
                            .map { it.time }
                            .distinct()
                            .sortedWith(compareBy { parseStartHour(it) ?: Int.MAX_VALUE })
                    }
            }
    }

    private fun showtimeDateKey(showtime: ShowtimeModel): String {
        return Common.extractDateKey(showtime.dateKey.ifBlank { showtime.date })
    }

    private fun parseStartHour(showtime: String): Int? {
        return showtime.substringBefore(" - ").substringBefore(":").trim().toIntOrNull()
    }

    private fun ShowtimeModel.isExpired(): Boolean {
        val endMillis = getShowtimeEndMillis(dateKey, time) ?: return false
        return endMillis <= System.currentTimeMillis()
    }

    private fun getShowtimeEndMillis(dateKey: String, showtime: String): Long? {
        val hasExplicitYear = Regex("""\d{4}-\d{2}-\d{2}""").matches(Common.legacyDateKey(dateKey))
        val normalizedDateKey = Common.extractDateKey(dateKey)
        val dateParts = normalizedDateKey.split("-")
        if (dateParts.size != 3) return null

        val year = dateParts[0].toIntOrNull() ?: return null
        val month = dateParts[1].toIntOrNull() ?: return null
        val day = dateParts[2].toIntOrNull() ?: return null
        val startMinutes = parseTimeMinutes(showtime.substringBefore(" - ")) ?: return null
        val endMinutes = parseTimeMinutes(showtime.substringAfter(" - ", "")) ?: return null

        val now = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, startMinutes / 60)
            set(Calendar.MINUTE, startMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!hasExplicitYear && startCalendar.before(startOfToday(now))) {
            startCalendar.add(Calendar.YEAR, 1)
        }

        return (startCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, endMinutes / 60)
            set(Calendar.MINUTE, endMinutes % 60)
            if (endMinutes <= startMinutes) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }.timeInMillis
    }

    private fun parseTimeMinutes(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null

        return hour * 60 + minute
    }

    private fun startOfToday(now: Calendar): Calendar {
        return (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showtimeListener?.remove()
        showtimeListener = null
    }
}
