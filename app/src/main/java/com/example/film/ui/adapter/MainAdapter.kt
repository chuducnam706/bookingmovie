package com.example.film.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.film.ui.fragment.ChooseCinemaFragment
import com.example.film.ui.fragment.ChooseFilmFragment
import com.example.film.ui.fragment.ChooseFoodFragment
import com.example.film.ui.fragment.MyTicketFragment
import com.example.film.ui.fragment.SaleFragment

class MainAdapter(fragmentActivity : FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(p0: Int): Fragment {
        return when(p0) {
            0 -> ChooseFilmFragment()
            1 -> ChooseCinemaFragment()
            2 -> ChooseFoodFragment()
            3 -> SaleFragment()
            4 -> MyTicketFragment()
            else -> ChooseFilmFragment()
        }
    }

    override fun getItemCount(): Int = 5
}