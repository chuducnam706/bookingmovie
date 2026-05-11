package com.example.film.ui.activity.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.film.base.BaseActivity
import com.example.film.databinding.ActivityAdminBinding
import com.example.film.model.BookingModel
import com.example.film.ui.activity.login.LoginActivity
import com.example.film.utils.Common
import com.example.film.viewmodel.AdminViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdminActivity : BaseActivity<ActivityAdminBinding>(ActivityAdminBinding::inflate) {

    private val db = FirebaseFirestore.getInstance()
    private var allBookings: List<BookingModel> = emptyList()
    private lateinit var pagerAdapter: AdminPagerAdapter
    private val adminViewModel: AdminViewModel by lazy { 
        ViewModelProvider(this)[AdminViewModel::class.java]
    }

    private var selectedCinema = "Tất cả"
    private var selectedYear = "Tất cả"
    private var selectedMonth = "Tất cả"


    override fun initializeComponent() {
        super.initializeComponent()

        setupToolbar()
        setupSpinners()
        setupViewPager()
        fetchData()
    }

    private fun setupToolbar() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupSpinners() {
        // Cinema Spinner
        val cinemas = mutableListOf("Tất cả")
        cinemas.addAll(Common.initCinema())
        val cinemaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cinemas)
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCinema.adapter = cinemaAdapter

        // Year Spinner
        val years = mutableListOf("Tất cả")
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        for (i in 0..2) {
            years.add((currentYear - i).toString())
        }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(1) // Default to current year (index 1)

        // Month Spinner
        val months = mutableListOf("Tất cả")
        for (i in 1..12) months.add(i.toString())
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCinema = binding.spinnerCinema.selectedItem.toString()
                selectedYear = binding.spinnerYear.selectedItem.toString()
                selectedMonth = binding.spinnerMonth.selectedItem.toString()
                updateCharts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerCinema.onItemSelectedListener = listener
        binding.spinnerYear.onItemSelectedListener = listener
        binding.spinnerMonth.onItemSelectedListener = listener
    }

    private fun setupViewPager() {
        pagerAdapter = AdminPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Doanh thu"
                1 -> "Lượng vé"
                else -> "Quản lý"
            }
        }.attach()
    }

    private fun fetchData() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                allBookings = result.toObjects(BookingModel::class.java)
                updateCharts()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCharts() {
        if (allBookings.isEmpty()) return

        val filtered = allBookings.filter { booking ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = booking.timestamp

            val matchesCinema = selectedCinema == "Tất cả" || booking.cinemaName == selectedCinema
            val matchesYear =
                selectedYear == "Tất cả" || calendar.get(Calendar.YEAR).toString() == selectedYear
            val matchesMonth =
                selectedMonth == "Tất cả" || (calendar.get(Calendar.MONTH) + 1).toString() == selectedMonth

            matchesCinema && matchesYear && matchesMonth
        }

        val revenueData = mutableListOf<Float>()
        val volumeData = mutableListOf<Float>()
        val labels = mutableListOf<String>()

        if (selectedYear != "Tất cả" && selectedMonth != "Tất cả") {
            // Group by Day in that specific Month/Year
            val grouped = filtered.groupBy {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.DAY_OF_MONTH)
            }
            for (i in 1..31) {
                labels.add(i.toString())
                val group = grouped[i] ?: emptyList()
                revenueData.add(group.sumOf { it.totalPrice }.toFloat())
                volumeData.add(group.sumOf { it.seats.size }.toFloat())
            }
        } else if (selectedYear != "Tất cả") {
            // Group by Month in that specific Year
            val grouped = filtered.groupBy {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) + 1
            }
            for (i in 1..12) {
                labels.add("T$i")
                val group = grouped[i] ?: emptyList()
                revenueData.add(group.sumOf { it.totalPrice }.toFloat())
                volumeData.add(group.sumOf { it.seats.size }.toFloat())
            }
        } else if (selectedMonth != "Tất cả") {
            // Group by Year for that specific Month
            val grouped = filtered.groupBy {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.YEAR)
            }
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            for (i in 2 downTo 0) {
                val year = currentYear - i
                labels.add(year.toString())
                val group = grouped[year] ?: emptyList()
                revenueData.add(group.sumOf { it.totalPrice }.toFloat())
                volumeData.add(group.sumOf { it.seats.size }.toFloat())
            }
        } else {
            // Default view: Group by Month of the current year
            val calNow = Calendar.getInstance()
            val currentYear = calNow.get(Calendar.YEAR)
            val grouped = filtered.filter {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.YEAR) == currentYear
            }.groupBy {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) + 1
            }
            
            for (i in 1..12) {
                labels.add("T$i")
                val group = grouped[i] ?: emptyList()
                revenueData.add(group.sumOf { it.totalPrice }.toFloat())
                volumeData.add(group.sumOf { it.seats.size }.toFloat())
            }
        }


        adminViewModel.updateRevenue(revenueData, labels, "Doanh thu (VNĐ)")
        adminViewModel.updateVolume(volumeData, labels, "Lượng vé (Cái)")
    }
}
