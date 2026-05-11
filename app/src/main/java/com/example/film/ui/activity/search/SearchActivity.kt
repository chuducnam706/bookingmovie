package com.example.film.ui.activity.search

import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.film.database.FilmDTO
import com.example.film.databinding.ActivitySearchBinding
import com.example.film.ui.activity.bookticket.BookingActivity
import com.example.film.ui.activity.detail.DetailActivity
import com.example.film.ui.adapter.ChooseFilmAdapter
import com.example.film.viewmodel.FilmViewModel
import com.example.film.base.BaseActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : BaseActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate) {

    private lateinit var adapter: ChooseFilmAdapter
    private val viewModel: FilmViewModel by viewModels()
    private var searchJob: Job? = null
    private val db = FirebaseFirestore.getInstance()

    override fun initializeComponent() {
        super.initializeComponent()

        // Setup Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Setup Adapter
        adapter = ChooseFilmAdapter(emptyList(),
            onClickItem = { moveToDetail(it) },
            onClickBooking = { moveToBooking(it) })
        binding.rvSearchResults.adapter = adapter

        // Listen for dynamic ticket price from Admin (same as ChooseFilmFragment)
        db.collection("settings").document("config")
            .addSnapshotListener { snapshot, _ ->
                if (!isDestroyed && !isFinishing && snapshot != null && snapshot.exists()) {
                    val ticketPrice = snapshot.getLong("ticketPrice") ?: 70000L
                    adapter.setTicketPrice(ticketPrice)
                }
            }

        // Observe ViewModel
        viewModel.movies.observe(this) { movies ->
            adapter.setData(movies)
            if (movies.isEmpty() && binding.edtSearchInput.text.isNotEmpty()) {
                binding.tvLoading.text = "Không tìm thấy kết quả"
                binding.tvLoading.visibility = View.VISIBLE
            } else {
                binding.tvLoading.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.tvLoading.text = "Đang tìm kiếm..."
                binding.tvLoading.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.rvSearchResults.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                binding.tvLoading.text = "Lỗi: $it"
                binding.tvLoading.visibility = View.VISIBLE
            }
        }

        // Setup Search Input
        binding.edtSearchInput.requestFocus()
        showKeyboard(binding.edtSearchInput)

        binding.edtSearchInput.addTextChangedListener { text ->
            searchJob?.cancel()
            val query = text.toString().trim()
            
            if (query.isEmpty()) {
                adapter.setData(emptyList())
                binding.tvLoading.visibility = View.GONE
                return@addTextChangedListener
            }

            searchJob = lifecycleScope.launch {
                delay(500) // Debounce 500ms
                viewModel.searchMovies(query)
            }
        }
    }

    private fun moveToDetail(data: FilmDTO) {
        val intent = Intent(this, DetailActivity::class.java)
        val gson = Gson()
        val sentData = gson.toJson(data)
        intent.putExtra("FILM", sentData)
        startActivity(intent)
    }

    private fun moveToBooking(data: FilmDTO) {
        val intent = Intent(this, BookingActivity::class.java)
        intent.putExtra("movie_name", data.original_title ?: "")
        intent.putExtra("movie_poster", data.poster_path ?: "")
        startActivity(intent)
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
