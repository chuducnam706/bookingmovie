package com.example.film.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.film.model.FoodOrder
import com.example.film.model.FoodOrderItem
import com.example.film.utils.Common
import com.example.film.databinding.FragmentChooseFoodBinding
import com.example.film.model.FoodItem
import com.example.film.ui.activity.vnpay.PaymentActivity
import com.example.film.ui.adapter.CinemaAdapter
import com.example.film.ui.adapter.FoodAdapter
import com.example.film.viewmodel.PaymentViewModel
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChooseFoodFragment : BaseFragment<FragmentChooseFoodBinding>(FragmentChooseFoodBinding::inflate) {

    private lateinit var cinemaAdapter: CinemaAdapter
    private lateinit var foodAdapter: FoodAdapter
    private var selectedCinema: String = ""
    private var foodList: MutableList<FoodItem> = mutableListOf()
    private val viewModel: PaymentViewModel by viewModels()

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            saveFoodOrder()
        } else {
            Toast.makeText(requireContext(), "Thanh toán đã bị hủy hoặc thất bại", Toast.LENGTH_SHORT).show()
            binding.btnCheckout.isEnabled = true
        }
    }

    override fun initializeComponent() {
        super.initializeComponent()

        // 1. Setup Cinema list
        val cinemas = Common.initCinema()
        selectedCinema = cinemas[0]
        cinemaAdapter = CinemaAdapter(cinemas.toMutableList()) { cinema ->
            selectedCinema = cinema
            // Reset food quantities khi đổi rạp
            foodList.forEach { it.quantity = 0 }
            foodAdapter.notifyDataSetChanged()
            updateFooter()
        }
        binding.lstCinema.adapter = cinemaAdapter

        // 2. Setup Food list
        foodAdapter = FoodAdapter(foodList) {
            updateFooter()
        }
        binding.lstFood.adapter = foodAdapter
        listenToFoods()

        // 3. Quan sát kết quả thanh toán
        viewModel.payment.observe(this) { payment ->
            if (payment.status == "success" && !binding.btnCheckout.isEnabled) {
                val intent = Intent(requireContext(), PaymentActivity::class.java)
                intent.putExtra("payment_url", payment.paymentUrl)
                paymentLauncher.launch(intent)
            } else if (payment.status != "success" && !binding.btnCheckout.isEnabled) {
                Toast.makeText(requireContext(), "Không thể khởi tạo thanh toán", Toast.LENGTH_SHORT).show()
                binding.btnCheckout.isEnabled = true
            }
        }

        // 4. Checkout button — khởi tạo thanh toán
        binding.btnCheckout.setOnClickListener {
            val totalItems = foodList.sumOf { it.quantity }
            if (totalItems == 0) {
                Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPrice = foodList.sumOf { it.price * it.quantity }
            val uniqueOrderId = "FOOD_${System.currentTimeMillis()}"
            val orderDescription = "Thanh toan do an tai $selectedCinema"

            binding.btnCheckout.isEnabled = false
            viewModel.createPayment(totalPrice.toLong(), orderDescription, uniqueOrderId)
        }

        updateFooter()
    }

    /**
     * Lưu đơn food lên Firestore collection "food_orders"
     * Sau khi lưu thành công → reset form
     */
    private fun listenToFoods() {
        val db = FirebaseFirestore.getInstance()
        db.collection("foods")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && isAdded) {
                    val newFoods = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id.toIntOrNull() ?: 0
                        val name = doc.getString("name") ?: ""
                        val price = doc.getLong("price") ?: 0L
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        
                        // Giữ lại số lượng đã chọn nếu item vẫn tồn tại
                        val existing = foodList.find { it.id == id }
                        FoodItem(id, name, price, imageUrl, existing?.quantity ?: 0)
                    }
                    foodList.clear()
                    foodList.addAll(newFoods)
                    foodAdapter.notifyDataSetChanged()
                    updateFooter()
                }
            }
    }

    private fun saveFoodOrder() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            binding.btnCheckout.isEnabled = true
            return
        }

        val db = FirebaseFirestore.getInstance()
        val orderId = db.collection("food_orders").document().id

        val selectedItems = foodList
            .filter { it.quantity > 0 }
            .map { FoodOrderItem(name = it.name, price = it.price, quantity = it.quantity) }

        val totalPrice = foodList.sumOf { it.price * it.quantity }

        val order = FoodOrder(
            orderId = orderId,
            userId = uid,
            cinemaName = selectedCinema,
            items = selectedItems,
            totalPrice = totalPrice,
            timestamp = System.currentTimeMillis()
        )

        db.collection("food_orders")
            .document(orderId)
            .set(order)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                Toast.makeText(
                    requireContext(),
                    "Đặt đồ ăn thành công tại $selectedCinema! Tổng: ${String.format("%,d", totalPrice)}đ",
                    Toast.LENGTH_LONG
                ).show()
                // Reset toàn bộ form
                foodList.forEach { it.quantity = 0 }
                foodAdapter.notifyDataSetChanged()
                updateFooter()
                binding.btnCheckout.isEnabled = true
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(
                    requireContext(),
                    "Lỗi đặt đồ ăn: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnCheckout.isEnabled = true
            }
    }

    private fun updateFooter() {
        val totalItems = foodList.sumOf { it.quantity }
        val totalPrice = foodList.sumOf { it.price * it.quantity }

        binding.tvItemCount.text = "$totalItems sản phẩm"
        binding.tvTotalPrice.text = "${String.format("%,d", totalPrice)}đ"
    }
}