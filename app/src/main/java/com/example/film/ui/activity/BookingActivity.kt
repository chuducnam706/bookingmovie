package com.example.film.ui.activity

import android.content.Intent
import com.example.film.Common
import com.example.film.databinding.ActivityBookingBinding
import com.example.film.ui.adapter.DateAdapter
import com.example.film.ui.adapter.TimeAdapter
import com.example.moneymanagement.presentation.view.base.BaseActivity

class BookingActivity : BaseActivity<ActivityBookingBinding>(ActivityBookingBinding::inflate){

    private lateinit var adapterDate: DateAdapter
    private lateinit var adapterTime : TimeAdapter


    override fun initializeComponent() {
        super.initializeComponent()

        val dates = Common.initDate()

        adapterTime = TimeAdapter(
            Common.generateShowTimes(true, 0, 24, 3).toMutableList()
        ) { time ->
            val intent = Intent(this, SeatCinemaActivity::class.java)
            startActivity(intent)
        }
        binding.lstTime.adapter = adapterTime

        adapterDate = DateAdapter(dates.toMutableList()){ date ->
            val position = dates.indexOf(date)
            val isToday = position == 0
            adapterTime.updateData(Common.generateShowTimes(isToday, 0, 24, 3))
        }
        binding.lstDate.adapter = adapterDate

    }

}



