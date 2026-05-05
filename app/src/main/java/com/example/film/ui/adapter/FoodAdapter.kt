package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.film.databinding.ItemFoodBinding
import com.example.film.model.FoodItem

class FoodAdapter(
    private val data: MutableList<FoodItem>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<FoodItem>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FoodItem) {
            binding.tvFoodName.text = item.name
            binding.tvFoodPrice.text = "${String.format("%,d", item.price)}đ"

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .centerCrop()
                .into(binding.imgFood)

            updateQuantityUI(item)

            binding.btnAdd.setOnClickListener {
                item.quantity = 1
                updateQuantityUI(item)
                onQuantityChanged()
            }

            binding.btnPlus.setOnClickListener {
                item.quantity++
                updateQuantityUI(item)
                onQuantityChanged()
            }

            binding.btnMinus.setOnClickListener {
                item.quantity--
                updateQuantityUI(item)
                onQuantityChanged()
            }
        }

        private fun updateQuantityUI(item: FoodItem) {
            if (item.quantity > 0) {
                binding.btnAdd.visibility = View.GONE
                binding.layoutQuantity.visibility = View.VISIBLE
                binding.tvQuantity.text = item.quantity.toString()
            } else {
                binding.btnAdd.visibility = View.VISIBLE
                binding.layoutQuantity.visibility = View.GONE
            }
        }
    }
}
