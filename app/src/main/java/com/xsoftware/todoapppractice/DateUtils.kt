package com.xsoftware.todoapppractice

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatDate(date: Date): String {
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        return when {
            isSameDay(date, today.time) -> "Today"
            isSameDay(date, tomorrow.time) -> "Tomorrow"
            else -> dateFormat.format(date)
        }
    }

    fun parseDate(date: String): Date? {
        return when (date) {
            "Today" -> Calendar.getInstance().time
            "Tomorrow" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
            else -> dateFormat.parse(date)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }
}