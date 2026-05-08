package com.example.film.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.film.databinding.FragmentMyTicketBinding
import com.example.film.ui.activity.detail.DetailTicketActivity
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator

/**
 * MyTicketFragment — Host Fragment chứa 2 tab:
 *   Tab 0: "Vé phim" (TicketListFragment) — danh sách vé + auto-delete vé hết hạn
 *   Tab 1: "Đồ ăn"  (FoodOrderListFragment) — lịch sử đơn food
 *
 * Tab switch dùng ViewPager2 + TabLayout (Material).
 * QR Scanner giữ nguyên ở host Fragment, luôn hiển thị bất kể tab nào.
 */
class MyTicketFragment : BaseFragment<FragmentMyTicketBinding>(FragmentMyTicketBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()

        binding.btnScan.setOnClickListener {
            startScanner()
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> TicketListFragment()
                else -> FoodOrderListFragment()
            }
        }
        binding.viewPager.adapter = pagerAdapter

        // Gắn TabLayout với ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "🎟 Vé phim"
                else -> "🍿 Đồ ăn"
            }
        }.attach()
    }

    // ─── QR Scanner ───────────────────────────────────────────────────────────

    private fun startScanner() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
            return
        }

        val integrator =
            com.google.zxing.integration.android.IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(com.google.zxing.integration.android.IntentIntegrator.QR_CODE)
        integrator.setPrompt("Quét mã QR vé")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001 && grantResults.isNotEmpty()
            && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            startScanner()
        } else {
            android.widget.Toast.makeText(
                requireContext(), "Cần quyền camera để quét mã", android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = com.google.zxing.integration.android.IntentIntegrator.parseActivityResult(
            requestCode, resultCode, data
        )
        if (result != null) {
            if (result.contents != null) {
                fetchAndShowTicket(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fetchAndShowTicket(bookingId: String) {
        if (!isAdded) return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                if (document.exists()) {
                    val booking = document.toObject(com.example.film.model.BookingModel::class.java)
                    if (booking != null) {
                        val intent = android.content.Intent(
                            requireContext(),
                            DetailTicketActivity::class.java
                        )
                        intent.putExtra("BOOKING_DATA", booking)
                        startActivity(intent)
                    }
                } else {
                    android.widget.Toast.makeText(
                        requireContext(), "Không tìm thấy vé: $bookingId",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                android.widget.Toast.makeText(
                    requireContext(), "Lỗi: ${e.message}", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
    }
}