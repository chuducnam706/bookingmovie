package com.example.film.ui.fragment

import android.os.Bundle

import android.view.View
import com.example.film.databinding.FragmentMyTicketBinding
import com.example.film.model.BookingModel
import com.example.film.ui.adapter.TicketAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyTicketFragment : BaseFragment<FragmentMyTicketBinding>(FragmentMyTicketBinding::inflate) {

    private lateinit var adapter: TicketAdapter
    private val bookingList = mutableListOf<BookingModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchBookings()
        
        binding.btnScan.setOnClickListener {
            startScanner()
        }
    }

    private fun startScanner() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
            return
        }
        
        val integrator = com.google.zxing.integration.android.IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(com.google.zxing.integration.android.IntentIntegrator.QR_CODE)
        integrator.setPrompt("Quét mã QR vé")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startScanner()
        } else {
            android.widget.Toast.makeText(requireContext(), "Cần quyền camera để quét mã", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = com.google.zxing.integration.android.IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                fetchAndShowTicket(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fetchAndShowTicket(bookingId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("bookings")
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val booking = document.toObject(BookingModel::class.java)
                    if (booking != null) {
                        val intent = android.content.Intent(requireContext(), com.example.film.ui.activity.DetailTicketActivity::class.java)
                        intent.putExtra("BOOKING_DATA", booking)
                        startActivity(intent)
                    }
                } else {
                    android.widget.Toast.makeText(requireContext(), "Không tìm thấy vé: $bookingId", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(requireContext(), "Lỗi: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchBookings() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("bookings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                bookingList.clear()
                for (document in result) {
                    val booking = document.toObject(BookingModel::class.java)
                    bookingList.add(booking)
                }
                // In-memory sort to avoid needing a Firestore composite index
                bookingList.sortByDescending { it.timestamp }
                
                adapter = TicketAdapter(bookingList)
                binding.lstTicket.adapter = adapter
            }

            .addOnFailureListener { e ->
                android.util.Log.e("MyTicketFragment", "Error fetching bookings", e)
                android.widget.Toast.makeText(context, "Failed to load tickets: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                if (e.message?.contains("index") == true) {
                    android.util.Log.d("MyTicketFragment", "Missing index. Check Logcat for the link to create it.")
                }
            }

    }
}