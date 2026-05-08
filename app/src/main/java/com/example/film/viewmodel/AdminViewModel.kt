package com.example.film.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ChartData(
    val values: List<Float>,
    val labels: List<String>,
    val title: String
)

class AdminViewModel : ViewModel() {

    private val _revenueData = MutableLiveData<ChartData>()
    val revenueData: LiveData<ChartData> = _revenueData

    private val _volumeData = MutableLiveData<ChartData>()
    val volumeData: LiveData<ChartData> = _volumeData

    fun updateRevenue(values: List<Float>, labels: List<String>, title: String) {
        _revenueData.value = ChartData(values, labels, title)
    }

    fun updateVolume(values: List<Float>, labels: List<String>, title: String) {
        _volumeData.value = ChartData(values, labels, title)
    }
}
