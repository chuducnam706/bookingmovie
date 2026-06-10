package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemDateTimeBinding

class TimeAdapter(
    private val data: MutableList<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

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


    fun updateData(newData: List<String>) {
        if (data == newData && selectedPosition == 0) return

        data.clear()
        data.addAll(newData)
        selectedPosition = 0
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDateTimeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(date: String, position: Int) {

            binding.txtDate.text = date
            if (position == selectedPosition) {
                binding.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#3B82F6"))
                binding.txtDate.setTextColor(android.graphics.Color.WHITE)
            } else {
                binding.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#334155"))
                binding.txtDate.setTextColor(android.graphics.Color.WHITE)
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
