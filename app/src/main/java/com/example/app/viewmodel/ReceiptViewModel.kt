package com.example.app.viewmodel

import androidx.lifecycle.*
import com.example.app.data.AppDatabase
import com.example.app.data.Receipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiptViewModel(private val db: AppDatabase) : ViewModel() {

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> get() = _total

    init {
        refreshTotal()
    }

    fun add(receipt: Receipt) {
        viewModelScope.launch(Dispatchers.IO) {
            db.receiptDao().insert(receipt)
            refreshTotal()
        }
    }

    private fun refreshTotal() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalValue = db.receiptDao().getTotalAmount()
            _total.postValue(totalValue ?: 0.0)
        }
    }
}
