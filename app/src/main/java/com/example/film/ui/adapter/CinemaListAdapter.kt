package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemCinemaListBinding

/**
 * Adapter danh sách rạp cho ChooseCinemaFragment
 * Item gồm: tên rạp + địa chỉ + click mở CinemaDetailActivity
 */
class CinemaListAdapter(
    private val data: List<Pair<String, String>>, // Pair(name, address)
    private val onClick: (name: String, address: String) -> Unit
) : RecyclerView.Adapter<CinemaListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCinemaListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, address) = data[position]
        holder.bind(name, address)
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val binding: ItemCinemaListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String, address: String) {
            binding.txtCinemaName.text = name
            binding.txtCinemaAddress.text = address

            binding.root.setOnClickListener {
                onClick(name, address)
            }
        }
    }
}
