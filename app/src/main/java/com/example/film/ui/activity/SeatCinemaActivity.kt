package com.example.film.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.film.R
import com.example.film.databinding.ActivitySeatCinemaBinding
import com.example.film.ui.adapter.SeatAdapter
import com.example.moneymanagement.presentation.view.base.BaseActivity

class SeatCinemaActivity : BaseActivity<ActivitySeatCinemaBinding>(ActivitySeatCinemaBinding::inflate) {

        private lateinit var adapter : SeatAdapter


    override fun initializeComponent() {
        super.initializeComponent()

        adapter = SeatAdapter()
        binding.lstSeat.adapter = adapter

    }


}