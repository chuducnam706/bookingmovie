package com.example.film.ui.activity.admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.film.base.BaseActivity
import com.example.film.databinding.ActivityAdminBinding
import com.example.film.model.BookingModel
import com.example.film.ui.activity.admin.fragment.AdminManagementFragment
import com.example.film.ui.activity.detail.DetailTicketActivity
import com.example.film.ui.activity.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator

class AdminActivity : BaseActivity<ActivityAdminBinding>(ActivityAdminBinding::inflate) {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    private val db = FirebaseFirestore.getInstance()

    override fun initializeComponent() {
        super.initializeComponent()

        setupToolbar()
        showManagement()
    }

    private fun setupToolbar() {
        binding.btnScan.setOnClickListener {
            startScanner()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showManagement() {
        if (supportFragmentManager.findFragmentById(binding.adminContent.id) != null) return

        supportFragmentManager.beginTransaction()
            .replace(binding.adminContent.id, AdminManagementFragment())
            .commit()
    }

    private fun startScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            return
        }

        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Quét mã QR vé")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
            initiateScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startScanner()
        } else if (requestCode == CAMERA_PERMISSION_REQUEST) {
            Toast.makeText(this, "Cần quyền camera để quét mã", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            result.contents?.let { fetchAndShowTicket(it) }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fetchAndShowTicket(bookingId: String) {
        db.collection("bookings").document(bookingId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Không tìm thấy vé: $bookingId", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val booking = document.toObject(BookingModel::class.java)
                if (booking == null) {
                    Toast.makeText(this, "Không đọc được thông tin vé", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                if (booking.bookingId.isBlank()) {
                    booking.bookingId = document.id
                }

                startActivity(Intent(this, DetailTicketActivity::class.java).apply {
                    putExtra("BOOKING_DATA", booking)
                })
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
