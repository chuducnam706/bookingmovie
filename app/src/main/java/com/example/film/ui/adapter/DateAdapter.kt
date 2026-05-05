package com.example.film.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemDateTimeBinding

class DateAdapter(
    private val data: MutableList<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDateTimeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindView(data[position], position)
    }

    override fun getItemCount(): Int = data.size


    inner class ViewHolder(val binding: ItemDateTimeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(date: String, position: Int) {

            binding.txtDate.text = date
            if (position == selectedPosition) {
                binding.cardContainer.setCardBackgroundColor(Color.parseColor("#3B82F6"))
                binding.txtDate.setTextColor(Color.WHITE)
            } else {
                binding.cardContainer.setCardBackgroundColor(Color.parseColor("#334155"))
                binding.txtDate.setTextColor(Color.WHITE)
            }
            binding.root.setOnClickListener {
                onClickItem(date, position)
            }
        }


        fun onClickItem(item: String, position: Int) {
            val oldPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)

            onClick(item)
        }

    }
}