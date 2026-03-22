package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemSeatViewBinding

class SeatAdapter : RecyclerView.Adapter<SeatAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        val binding = ItemSeatViewBinding.inflate(inflater, p0, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        p0: ViewHolder,
        p1: Int
    ) {

    }

    override fun getItemCount(): Int = 30


    inner class ViewHolder(val binding : ItemSeatViewBinding) : RecyclerView.ViewHolder(binding.root){

    }
}