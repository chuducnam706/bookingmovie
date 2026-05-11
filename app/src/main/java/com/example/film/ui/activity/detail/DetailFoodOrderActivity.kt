package com.example.film.ui.activity.detail

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.film.databinding.ActivityDetailFoodOrderBinding
import com.example.film.model.FoodOrder
import com.example.film.ui.adapter.FoodOrderItemAdapter
import com.example.film.utils.QRCodeHelper
import com.example.film.base.BaseActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailFoodOrderActivity : BaseActivity<ActivityDetailFoodOrderBinding>(ActivityDetailFoodOrderBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val order = intent.getSerializableExtra("FOOD_ORDER_DATA") as? FoodOrder
        if (order == null) {
            finish()
            return
        }

        setupUI(order)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setupUI(order: FoodOrder) {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.txtCinemaName.text = order.cinemaName
        binding.txtOrderId.text = "Mã đơn: #${
            if (order.orderId.isNotEmpty()) order.orderId.takeLast(8).uppercase()
            else order.timestamp.toString()
        }"
        binding.txtOrderTime.text = SimpleDateFormat(
            "dd/MM/yyyy HH:mm", Locale.getDefault()
        ).format(Date(order.timestamp))

        val itemAdapter = FoodOrderItemAdapter(order.items)
        binding.lstFoodItems.layoutManager = LinearLayoutManager(this)
        binding.lstFoodItems.adapter = itemAdapter
        binding.lstFoodItems.isNestedScrollingEnabled = false

        binding.txtTotalPrice.text = "${String.format("%,d", order.totalPrice)}đ"

        val totalQty = order.items.sumOf { it.quantity }
        binding.txtTotalItems.text = "$totalQty món"

        val qrContent = order.orderId.ifEmpty { "FOOD-${order.timestamp}" }
        val qrBitmap = QRCodeHelper.generateQRCode(qrContent, 500)
        binding.imgQRCode.setImageBitmap(qrBitmap)
    }
}
