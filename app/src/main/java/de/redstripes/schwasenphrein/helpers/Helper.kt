package de.redstripes.schwasenphrein.helpers

import android.annotation.SuppressLint
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class Helper {
    companion object {
        @SuppressLint("SimpleDateFormat")
        fun dateToString(date: LocalDateTime?): String {
            return DateTimeFormatter.ISO_DATE.format(date)
        }

        fun dateFromString(dateString: String): LocalDateTime {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE)
        }
    }
}