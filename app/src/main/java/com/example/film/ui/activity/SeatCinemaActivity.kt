package com.example.film.ui.activity

import android.widget.Toast
import com.example.film.utils.Common
import com.example.film.utils.RemoteConfigHelper
import com.example.film.databinding.ActivitySeatCinemaBinding
import com.example.film.model.BookingModel
import com.example.film.ui.adapter.SeatAdapter
import com.example.moneymanagement.presentation.view.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SeatCinemaActivity : BaseActivity<ActivitySeatCinemaBinding>(ActivitySeatCinemaBinding::inflate) {

    private lateinit var adapter: SeatAdapter
    private var selectedSeats: MutableList<String> = mutableListOf()
    private var bookedSeats: MutableList<String> = mutableListOf()
    private var ticketPrice: Long = 0L
    private var showKey: String = ""
    private var seatListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()

    override fun initializeComponent() {
        super.initializeComponent()

        val cinemaName = intent.getStringExtra("selected_cinema") ?: ""
        val date = intent.getStringExtra("selected_date") ?: ""
        val time = intent.getStringExtra("selected_time") ?: ""
        val movieName = intent.getStringExtra("movie_name") ?: ""
        val moviePoster = intent.getStringExtra("movie_poster") ?: ""

        // Use only the dd/MM part of the date for the key — consistent across all devices
        val dateKey = Common.extractDateKey(date)

        // Create a safe document ID — alphanumeric, underscores and hyphens only
        val rawKey = "${movieName}_${cinemaName}_${dateKey}_${time}"
        showKey = rawKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")

        // Fetch ticket price from Firebase Remote Config
        RemoteConfigHelper.fetchTicketPrice { price ->
            ticketPrice = price
            updateUI(selectedSeats.size)
        }

        // Listen to ONE document (no query, no index needed)
        listenToShowtimeDocument()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnBuy.setOnClickListener {
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            processBooking(movieName, moviePoster, cinemaName, date, time)
        }
    }

    private fun listenToShowtimeDocument() {
        val docRef = db.collection("showtimes").document(showKey)

        seatListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                android.util.Log.e("SeatCinema", "Listen failed: ${e.message}", e)
                // If doc doesn't exist yet, just show empty seats
                setupSeatList()
                return@addSnapshotListener
            }

            bookedSeats.clear()
            if (snapshot != null && snapshot.exists()) {
                val seats = snapshot.get("bookedSeats") as? List<String> ?: emptyList()
                bookedSeats.addAll(seats)
            }

            // Auto-deselect any seats that were just booked by someone else
            val conflict = selectedSeats.filter { bookedSeats.contains(it) }
            if (conflict.isNotEmpty()) {
                selectedSeats.removeAll(conflict.toSet())
                Toast.makeText(this, "Ghế ${conflict.joinToString(", ")} vừa bị người khác đặt!", Toast.LENGTH_LONG).show()
            }

            setupSeatList()
            updateUI(selectedSeats.size)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        seatListener?.remove()
    }

    private fun setupSeatList() {
        val seatList = mutableListOf<String>()
        val rowLabels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
        
        val layout = listOf(
            "__SS__SS__", // A
            "_SSS__SSS_", // B
            "SSSS__SSSS", // C
            "SSSS__SSSS", // D
            "SSSS__SSSS", // E
            "SSSS__SSSS", // F
            "SSSS__SSSS", // G
            "SSSS__SSSS", // H
            "SSSSSSSSSS"  // I
        )

        for (i in layout.indices) {
            val rowConfig = layout[i]
            val rowLabel = rowLabels[i]
            for (j in rowConfig.indices) {
                if (rowConfig[j] == 'S') {
                    seatList.add("$rowLabel${j + 1}")
                } else {
                    seatList.add("_")
                }
            }
        }
        
        adapter = SeatAdapter(seatList, selectedSeats, bookedSeats) { count, _ ->
            updateUI(count)
        }
        binding.lstSeat.adapter = adapter
    }

    private fun updateUI(count: Int) {
        binding.txtSelectedSeats.text = if (selectedSeats.isEmpty()) "Chưa chọn" else "Đã chọn: ${selectedSeats.joinToString(", ")}"
        binding.txtTotalPrice.text = "${count * ticketPrice}đ"
    }

    /**
     * Uses a Firestore TRANSACTION to atomically:
     * 1. Read the current booked seats from showtimes/{showKey}
     * 2. Check if any selected seat is already taken
     * 3. If not, add the new seats to the document
     * 4. Create the booking record for ticket history
     */
    private fun processBooking(movieName: String, moviePoster: String, cinemaName: String, date: String, time: String) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt vé", Toast.LENGTH_SHORT).show()
            // Optional: Redirect to login activity here if you have one
            return
        }
        
        val uid = user.uid
        binding.btnBuy.isEnabled = false

        val showtimeRef = db.collection("showtimes").document(showKey)
        val seatsToBook = ArrayList(selectedSeats) // Copy

        db.runTransaction { transaction ->
            val snapshot = transaction.get(showtimeRef)

            // Get current booked seats
            val currentBooked = if (snapshot.exists()) {
                val data = snapshot.data
                @Suppress("UNCHECKED_CAST")
                data?.get("bookedSeats") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }

            // Check for conflicts
            val conflicts = seatsToBook.filter { currentBooked.contains(it) }
            if (conflicts.isNotEmpty()) {
                throw Exception("Ghế ${conflicts.joinToString(", ")} đã bị đặt rồi!")
            }

            // Merge new seats into the document
            val updatedSeats = currentBooked + seatsToBook
            val showtimeData = hashMapOf(
                "bookedSeats" to updatedSeats,
                "showKey" to showKey,
                "movieName" to movieName,
                "cinemaName" to cinemaName
            )
            
            transaction.set(showtimeRef, showtimeData)

            // Also create the booking record for ticket history
            val bookingId = db.collection("bookings").document().id
            val bookingRef = db.collection("bookings").document(bookingId)
            val booking = BookingModel(
                bookingId = bookingId,
                userId = uid,
                movieName = movieName,
                moviePoster = moviePoster,
                cinemaName = cinemaName,
                date = date,
                time = time,
                showKey = showKey,
                seats = seatsToBook,
                totalPrice = seatsToBook.size * ticketPrice
            )
            transaction.set(bookingRef, booking)

            null // Transaction success
        }.addOnSuccessListener {
            Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            android.util.Log.e("SeatCinema", "Booking failed", e)
            val errorMsg = when {
                e.message?.contains("PERMISSION_DENIED") == true -> "Lỗi: Bạn không có quyền ghi dữ liệu. Kiểm tra Firestore Rules."
                e.message?.contains("NOT_FOUND") == true -> "Lỗi: Không tìm thấy dữ liệu."
                else -> e.message ?: "Lỗi không xác định"
            }
            Toast.makeText(this, "Lỗi đặt vé: $errorMsg", Toast.LENGTH_LONG).show()
            binding.btnBuy.isEnabled = true
        }
    }
}