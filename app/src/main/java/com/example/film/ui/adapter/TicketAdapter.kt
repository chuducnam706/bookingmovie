package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.film.databinding.ItemTicketBinding
import com.example.film.model.BookingModel

class TicketAdapter(
    private val data: List<BookingModel>
) : RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTicketBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(booking: BookingModel) {
            binding.txtMovieName.text = booking.movieName.ifEmpty { "Unknown Movie" }
            binding.txtCinemaName.text = booking.cinemaName
            binding.txtDate.text = booking.date
            binding.txtTime.text = booking.time

            // Load movie poster
            if (booking.moviePoster.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w300${booking.moviePoster}")
                    .centerCrop()
                    .into(binding.imgPoster)
            }

            // Click to open Detail screen
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = android.content.Intent(context, com.example.film.ui.activity.DetailTicketActivity::class.java)
                intent.putExtra("BOOKING_DATA", booking)
                context.startActivity(intent)
            }
        }
    }
}
