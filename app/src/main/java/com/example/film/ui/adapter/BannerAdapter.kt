package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.film.database.FilmDTO
import com.example.film.databinding.ItemBannerBinding

class BannerAdapter(
    private var data: List<FilmDTO>,
    private val onClickItem: (FilmDTO) -> Unit
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    fun setData(newData: List<FilmDTO>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FilmDTO) {
            binding.tvTitle.text = item.original_title
            Glide.with(binding.imgBanner.context)
                .load("https://image.tmdb.org/t/p/w780${item.poster_path}")
                .centerCrop()
                .into(binding.imgBanner)

            binding.root.setOnClickListener { onClickItem(item) }
        }
    }
}
