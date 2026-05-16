package com.example.film.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.film.model.FoodItem


object Common {

    val APIKEY : String = ""

    fun initDate(daysAhead: Int = 7) : List<String>{

        val dateList = mutableListOf<String>()

        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("E", Locale("vi"))

        val calendar = Calendar.getInstance()

        repeat(daysAhead + 1) {
            val dayOfWeek = dayFormat.format(calendar.time)
            val date = dateFormat.format(calendar.time)

            dateList.add("$dayOfWeek - $date")

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList

    }


    fun extractDateKey(displayDate: String): String {
        return normalizeDateKey(displayDate)
    }

    fun legacyDateKey(displayDate: String): String {
        return displayDate.substringAfter(" - ", displayDate).trim()
    }

    fun dateKeyCandidates(displayDate: String): List<String> {
        return listOf(extractDateKey(displayDate), legacyDateKey(displayDate))
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun isSameDateKey(storedDateKey: String, selectedDisplayDate: String): Boolean {
        if (storedDateKey.isBlank() || selectedDisplayDate.isBlank()) return false
        return dateKeyCandidates(selectedDisplayDate).contains(storedDateKey) ||
                normalizeDateKey(storedDateKey) == extractDateKey(selectedDisplayDate)
    }

    fun normalizeDateKey(dateValue: String): String {
        val value = legacyDateKey(dateValue)
        if (Regex("""\d{4}-\d{2}-\d{2}""").matches(value)) return value

        val match = Regex("""(\d{1,2})/(\d{1,2})""").find(value) ?: return value
        val day = match.groupValues[1].toIntOrNull() ?: return value
        val month = match.groupValues[2].toIntOrNull() ?: return value

        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.before(startOfToday)) {
            calendar.add(Calendar.YEAR, 1)
        }

        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    fun buildShowKey(movieName: String, cinemaName: String, displayDate: String, time: String): String {
        val rawKey = "${movieName}_${cinemaName}_${legacyDateKey(displayDate)}_${time}"
        return rawKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    }

    fun initEveningShowTimes(): List<String> {
        return listOf(
            "06:00 - 08:00",
            "08:00 - 10:00",
            "10:00 - 12:00",
            "12:00 - 14:00",
            "14:00 - 16:00",
            "16:00 - 18:00",
            "18:00 - 20:00",
            "20:00 - 22:00",
            "22:00 - 00:00",
            "00:00 - 02:00",
            "02:00 - 04:00"
        )
    }

    fun initCinema() : List<String> {
        return listOf(
            "CGV Vincom Royal City",
            "CGV Aeon Mall Long Biên",
            "Lotte Cinema Ba Đình",
            "Beta Cinema Mỹ Đình",
            "BHD Star Phạm Ngọc Thạch",
            "Galaxy Cinema Nguyễn Du - HN",
            "Mega GS Cao Thắng - HN",
            "Cinestar Quốc Gia - HN"
        )
    }

    fun initCinemaWithAddress(): List<Pair<String, String>> {
        return listOf(
            "CGV Vincom Royal City" to "72A Nguyễn Trãi, Thanh Xuân, Hà Nội",
            "CGV Aeon Mall Long Biên" to "27 Cổ Linh, Long Biên, Hà Nội",
            "Lotte Cinema Ba Đình" to "54 Liễu Giai, Ba Đình, Hà Nội",
            "Beta Cinema Mỹ Đình" to "Tòa Golden Palace, Mễ Trì, Nam Từ Liêm, Hà Nội",
            "BHD Star Phạm Ngọc Thạch" to "Vincom Center, 2 Phạm Ngọc Thạch, Đống Đa, Hà Nội",
            "Galaxy Cinema Nguyễn Du - HN" to "116 Nguyễn Du, Hai Bà Trưng, Hà Nội",
            "Mega GS Cao Thắng - HN" to "19 Cao Thắng, Hai Bà Trưng, Hà Nội",
            "Cinestar Quốc Gia - HN" to "87 Láng Hạ, Đống Đa, Hà Nội"
        )
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

    fun initFood(): List<FoodItem> {
        return listOf(
            FoodItem(1, "Bắp rang bơ (Lớn)", 55000, "https://images.unsplash.com/photo-1585735024697-15e4f4e1c489?w=400"),
            FoodItem(2, "Bắp rang bơ (Vừa)", 45000, "https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=400"),
            FoodItem(3, "Coca Cola", 30000, "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=400"),
            FoodItem(4, "Pepsi", 30000, "https://images.unsplash.com/photo-1553456558-aff63285bdd1?w=400"),
            FoodItem(5, "Nước suối", 15000, "https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400"),
            FoodItem(6, "Trà sữa", 40000, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400"),
            FoodItem(7, "Combo Bắp + Nước", 65000, "https://images.unsplash.com/photo-1635805737707-575885ab0820?w=400"),
            FoodItem(8, "Nachos phô mai", 50000, "https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?w=400"),
            FoodItem(9, "Hotdog", 45000, "https://images.unsplash.com/photo-1612392062126-21009b3e8567?w=400"),
            FoodItem(10, "Xúc xích nướng", 35000, "https://images.unsplash.com/photo-1587536849024-daaa4a417b16?w=400")
        )
    }

    fun removeAccents(s: String): String {
        val temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .replace(Regex("[^a-zA-Z0-9]"), " ")
            .trim()
            .replace(Regex("\\s+"), "_")
    }

}
