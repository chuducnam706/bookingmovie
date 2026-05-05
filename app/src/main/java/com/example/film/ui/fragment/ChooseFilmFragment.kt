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
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseFilmFragment :
    BaseFragment<FragmentChooseFilmBinding>(FragmentChooseFilmBinding::inflate) {

    private val API_KEY = "5a6545a40e13bd2dc08a0792ba8a1ba9"
    private lateinit var adapter: ChooseFilmAdapter

    override fun initializeComponent() {
        super.initializeComponent()

        adapter = ChooseFilmAdapter(emptyList(),
            onClickItem = { moveToDetail(it) },
            onClickBooking = { moveToBooking(it) })

        RemoteConfigHelper.fetchTicketPrice {
            adapter.setTicketPrice(it)
        }

        binding.lstFilm.adapter = adapter

        // Setup tabs
        val tabs = listOf("Đang chiếu", "Sắp chiếu", "Phim hot")
        for (tab in tabs) {
            binding.tabCategory.addTab(binding.tabCategory.newTab().setText(tab))
        }

        // Load default tab (Đang chiếu)
        loadMovies(0)

        binding.tabCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { loadMovies(it.position) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadMovies(category: Int) {
        binding.tvLoading.visibility = View.VISIBLE
        binding.lstFilm.visibility = View.GONE
        adapter.setData(emptyList())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val movies = when (category) {
                    0 -> RetrofitClient.retrofit.getNowPlayingMovies(API_KEY, 1).results
                    1 -> RetrofitClient.retrofit.getUpcomingMovies(API_KEY, 1).results
                    2 -> RetrofitClient.retrofit.getPopularMovies(API_KEY, 1).results
                    else -> emptyList()
                }

                withContext(Dispatchers.Main) {
                    binding.tvLoading.visibility = View.GONE
                    binding.lstFilm.visibility = View.VISIBLE
                    adapter.setData(movies)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvLoading.visibility = View.GONE
                    binding.lstFilm.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun moveToDetail(data: FilmDTO) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        val gson = Gson()
        val sentData = gson.toJson(data)
        intent.putExtra("FILM", sentData)
        startActivity(intent)
    }

    private fun moveToBooking(data: FilmDTO) {
        val intent = Intent(requireContext(), BookingActivity::class.java)
        intent.putExtra("movie_name", data.original_title ?: "")
        intent.putExtra("movie_poster", data.poster_path ?: "")
        startActivity(intent)
    }
}