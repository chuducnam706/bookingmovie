package com.example.film.ui.fragment

import android.content.Intent
import com.example.film.databinding.FragmentChooseCinemaBinding
import com.example.film.ui.activity.cinemadetai.CinemaDetailActivity
import com.example.film.ui.adapter.CinemaListAdapter
import com.example.film.utils.Common
import com.example.moneymanagement.presentation.view.base.BaseFragment

class ChooseCinemaFragment : BaseFragment<FragmentChooseCinemaBinding>(FragmentChooseCinemaBinding::inflate) {

    override fun initializeComponent() {
        super.initializeComponent()

        // Danh sách rạp kèm địa chỉ
        val cinemas = Common.initCinemaWithAddress()

        val adapter = CinemaListAdapter(cinemas) { name, address ->
            val intent = Intent(requireContext(), CinemaDetailActivity::class.java)
            intent.putExtra("cinema_name", name)
            intent.putExtra("cinema_address", address)
            startActivity(intent)
        }

        binding.lstCinema.adapter = adapter
    }
}