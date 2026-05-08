package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.film.databinding.ItemTicketBinding
import com.example.film.model.BookingModel
import com.example.film.ui.activity.detail.DetailTicketActivity

class TicketAdapter : RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookingModel>() {
            override fun areItemsTheSame(oldItem: BookingModel, newItem: BookingModel): Boolean {
                return oldItem.bookingId == newItem.bookingId
            }

            override fun areContentsTheSame(oldItem: BookingModel, newItem: BookingModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    fun submitList(newList: List<BookingModel>) {
        differ.submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class ViewHolder(val binding: ItemTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(booking: BookingModel) {
            binding.txtMovieName.text = booking.movieName.ifEmpty { "Unknown Movie" }
            binding.txtCinemaName.text = booking.cinemaName
            binding.txtDate.text = booking.date
            binding.txtTime.text = booking.time

            // FIX: Clear ảnh cũ khi poster trống — ViewHolder bị recycle sẽ giữ ảnh cũ
            // nếu không clear → item không có poster sẽ hiển thị poster của item trước đó
            if (booking.moviePoster.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w300${booking.moviePoster}")
                    .centerCrop()
                    .into(binding.imgPoster)
            } else {
                Glide.with(binding.root.context).clear(binding.imgPoster)
            }

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = android.content.Intent(
                    context,
                    DetailTicketActivity::class.java
                )
                intent.putExtra("BOOKING_DATA", booking)
                context.startActivity(intent)
            }
        }
    }
}
