package com.example.film.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemFoodManagementBinding
import com.example.film.model.FoodItem
import com.bumptech.glide.Glide

class FoodManagementAdapter(
    private var foods: List<FoodItem>,
    private val onUpdatePrice: (FoodItem, Long) -> Unit,
    private val onDelete: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodManagementAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFoodManagementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foods[position]
        holder.binding.apply {
            txtFoodName.text = food.name
            txtFoodPrice.text = String.format("%,dđ", food.price)
            
            Glide.with(root.context)
                .load(food.imageUrl)
                .into(imgFood)

            btnIncrease.setOnClickListener { onUpdatePrice(food, food.price + 5000) }
            btnDecrease.setOnClickListener { onUpdatePrice(food, food.price - 5000) }
            btnDelete.setOnClickListener { onDelete(food) }
        }
    }

    override fun getItemCount(): Int = foods.size

    fun updateData(newFoods: List<FoodItem>) {
        foods = newFoods
        notifyDataSetChanged()
    }
}
