package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.film.database.FilmDTO
import com.example.film.databinding.ItemFilmBinding

class ChooseFilmAdapter(
    private var data: List<FilmDTO>,
    private val onClickItem : (FilmDTO) -> Unit,
    private val onClickBooking : (FilmDTO) -> Unit
) : RecyclerView.Adapter<ChooseFilmAdapter.ViewHolder>() {

    private var ticketPrice: Long = 0

    fun setData(data: List<FilmDTO>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun setTicketPrice(ticketPrice: Long){
        this.ticketPrice = ticketPrice
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFilmBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bindView(data[position])
    }

    override fun getItemCount(): Int = data.size


    inner class ViewHolder(val binding: ItemFilmBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(item: FilmDTO) {
            binding.nameFilm.text = item.original_title

            Glide.with(binding.BannerFilm.context)
                .load("https://image.tmdb.org/t/p/w500${item.poster_path}")
                .centerCrop()
                .into(binding.BannerFilm)
            binding.countVote.text = "${item.vote_count} vote"
            binding.priceTicket.text = "${String.format("%,d", ticketPrice)}đ"

            binding.root.setOnClickListener { onClickItem(item) }
            binding.btnBooking.setOnClickListener { onClickBooking(item) }
        }


    }
}