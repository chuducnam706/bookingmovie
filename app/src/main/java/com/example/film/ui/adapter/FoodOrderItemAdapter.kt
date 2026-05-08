package com.example.film.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemFoodOrderItemBinding
import com.example.film.model.FoodOrderItem

class FoodOrderItemAdapter(
    private var items: List<FoodOrderItem>
) : RecyclerView.Adapter<FoodOrderItemAdapter.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<FoodOrderItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodOrderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size

    class ViewHolder(private val binding: ItemFoodOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(item: FoodOrderItem) {
            binding.txtItemName.text = "• ${item.name} x${item.quantity}"
            binding.txtItemPrice.text = "${String.format("%,d", item.price * item.quantity)}đ"
        }
    }
}
