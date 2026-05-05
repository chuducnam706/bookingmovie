package com.example.film.ui.fragment

import android.widget.Toast
import com.example.film.Common
import com.example.film.databinding.FragmentChooseFoodBinding
import com.example.film.model.FoodItem
import com.example.film.ui.adapter.CinemaAdapter
import com.example.film.ui.adapter.FoodAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment

class ChooseFoodFragment : BaseFragment<FragmentChooseFoodBinding>(FragmentChooseFoodBinding::inflate) {

    private lateinit var cinemaAdapter: CinemaAdapter
    private lateinit var foodAdapter: FoodAdapter
    private var selectedCinema: String = ""
    private var foodList: MutableList<FoodItem> = mutableListOf()

    override fun initializeComponent() {
        super.initializeComponent()

        // 1. Setup Cinema list
        val cinemas = Common.initCinema()
        selectedCinema = cinemas[0]
        cinemaAdapter = CinemaAdapter(cinemas.toMutableList()) { cinema ->
            selectedCinema = cinema
            // Reset food quantities when switching cinema
            foodList.forEach { it.quantity = 0 }
            foodAdapter.notifyDataSetChanged()
            updateFooter()
        }
        binding.lstCinema.adapter = cinemaAdapter

        // 2. Setup Food list
        foodList = Common.initFood().toMutableList()
        foodAdapter = FoodAdapter(foodList) {
            updateFooter()
        }
        binding.lstFood.adapter = foodAdapter

        // 3. Checkout button
        binding.btnCheckout.setOnClickListener {
            val totalItems = foodList.sumOf { it.quantity }
            if (totalItems == 0) {
                Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val orderSummary = foodList
                .filter { it.quantity > 0 }
                .joinToString("\n") { "• ${it.name} x${it.quantity} = ${String.format("%,d", it.price * it.quantity)}đ" }

            val totalPrice = foodList.sumOf { it.price * it.quantity }

            Toast.makeText(
                requireContext(),
                "Đặt tại: $selectedCinema\n$orderSummary\nTổng: ${String.format("%,d", totalPrice)}đ",
                Toast.LENGTH_LONG
            ).show()
        }

        updateFooter()
    }

    private fun updateFooter() {
        val totalItems = foodList.sumOf { it.quantity }
        val totalPrice = foodList.sumOf { it.price * it.quantity }

        binding.tvItemCount.text = "$totalItems sản phẩm"
        binding.tvTotalPrice.text = "${String.format("%,d", totalPrice)}đ"
    }
}