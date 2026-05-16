package com.example.film.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.example.film.databinding.FragmentFoodOrderListBinding
import com.example.film.model.FoodOrder
import com.example.film.ui.adapter.FoodOrderAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class FoodOrderListFragment : BaseFragment<FragmentFoodOrderListBinding>(FragmentFoodOrderListBinding::inflate) {

    companion object {
        private const val EXPIRE_DELAY_MINUTES = 1
    }

    private lateinit var adapter: FoodOrderAdapter
    private var foodListener: ListenerRegistration? = null
    private val expirationHandler = Handler(Looper.getMainLooper())
    private var expirationRunnable: Runnable? = null
    private var isCleaningExpired = false

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

                val allOrders = result.documents
                    .mapNotNull { doc ->
                        doc.toObject(FoodOrder::class.java)?.let { order ->
                            if (order.orderId.isBlank()) order.copy(orderId = doc.id) else order
                        }
                    }

                val orders = cleanExpiredFoodOrdersIfNeeded(db, allOrders)
                    .sortedByDescending { it.timestamp }

                adapter.submitList(orders)
                scheduleNextExpiration(orders)

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
        expirationRunnable?.let { expirationHandler.removeCallbacks(it) }
        expirationRunnable = null
    }

    private fun cleanExpiredFoodOrdersIfNeeded(
        db: FirebaseFirestore,
        orders: List<FoodOrder>
    ): List<FoodOrder> {
        val now = System.currentTimeMillis()
        val expiredOrders = orders.filter { getExpirationMillis(it) <= now }

        if (expiredOrders.isNotEmpty() && !isCleaningExpired) {
            deleteExpiredFoodOrders(db, expiredOrders)
        }

        val expiredIds = expiredOrders.map { it.orderId }.toSet()
        return orders.filterNot { it.orderId in expiredIds }
    }

    private fun scheduleNextExpiration(orders: List<FoodOrder>) {
        expirationRunnable?.let { expirationHandler.removeCallbacks(it) }
        expirationRunnable = null

        val now = System.currentTimeMillis()
        val nextExpirationMillis = orders
            .map { getExpirationMillis(it) }
            .filter { it > now }
            .minOrNull()
            ?: return

        val runnable = Runnable {
            if (isAdded && view != null) {
                startListeningFoodOrders()
            }
        }

        expirationRunnable = runnable
        expirationHandler.postDelayed(runnable, nextExpirationMillis - now)
    }

    private fun deleteExpiredFoodOrders(db: FirebaseFirestore, orders: List<FoodOrder>) {
        isCleaningExpired = true

        val batch = db.batch()
        orders.forEach { order ->
            if (order.orderId.isNotBlank()) {
                batch.delete(db.collection("food_orders").document(order.orderId))
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("FoodOrderListFragment", "Deleted ${orders.size} expired food orders")
                isCleaningExpired = false
            }
            .addOnFailureListener { e ->
                Log.e("FoodOrderListFragment", "Delete expired food orders failed", e)
                isCleaningExpired = false
            }
    }

    private fun getExpirationMillis(order: FoodOrder): Long {
        return order.timestamp + EXPIRE_DELAY_MINUTES * 60 * 1000L
    }
}
