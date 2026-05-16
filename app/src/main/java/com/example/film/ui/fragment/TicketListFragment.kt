package com.example.film.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val expirationHandler = Handler(Looper.getMainLooper())
    private var expirationRunnable: Runnable? = null

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

                val allTickets = mutableListOf<BookingModel>()

                for (doc in result.documents) {
                    val booking = doc.toObject(BookingModel::class.java) ?: continue
                    if (booking.bookingId.isBlank()) booking.bookingId = doc.id
                    allTickets.add(booking)
                }

                val activeTickets = cleanExpiredTicketsIfNeeded(db, allTickets)
                val sorted = activeTickets.sortedByDescending { it.timestamp }
                adapter.submitList(sorted)
                scheduleNextExpiration(activeTickets)

                binding.lstTicket.visibility =
                    if (sorted.isEmpty()) View.GONE else View.VISIBLE
                
                binding.txtEmpty.visibility =
                    if (sorted.isEmpty()) View.VISIBLE else View.GONE
                
                android.util.Log.d("TicketListFragment", "Loaded ${sorted.size} tickets")
            }
    }

    private fun stopListeningBookings() {
        bookingListener?.remove()
        bookingListener = null
        expirationRunnable?.let { expirationHandler.removeCallbacks(it) }
        expirationRunnable = null
    }

    private fun getExpirationMillis(dateStr: String, timeStr: String): Long? {
        val datePart = dateStr.substringAfter(" - ", dateStr).trim()

        return try {
            val dateParts = datePart.split("/")
            if (dateParts.size != 2) return null

            val day = dateParts[0].toIntOrNull() ?: return null
            val month = dateParts[1].toIntOrNull() ?: return null
            val startPart = timeStr.substringBefore(" - ", timeStr).trim()
            val endPart = timeStr.substringAfter(" - ", startPart).trim()
            val startMinutes = parseTimeMinutes(startPart) ?: return null
            val endMinutes = parseTimeMinutes(endPart) ?: return null

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val startCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, startMinutes / 60)
                set(Calendar.MINUTE, startMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, endMinutes / 60)
                set(Calendar.MINUTE, endMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val sixMonthsLater = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }
            if (startCal.after(sixMonthsLater)) {
                startCal.add(Calendar.YEAR, -1)
                endCal.add(Calendar.YEAR, -1)
            }

            if (endMinutes <= startMinutes) {
                endCal.add(Calendar.DAY_OF_MONTH, 1)
            }

            endCal.add(Calendar.MINUTE, EXPIRE_DELAY_MINUTES)
            
            val result = endCal.timeInMillis
           Log.d("TicketListFragment", "Ticket: $dateStr $timeStr -> Expire at: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(result)}")
            
            result
        } catch (ex: Exception) {
            null
        }
    }

    private fun cleanExpiredTicketsIfNeeded(
        db: FirebaseFirestore,
        tickets: List<BookingModel>
    ): List<BookingModel> {
        val now = System.currentTimeMillis()
        val expiredTickets = tickets.filter { booking ->
            val expirationMillis = getExpirationMillis(booking.date, booking.time)
            expirationMillis != null && expirationMillis <= now
        }

        if (expiredTickets.isNotEmpty() && !isCleaningExpired) {
            deleteExpiredTickets(db, expiredTickets)
        }

        val expiredIds = expiredTickets.map { it.bookingId }.toSet()
        return tickets.filterNot { it.bookingId in expiredIds }
    }

    private fun scheduleNextExpiration(tickets: List<BookingModel>) {
        expirationRunnable?.let { expirationHandler.removeCallbacks(it) }
        expirationRunnable = null

        val now = System.currentTimeMillis()
        val nextExpirationMillis = tickets
            .mapNotNull { getExpirationMillis(it.date, it.time) }
            .filter { it > now }
            .minOrNull()
            ?: return

        val runnable = Runnable {
            if (isAdded && view != null) {
                startListeningBookings()
            }
        }

        expirationRunnable = runnable
        expirationHandler.postDelayed(runnable, nextExpirationMillis - now)
    }

    private fun deleteExpiredTickets(db: FirebaseFirestore, tickets: List<BookingModel>) {
        isCleaningExpired = true

        db.runTransaction { transaction ->
            val showtimeRefs = tickets
                .filter { it.showKey.isNotBlank() && it.seats.isNotEmpty() }
                .groupBy { it.showKey }
                .mapValues { db.collection("showtimes").document(it.key) }

            val showtimeSnapshots = showtimeRefs.mapValues { transaction.get(it.value) }

            tickets.forEach { booking ->
                if (booking.bookingId.isNotBlank()) {
                    transaction.delete(db.collection("bookings").document(booking.bookingId))
                }
            }

            showtimeSnapshots.forEach { (showKey, snapshot) ->
                if (!snapshot.exists()) return@forEach

                val seatsToRemove = tickets
                    .filter { it.showKey == showKey }
                    .flatMap { it.seats }
                    .toSet()

                if (seatsToRemove.isEmpty()) return@forEach

                @Suppress("UNCHECKED_CAST")
                val currentSeats = snapshot.get("bookedSeats") as? List<String> ?: emptyList()
                val remainingSeats = currentSeats.filterNot { it in seatsToRemove }
                transaction.update(showtimeRefs.getValue(showKey), "bookedSeats", remainingSeats)
            }

            null
        }
            .addOnSuccessListener {
                Log.d("TicketListFragment", "Deleted ${tickets.size} expired tickets")
                isCleaningExpired = false
            }
            .addOnFailureListener { e ->
                Log.e("TicketListFragment", "Delete expired failed", e)
                isCleaningExpired = false
            }
    }

    private fun parseTimeMinutes(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null

        return hour * 60 + minute
    }
}
