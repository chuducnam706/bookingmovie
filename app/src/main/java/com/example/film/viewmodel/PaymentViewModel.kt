package com.example.film.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.film.model.VnPayModel
import com.example.film.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {

    private val repository = PaymentRepository()

    private val _payment =
        MutableLiveData<VnPayModel>()

    val payment: LiveData<VnPayModel>
        get() = _payment

    fun createPayment(
        amount: Long,
        orderInfo: String,
        orderId: String
    ) {

        viewModelScope.launch {

            try {
                val repository = repository.createPayment(amount, orderInfo, orderId)
                _payment.value = repository
            } catch (e: Exception) {
                Log.d("ErrorPayment", e.toString())
            }

        }


    }

}