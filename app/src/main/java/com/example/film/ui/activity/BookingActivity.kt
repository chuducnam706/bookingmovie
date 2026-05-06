package com.example.film.ui.activity

import android.content.Intent
import com.example.film.utils.Common
import com.example.film.databinding.ActivityBookingBinding
import com.example.film.ui.adapter.CinemaAdapter
import com.example.film.ui.adapter.DateAdapter
import com.example.film.ui.adapter.TimeAdapter
import com.bumptech.glide.Glide
import com.example.moneymanagement.presentation.view.base.BaseActivity

class BookingActivity : BaseActivity<ActivityBookingBinding>(ActivityBookingBinding::inflate){

    private lateinit var adapterDate: DateAdapter
    private lateinit var adapterTime : TimeAdapter
    private lateinit var adapterCinema : CinemaAdapter
    private var selectedCinema: String = ""
    private var selectedDate: String = ""


    override fun initializeComponent() {
        super.initializeComponent()

        // 1. Initialize Date
        val dates = Common.initDate()
        selectedDate = dates[0]
        adapterDate = DateAdapter(dates.toMutableList()){ date ->
            selectedDate = date
            val position = dates.indexOf(date)
            val isToday = position == 0
            adapterTime.updateData(Common.generateShowTimes(isToday, 0, 24, 3))
        }
        binding.lstDate.adapter = adapterDate

        // 2. Initialize Cinema
        val cinemas = Common.initCinema()
        selectedCinema = cinemas[0] // Select first cinema by default
        
        // Show time section by default
        binding.lblTime.visibility = android.view.View.VISIBLE
        binding.lstTime.visibility = android.view.View.VISIBLE

        adapterCinema = CinemaAdapter(cinemas.toMutableList()) { cinema ->
            selectedCinema = cinema
            
            // Refresh times based on current date
            val datePosition = dates.indexOf(selectedDate)
            adapterTime.updateData(Common.generateShowTimes(datePosition == 0, 0, 24, 3))
            
            // Smooth scroll to time section when changing cinema
            binding.root.post {
                val scrollY = binding.lblTime.top
                binding.nestedScrollView.smoothScrollTo(0, scrollY)
            }
        }
        binding.lstCinema.adapter = adapterCinema

        val movieName = intent.getStringExtra("movie_name") ?: ""
        val moviePoster = intent.getStringExtra("movie_poster") ?: ""

        binding.tvMovieTitle.text = movieName
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780$moviePoster")
            .centerCrop()
            .into(binding.imgPoster)

        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780$moviePoster")
            .centerCrop()
            .into(binding.imgBackdrop)

        binding.btnBack.setOnClickListener { finish() }

        // 3. Initialize Time
        val initialTimes = Common.generateShowTimes(true, 0, 24, 3)
        adapterTime = TimeAdapter(initialTimes.toMutableList()) { time ->
            val intent = Intent(this, SeatCinemaActivity::class.java)
            intent.putExtra("movie_name", movieName)
            intent.putExtra("movie_poster", moviePoster)
            intent.putExtra("selected_cinema", selectedCinema)
            intent.putExtra("selected_date", selectedDate)
            intent.putExtra("selected_time", time)
            startActivity(intent)
        }

        binding.lstTime.adapter = adapterTime

    }




}



