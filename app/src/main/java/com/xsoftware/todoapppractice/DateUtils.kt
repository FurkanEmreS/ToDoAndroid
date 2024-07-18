package com.xsoftware.todoapppractice

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val formattedDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    fun formatTaskDate(date: String): String {
        val taskDate = dateFormat.parse(date)
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        return when {
            isSameDay(taskDate, today.time) -> "Today"
            isSameDay(taskDate, tomorrow.time) -> "Tomorrow"
            else -> formattedDateFormat.format(taskDate)
        }
    }

    fun parseTaskDate(date: String): Date {
        return when (date) {
            "Today" -> Calendar.getInstance().time
            "Tomorrow" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
            else -> try {
                dateFormat.parse(date)
            } catch (e: Exception) {
                formattedDateFormat.parse(date)
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }
}