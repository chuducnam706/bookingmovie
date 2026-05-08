package com.example.film.ui.activity.main

import android.Manifest
import android.os.Build
import androidx.viewpager2.widget.ViewPager2
import com.example.film.R
import com.example.film.databinding.ActivityMainBinding
import com.example.film.ui.adapter.MainAdapter
import com.example.moneymanagement.presentation.view.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var adapter: MainAdapter

    override fun initializeComponent() {
        super.initializeComponent()

        adapter = MainAdapter(this)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.isUserInputEnabled = false

        // Link BottomNavigationView with ViewPager2
        binding.nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_film -> binding.viewPager2.currentItem = 0
                R.id.menu_cinema -> binding.viewPager2.currentItem = 1
                R.id.menu_food -> binding.viewPager2.currentItem = 2
                R.id.menu_ticket -> binding.viewPager2.currentItem = 3
                R.id.menu_setting -> binding.viewPager2.currentItem = 4
            }
            true
        }

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.nav.menu.getItem(position).isChecked = true
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

    }
}