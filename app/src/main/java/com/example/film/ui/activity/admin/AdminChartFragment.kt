package com.example.film.ui.activity.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.film.databinding.FragmentAdminChartBinding
import com.example.film.viewmodel.AdminViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AdminChartFragment : Fragment() {

    private var _binding: FragmentAdminChartBinding? = null
    private val binding get() = _binding!!

    private val adminViewModel: AdminViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    private var chartType = 0 // 0 for Revenue, 1 for Volume

    companion object {
        fun newInstance(position: Int): AdminChartFragment {
            val fragment = AdminChartFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chartType = arguments?.getInt("position") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (chartType == 0) {
            adminViewModel.revenueData.observe(viewLifecycleOwner) { data ->
                updateChart(data.values, data.labels, data.title)
            }
        } else {
            adminViewModel.volumeData.observe(viewLifecycleOwner) { data ->
                updateChart(data.values, data.labels, data.title)
            }
        }
    }

    fun updateChart(data: List<Float>, labels: List<String>, title: String) {
        if (_binding == null) return
        
        val entries = data.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value)
        }
        val dataSet = BarDataSet(entries, title)
        dataSet.color = Color.parseColor("#3B82F6")
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f
        
        // Hide zero values
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) String.format("%,d", value.toLong()) else ""
            }
        }

        val barData = BarData(dataSet)
        binding.barChart.data = barData

        // Customize Chart
        binding.barChart.description.isEnabled = false
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.animateY(1000)
        binding.barChart.legend.textColor = Color.WHITE
        binding.barChart.setExtraOffsets(0f, 10f, 0f, 10f) // Adjust offsets

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelCount = 10 // Show roughly 10 labels to avoid overlapping
        xAxis.granularity = 1f

        val yAxisLeft = binding.barChart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.parseColor("#334155")
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.setDrawAxisLine(true)

        binding.barChart.axisRight.isEnabled = false
        
        // Limit visible range to 10 items to make it readable
        if (labels.size > 10) {
            binding.barChart.setVisibleXRangeMaximum(10f)
            binding.barChart.moveViewToX(0f)
        }

        // Handle conflict with ViewPager2
        binding.barChart.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN,
                android.view.MotionEvent.ACTION_MOVE -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
