package com.example.film.ui.fragment

import android.os.Bundle
import android.view.View
import com.example.film.databinding.FragmentFoodOrderListBinding
import com.example.film.model.FoodOrder
import com.example.film.ui.adapter.FoodOrderAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class FoodOrderListFragment : BaseFragment<FragmentFoodOrderListBinding>(FragmentFoodOrderListBinding::inflate) {

    private lateinit var adapter: FoodOrderAdapter
    private var foodListener: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FoodOrderAdapter()
        binding.lstFoodOrders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        startListeningFoodOrders()
    }

    override fun onPause() {
        super.onPause()
        stopListeningFoodOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopListeningFoodOrders()
    }

    private fun startListeningFoodOrders() {
        stopListeningFoodOrders()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        foodListener = db.collection("food_orders")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { result, e ->
                if (!isAdded || view == null) return@addSnapshotListener
                if (e != null) {
                    android.util.Log.e("FoodOrderListFragment", "Error", e)
                    return@addSnapshotListener
                }
                if (result == null) return@addSnapshotListener

                val orders = result.documents
                    .mapNotNull { it.toObject(FoodOrder::class.java) }
                    .sortedByDescending { it.timestamp }

                adapter.submitList(orders)

                // Hiện/ẩn empty state
                binding.layoutEmpty.visibility =
                    if (orders.isEmpty()) View.VISIBLE else View.GONE
                binding.lstFoodOrders.visibility =
                    if (orders.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    private fun stopListeningFoodOrders() {
        foodListener?.remove()
        foodListener = null
    }
}
