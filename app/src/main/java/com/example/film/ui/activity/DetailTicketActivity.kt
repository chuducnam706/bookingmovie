package com.example.film.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.film.databinding.ActivityDetailTicketBinding
import com.example.film.model.BookingModel
import com.example.film.utils.QRCodeHelper

class DetailTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTicketBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val booking = intent.getSerializableExtra("BOOKING_DATA") as? BookingModel
        if (booking == null) {
            finish()
            return
        }

        setupUI(booking)
    }

    private fun setupUI(booking: BookingModel) {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.txtMovieName.text = booking.movieName
        binding.txtCinemaName.text = booking.cinemaName
        binding.txtBookingId.text = "Mã vé: #${if (booking.bookingId.isNotEmpty()) booking.bookingId else booking.timestamp}"
        binding.txtDate.text = booking.date
        binding.txtTime.text = booking.time
        binding.txtSeats.text = booking.seats.joinToString(", ")
        binding.txtTotalPrice.text = "${String.format("%,d", booking.totalPrice)}đ"

        if (booking.moviePoster.isNotEmpty()) {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w300${booking.moviePoster}")
                .centerCrop()
                .into(binding.imgPoster)
        }

        val qrContent = if (booking.bookingId.isNotEmpty()) booking.bookingId else "TICKET-${booking.timestamp}"
        val qrBitmap = QRCodeHelper.generateQRCode(qrContent, 500)
        binding.imgQRCode.setImageBitmap(qrBitmap)
    }
}
