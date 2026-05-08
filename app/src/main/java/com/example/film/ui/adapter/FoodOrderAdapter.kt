package com.example.film.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.film.databinding.ItemFoodOrderBinding
import com.example.film.model.FoodOrder
import com.example.film.ui.activity.detail.DetailFoodOrderActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodOrderAdapter : RecyclerView.Adapter<FoodOrderAdapter.ViewHolder>() {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FoodOrder>() {
            override fun areItemsTheSame(oldItem: FoodOrder, newItem: FoodOrder) =
                oldItem.orderId == newItem.orderId

            override fun areContentsTheSame(oldItem: FoodOrder, newItem: FoodOrder) =
                oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    fun submitList(list: List<FoodOrder>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(differ.currentList[position])

    override fun getItemCount() = differ.currentList.size

    inner class ViewHolder(private val binding: ItemFoodOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val itemAdapter = FoodOrderItemAdapter(emptyList())

        init {
            binding.lstFoodItems.layoutManager = LinearLayoutManager(binding.root.context)
            binding.lstFoodItems.adapter = itemAdapter
            binding.lstFoodItems.isNestedScrollingEnabled = false
        }

        @SuppressLint("DefaultLocale", "SetTextI18n")
        fun bind(order: FoodOrder) {
            binding.txtCinema.text = order.cinemaName
            binding.txtTotal.text = "Tổng: ${String.format("%,d", order.totalPrice)}đ"
            binding.txtOrderTime.text = SimpleDateFormat(
                "dd/MM/yyyy HH:mm", Locale.getDefault()
            ).format(Date(order.timestamp))

            itemAdapter.updateItems(order.items)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = android.content.Intent(
                    context,
                    DetailFoodOrderActivity::class.java
                )
                intent.putExtra("FOOD_ORDER_DATA", order)
                context.startActivity(intent)
            }
        }
    }
}
