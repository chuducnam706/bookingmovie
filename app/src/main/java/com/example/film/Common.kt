package com.example.film

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object Common {

    fun initDate() : List<String>{

        val dateList = mutableListOf<String>()

        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("E", Locale("vi"))

        val calendar = Calendar.getInstance()

        for (i in 0..7) {
            val dayOfWeek = dayFormat.format(calendar.time)
            val date = dateFormat.format(calendar.time)

            dateList.add("$dayOfWeek - $date")

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList

    }


    fun generateShowTimes(isToday: Boolean, startHour: Int, endHour: Int, stepHour: Int): List<String> {
        val result: MutableList<String> = ArrayList<String>()

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)

        var i = startHour
        while (i < endHour) {
            val from = i
            val to = i + stepHour

            val fromStr = String.format("%02d:00", from % 24)
            val toStr = String.format("%02d:00", to % 24)

            // lọc suất đã qua nếu là hôm nay
            if (!isToday || to > currentHour) {
                result.add(fromStr + " - " + toStr)
            }
            i += stepHour
        }

        return result
    }

}