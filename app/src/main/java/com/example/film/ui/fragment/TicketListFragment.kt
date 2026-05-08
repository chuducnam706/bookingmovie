package com.example.film.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.film.databinding.FragmentTicketListBinding
import com.example.film.model.BookingModel
import com.example.film.ui.adapter.TicketAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TicketListFragment : BaseFragment<FragmentTicketListBinding>(FragmentTicketListBinding::inflate) {

    companion object {
        private const val EXPIRE_DELAY_MINUTES = 1
    }

    private lateinit var adapter: TicketAdapter
    private var bookingListener: ListenerRegistration? = null

    private var isCleaningExpired = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TicketAdapter()
        binding.lstTicket.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        isCleaningExpired = false
        startListeningBookings()
    }

    override fun onPause() {
        super.onPause()
        stopListeningBookings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopListeningBookings()
    }

    private fun startListeningBookings() {
        stopListeningBookings()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        bookingListener = db.collection("bookings")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { result, e ->
                if (!isAdded || view == null) return@addSnapshotListener
                if (e != null) {
                    android.util.Log.e("TicketListFragment", "Error", e)
                    return@addSnapshotListener
                }
                if (result == null) return@addSnapshotListener

                val now = System.currentTimeMillis()

                val validTickets = mutableListOf<BookingModel>()
                val expiredIds = mutableListOf<String>()

                for (doc in result.documents) {
                    val booking = doc.toObject(BookingModel::class.java) ?: continue
                    val expirationMillis = getExpirationMillis(booking.date, booking.time)

                    // Nếu quá hạn (expirationMillis < now) thì xóa
                    if (expirationMillis != null && expirationMillis < now) {
                        expiredIds.add(doc.id)
                    } else {
                        validTickets.add(booking)
                    }
                }

                if (expiredIds.isNotEmpty() && !isCleaningExpired) {
                    isCleaningExpired = true
                    deleteExpiredTickets(db, expiredIds)
                }

                val sorted = validTickets.sortedByDescending { it.timestamp }
                adapter.submitList(sorted)

                binding.lstTicket.visibility =
                    if (sorted.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    private fun stopListeningBookings() {
        bookingListener?.remove()
        bookingListener = null
    }

    private fun getExpirationMillis(dateStr: String, timeStr: String): Long? {
        // dateStr: "Thứ sáu - 08/05" -> lấy "08/05"
        val datePart = dateStr.substringAfter(" - ", dateStr).trim()
        
        // timeStr: "10:00 - 13:00" -> lấy giờ kết thúc "13:00"
        // Nếu chỉ có "10:00" thì lấy chính nó
        val timePart = timeStr.substringAfter(" - ", timeStr).trim()

        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val parsed = sdf.parse("$datePart/$currentYear $timePart") ?: return null

            val parsedCal = Calendar.getInstance().apply { time = parsed }
            
            // Xử lý khi qua năm mới (ví dụ vé tháng 1 nhưng đang là tháng 12)
            val sixMonthsLater = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
            if (parsedCal.after(sixMonthsLater)) {
                parsedCal.add(Calendar.YEAR, -1)
            }

            parsedCal.add(Calendar.MINUTE, EXPIRE_DELAY_MINUTES)
            
            val result = parsedCal.timeInMillis
           Log.d("TicketListFragment", "Ticket: $dateStr $timeStr -> Expire at: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(result)}")
            
            result
        } catch (ex: Exception) {
            null
        }
    }

    private fun deleteExpiredTickets(db: FirebaseFirestore, ids: List<String>) {
        val batch = db.batch()
        ids.forEach { id ->
            batch.delete(db.collection("bookings").document(id))
        }
        batch.commit()
            .addOnSuccessListener {
                Log.d("TicketListFragment", "Deleted ${ids.size} expired tickets")

            }
            .addOnFailureListener { e ->
                Log.e("TicketListFragment", "Delete expired failed", e)
                isCleaningExpired = false
            }
    }
}
