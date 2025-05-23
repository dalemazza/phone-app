package com.example.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun parse(dateStr: String): Date? =
        try { formatter.parse(dateStr) } catch (e: Exception) { null }

    fun format(date: Date): String = formatter.format(date)
}
