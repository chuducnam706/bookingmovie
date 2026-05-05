package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemSeatViewBinding

class SeatAdapter(
    private val seatList: List<String>,
    private val selectedSeats: MutableList<String>,
    private val bookedSeats: List<String> = listOf(),
    private val onSelectionChanged: (Int, Int) -> Unit
) : RecyclerView.Adapter<SeatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSeatViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seatId = seatList[position]
        
        if (seatId == "_") {
            holder.binding.seatView.visibility = android.view.View.INVISIBLE
            holder.binding.seatView.setOnClickListener(null)
            return
        }
        
        holder.binding.seatView.visibility = android.view.View.VISIBLE
        val isBooked = bookedSeats.contains(seatId)
        
        holder.binding.seatView.seatId = seatId
        holder.binding.seatView.isBookedSeat = isBooked
        holder.binding.seatView.isSelectedSeat = selectedSeats.contains(seatId)
        
        if (isBooked) {
            holder.binding.seatView.setOnClickListener(null)
        } else {
            holder.binding.seatView.setOnClickListener {
                if (selectedSeats.contains(seatId)) {
                    selectedSeats.remove(seatId)
                } else {
                    selectedSeats.add(seatId)
                }
                notifyItemChanged(position)
                onSelectionChanged(selectedSeats.size, position)
            }
        }
    }


    override fun getItemCount(): Int = seatList.size

    inner class ViewHolder(val binding: ItemSeatViewBinding) : RecyclerView.ViewHolder(binding.root)
}