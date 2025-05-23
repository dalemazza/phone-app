package com.example.app.viewmodel

    import android.content.Context
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import com.example.app.data.AppDatabase

    class ReceiptViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ReceiptViewModel::class.java)) {
    val db = AppDatabase.getInstance(context)
    return ReceiptViewModel(db) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
    }
    }
