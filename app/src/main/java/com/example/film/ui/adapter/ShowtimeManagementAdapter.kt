package com.example.film.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemShowtimeManagementBinding
import com.example.film.model.ShowtimeModel
import com.example.film.utils.Common
import com.google.android.material.chip.Chip

class ShowtimeManagementAdapter(
    showtimes: List<ShowtimeModel>,
    private val onEdit: (ShowtimeModel) -> Unit,
    private val onDelete: (ShowtimeModel) -> Unit
) : RecyclerView.Adapter<ShowtimeManagementAdapter.ViewHolder>() {

    private var groups: List<ShowtimeGroup> = groupShowtimes(showtimes)

    inner class ViewHolder(val binding: ItemShowtimeManagementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShowtimeManagementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.binding.apply {
            txtMovieName.text = group.movieName
            txtCinemaName.text = group.cinemaName
            txtDateTime.text = group.date
            txtBookedSeats.text = "${group.showtimes.size} suất • ${group.showtimes.sumOf { it.bookedSeats.size }} ghế đã đặt"
            renderShowtimeGrid(this, group.showtimes)
        }
    }

    override fun getItemCount(): Int = groups.size

    fun updateData(newShowtimes: List<ShowtimeModel>) {
        groups = groupShowtimes(newShowtimes)
        notifyDataSetChanged()
    }

    private fun renderShowtimeGrid(binding: ItemShowtimeManagementBinding, showtimes: List<ShowtimeModel>) {
        binding.gridShowtimes.removeAllViews()
        showtimes.sortedBy { showtimeSortIndex(it.time) }.forEach { showtime ->
            val chip = Chip(binding.root.context).apply {
                id = View.generateViewId()
                text = showtime.time
                gravity = Gravity.CENTER
                isCheckable = false
                isCloseIconVisible = true
                chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#334155"))
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#475569"))
                chipStrokeWidth = dp(binding, 1).toFloat()
                closeIconTint = ColorStateList.valueOf(Color.parseColor("#EF4444"))
                setTextColor(Color.WHITE)
                textSize = 12f
                setOnClickListener { onEdit(showtime) }
                setOnCloseIconClickListener { onDelete(showtime) }
            }
            chip.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(binding, 3), dp(binding, 3), dp(binding, 3), dp(binding, 7))
            }
            binding.gridShowtimes.addView(chip)
        }
    }

    private fun groupShowtimes(showtimes: List<ShowtimeModel>): List<ShowtimeGroup> {
        return showtimes
            .groupBy {
                ShowtimeGroupKey(
                    movieId = it.movieId,
                    movieName = it.movieName,
                    cinemaName = it.cinemaName,
                    date = it.date,
                    dateKey = it.dateKey
                )
            }
            .map { (key, groupShowtimes) ->
                ShowtimeGroup(
                    movieName = key.movieName,
                    cinemaName = key.cinemaName,
                    date = key.date,
                    dateKey = key.dateKey,
                    showtimes = groupShowtimes
                )
            }
            .sortedWith(
                compareBy<ShowtimeGroup> { it.dateKey }
                    .thenBy { it.cinemaName }
                    .thenBy { it.movieName }
            )
    }

    private fun showtimeSortIndex(time: String): Int {
        val index = Common.initEveningShowTimes().indexOf(time)
        return if (index >= 0) index else Int.MAX_VALUE
    }

    private fun dp(binding: ItemShowtimeManagementBinding, value: Int): Int {
        return (value * binding.root.resources.displayMetrics.density).toInt()
    }
}

private data class ShowtimeGroupKey(
    val movieId: Int,
    val movieName: String,
    val cinemaName: String,
    val date: String,
    val dateKey: String
)

private data class ShowtimeGroup(
    val movieName: String,
    val cinemaName: String,
    val date: String,
    val dateKey: String,
    val showtimes: List<ShowtimeModel>
)
