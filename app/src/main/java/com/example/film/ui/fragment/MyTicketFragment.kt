package com.example.film.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.film.databinding.FragmentMyTicketBinding
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator


class MyTicketFragment : BaseFragment<FragmentMyTicketBinding>(FragmentMyTicketBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
    }

    private fun setupViewPager() {
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> TicketListFragment()
                else -> FoodOrderListFragment()
            }
        }
        binding.viewPager.adapter = pagerAdapter

        // Gắn TabLayout với ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "🎟 Vé phim"
                else -> "🍿 Đồ ăn"
            }
        }.attach()
    }
}
