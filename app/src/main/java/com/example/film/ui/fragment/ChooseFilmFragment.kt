package com.example.film.ui.fragment

import android.content.Intent
import android.view.View
import com.example.film.RemoteConfigHelper
import com.example.film.RetrofitClient
import com.example.film.database.FilmDTO
import com.example.film.databinding.FragmentChooseFilmBinding
import com.example.film.ui.activity.BookingActivity
import com.example.film.ui.activity.DetailActivity
import com.example.film.ui.adapter.ChooseFilmAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseFilmFragment :
    BaseFragment<FragmentChooseFilmBinding>(FragmentChooseFilmBinding::inflate) {

    private val API_KEY = "5a6545a40e13bd2dc08a0792ba8a1ba9"
    private lateinit var adapter : ChooseFilmAdapter


    override fun initializeComponent() {
        super.initializeComponent()

        adapter = ChooseFilmAdapter(emptyList(),
            onClickItem = {moveToDetail(it)},
            onClickBooking = {moveToBooking(it)} )

        RemoteConfigHelper.fetchTicketPrice {
            adapter.setTicketPrice(it)
        }

        binding.lstFilm.adapter = adapter
        binding.tvLoading.visibility = View.VISIBLE


        CoroutineScope(Dispatchers.IO).launch {
            val data1 = RetrofitClient.retrofit
                .getPopularMovies(API_KEY, 1).results

            withContext(Dispatchers.Main) {
                binding.tvLoading.visibility = View.GONE
                adapter.setData(data1)
            }
        }

    }

    private fun moveToDetail(data : FilmDTO){
        val intent = Intent(requireContext(), DetailActivity::class.java)
        val gson = Gson()
        val sentData = gson.toJson(data)
        intent.putExtra("FILM", sentData)
        startActivity(intent)
    }

    private fun moveToBooking(data : FilmDTO){
        val intent = Intent(requireContext(), BookingActivity::class.java)
        intent.putExtra("movie_name", data.original_title ?: "")
        intent.putExtra("movie_poster", data.poster_path ?: "")
        startActivity(intent)
    }


}