package com.example.film.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.res.ColorStateList
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemCinemaBinding

class CinemaAdapter(
    private val data: MutableList<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CinemaAdapter.ViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCinemaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(data[position], position)
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(val binding: ItemCinemaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(name: String, position: Int) {
            binding.txtCinema.text = name
            if (position == selectedPosition) {
                binding.cardContainer.setCardBackgroundColor(Color.parseColor("#1E293B"))
                binding.cardContainer.strokeColor = Color.parseColor("#3B82F6")
                binding.imgSelected.visibility = View.VISIBLE
                binding.imgCinema.imageTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))
            } else {
                binding.cardContainer.setCardBackgroundColor(Color.parseColor("#0F172A"))
                binding.cardContainer.strokeColor = Color.parseColor("#1E293B")
                binding.imgSelected.visibility = View.GONE
                binding.imgCinema.imageTintList = ColorStateList.valueOf(Color.parseColor("#475569"))
            }
            binding.root.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onClick(name)
            }
        }
    }
}
