package com.example.film.ui.activity.admin.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.film.R
import com.example.film.databinding.FragmentAdminManagementBinding
import com.google.android.material.chip.Chip

class AdminManagementFragment : Fragment() {

    private var _binding: FragmentAdminManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupManagementChips()

        if (savedInstanceState == null) {
            selectSection(binding.chipTicketPrice, TicketPriceManagementFragment())
        }
    }

    private fun setupManagementChips() {
        val chips = listOf(binding.chipTicketPrice, binding.chipShowtime, binding.chipFood)
        chips.forEach { chip ->
            chip.isCheckable = true
            chip.setTextColor(Color.WHITE)
            chip.chipStrokeWidth = 1f
            chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#475569"))
        }

        binding.chipTicketPrice.setOnClickListener {
            selectSection(binding.chipTicketPrice, TicketPriceManagementFragment())
        }
        binding.chipShowtime.setOnClickListener {
            selectSection(binding.chipShowtime, ShowtimeManagementFragment())
        }
        binding.chipFood.setOnClickListener {
            selectSection(binding.chipFood, FoodManagementFragment())
        }
    }

    private fun selectSection(selectedChip: Chip, fragment: Fragment) {
        listOf(binding.chipTicketPrice, binding.chipShowtime, binding.chipFood).forEach { chip ->
            val checked = chip == selectedChip
            chip.isChecked = checked
            chip.chipBackgroundColor = ColorStateList.valueOf(
                Color.parseColor(if (checked) "#3B82F6" else "#334155")
            )
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.managementContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
