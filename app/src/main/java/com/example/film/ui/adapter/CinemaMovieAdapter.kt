package com.example.film.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.film.database.FilmDTO
import com.example.film.databinding.ItemCinemaMovieBinding
import com.example.film.utils.Common
import com.google.android.material.chip.Chip

/**
 * Adapter hiển thị phim đang chiếu tại 1 rạp cụ thể
 * Mỗi item: poster + tên phim + genre + runtime + rating + ChipGroup suất chiếu
 *
 * Hỗ trợ filter suất chiếu theo khung giờ:
 *   - "Tất cả": hiển thị tất cả suất
 *   - "09:00 - 12:00": chỉ hiển thị suất bắt đầu từ 09h đến 11h
 *   - "12:00 - 15:00": suất bắt đầu từ 12h đến 14h
 *   - "15:00 - 18:00": suất bắt đầu từ 15h đến 17h
 *   - "18:00+": suất bắt đầu từ 18h trở đi
 *
 * Nếu sau khi lọc 1 phim không còn suất nào → ẩn card phim đó (GONE)
 */
class CinemaMovieAdapter(
    private var movies: List<FilmDTO>,
    private var selectedDate: String,
    private var timeFilter: String = "Tất cả",
    private val onShowtimeClick: (film: FilmDTO, time: String) -> Unit
) : RecyclerView.Adapter<CinemaMovieAdapter.ViewHolder>() {

    fun updateData(newMovies: List<FilmDTO>, date: String) {
        movies = newMovies
        selectedDate = date
        notifyDataSetChanged()
    }

    fun updateDate(date: String) {
        selectedDate = date
        notifyDataSetChanged()
    }

    /** Cập nhật filter khung giờ → re-render chip suất chiếu */
    fun updateTimeFilter(filter: String) {
        timeFilter = filter
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCinemaMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(movies[position])

    override fun getItemCount() = movies.size

    inner class ViewHolder(private val binding: ItemCinemaMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(film: FilmDTO) {
            binding.txtMovieName.text = film.original_title ?: "Unknown"
            binding.txtGenres.text = film.genres?.joinToString(", ") { it.name } ?: ""
            binding.txtFormat.text = "2D - Phụ đề"

            // Runtime
            val runtime = film.runtime ?: 0
            if (runtime > 0) {
                binding.txtRuntime.text = "⏱ ${runtime / 60} giờ ${runtime % 60} phút"
            } else {
                binding.txtRuntime.text = ""
            }

            // Rating
            binding.txtRating.text = "⭐ ${film.vote_count ?: 0} votes"

            // Poster
            if (!film.poster_path.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w300${film.poster_path}")
                    .centerCrop()
                    .into(binding.imgPoster)
            } else {
                Glide.with(binding.root.context).clear(binding.imgPoster)
            }

            // ──────────────────────────────────────────────────────────
            // Generate suất chiếu + lọc theo timeFilter
            // ──────────────────────────────────────────────────────────
            binding.chipGroupShowtimes.removeAllViews()

            val dates = Common.initDate()
            val datePosition = dates.indexOf(selectedDate)
            val isToday = datePosition == 0 || datePosition == -1

            // Tạo suất chiếu dựa trên film.id để mỗi phim có giờ khác nhau
            val startHour = (film.id % 4) + 9  // 9h - 12h
            val endHour = 24
            val step = 2

            val allTimes = Common.generateShowTimes(isToday, startHour, endHour, step)

            // Lọc suất chiếu theo khung giờ đã chọn
            val filteredTimes = filterShowtimes(allTimes, timeFilter)

            // Nếu phim không có suất nào phù hợp filter → ẩn card
            if (filteredTimes.isEmpty()) {
                binding.root.visibility = View.GONE
                binding.root.layoutParams = RecyclerView.LayoutParams(0, 0)
                return
            } else {
                binding.root.visibility = View.VISIBLE
                binding.root.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            for (time in filteredTimes) {
                val chip = Chip(binding.root.context).apply {
                    text = time
                    isClickable = true
                    isCheckable = false
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#334155"))
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    chipCornerRadius = 20f
                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#475569"))
                    chipStrokeWidth = 1f

                    setOnClickListener {
                        onShowtimeClick(film, time)
                    }
                }
                binding.chipGroupShowtimes.addView(chip)
            }
        }
    }

    /**
     * Lọc suất chiếu theo khung giờ.
     *
     * Mỗi suất có format "HH:00 - HH:00".
     * Parse giờ bắt đầu rồi so sánh với khoảng filter:
     *   "Tất cả"         → giữ hết
     *   "09:00 - 12:00"  → startHour in [9, 12)
     *   "12:00 - 15:00"  → startHour in [12, 15)
     *   "15:00 - 18:00"  → startHour in [15, 18)
     *   "18:00+"         → startHour >= 18
     */
    private fun filterShowtimes(times: List<String>, filter: String): List<String> {
        if (filter == "Tất cả") return times

        val range = parseFilterRange(filter) ?: return times

        return times.filter { time ->
            val startHour = parseStartHour(time)
            if (startHour == null) {
                true // Không parse được → giữ lại
            } else {
                startHour >= range.first && (range.second == null || startHour < range.second!!)
            }
        }
    }

    /**
     * Parse filter string thành Pair(startHour, endHour?)
     * endHour = null nghĩa là "trở đi" (vd: "18:00+")
     */
    private fun parseFilterRange(filter: String): Pair<Int, Int?>? {
        return when {
            filter.contains("+") -> {
                // "18:00+" → 18 trở đi
                val hour = filter.substringBefore(":").trim().toIntOrNull() ?: return null
                Pair(hour, null)
            }
            filter.contains(" - ") -> {
                // "09:00 - 12:00" → [9, 12)
                val parts = filter.split(" - ")
                val from = parts[0].substringBefore(":").trim().toIntOrNull() ?: return null
                val to = parts[1].substringBefore(":").trim().toIntOrNull() ?: return null
                Pair(from, to)
            }
            else -> null
        }
    }

    /**
     * Parse giờ bắt đầu từ chuỗi suất chiếu "HH:00 - HH:00"
     * Trả về giờ (Int) hoặc null nếu parse thất bại
     */
    private fun parseStartHour(showtime: String): Int? {
        val startPart = showtime.split(" - ").firstOrNull() ?: return null
        return startPart.substringBefore(":").trim().toIntOrNull()
    }
}
