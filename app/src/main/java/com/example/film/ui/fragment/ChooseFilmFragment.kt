package com.example.film.ui.fragment

import android.content.Intent
import android.view.View
import com.example.film.utils.RemoteConfigHelper
import com.example.film.database.FilmDTO
import com.example.film.databinding.FragmentChooseFilmBinding
import com.example.film.ui.activity.BookingActivity
import com.example.film.ui.activity.DetailActivity
import com.example.film.ui.adapter.BannerAdapter
import com.example.film.ui.adapter.ChooseFilmAdapter
import com.example.moneymanagement.presentation.view.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import androidx.lifecycle.ViewModelProvider
import com.example.film.viewmodel.FilmViewModel
import com.google.gson.Gson

class ChooseFilmFragment :
    BaseFragment<FragmentChooseFilmBinding>(FragmentChooseFilmBinding::inflate) {

    private val API_KEY = "5a6545a40e13bd2dc08a0792ba8a1ba9"
    private lateinit var adapter: ChooseFilmAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var viewModel: FilmViewModel

    override fun initializeComponent() {
        super.initializeComponent()

        adapter = ChooseFilmAdapter(emptyList(),
            onClickItem = { moveToDetail(it) },
            onClickBooking = { moveToBooking(it) })

        RemoteConfigHelper.fetchTicketPrice {
            adapter.setTicketPrice(it)
        }

        binding.lstFilm.adapter = adapter

        viewModel = ViewModelProvider(this)[FilmViewModel::class.java]
        setupBanner()
        observeViewModel()

        // Setup tabs
        val tabs = listOf("Đang chiếu", "Sắp chiếu", "Phim hot")
        for (tab in tabs) {
            binding.tabCategory.addTab(binding.tabCategory.newTab().setText(tab))
        }

        // Load default tab (Đang chiếu)
        viewModel.loadMovies(0)

        binding.tabCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { viewModel.loadMovies(it.position) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.movies.observe(viewLifecycleOwner) { movies ->
            adapter.setData(movies)
        }

        viewModel.hotMovies.observe(viewLifecycleOwner) { hotMovies ->
            bannerAdapter.setData(hotMovies)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.tvLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.lstFilm.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupBanner() {
        bannerAdapter = BannerAdapter(emptyList()) { moveToDetail(it) }
        binding.viewPagerBanner.adapter = bannerAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerBanner)
        viewModel.loadHotMovies()
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