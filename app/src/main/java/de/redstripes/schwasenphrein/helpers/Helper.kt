package de.redstripes.schwasenphrein.helpers

import android.annotation.SuppressLint
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class Helper {
    companion object {
        @SuppressLint("SimpleDateFormat")
        fun dateToString(date: LocalDate?): String {
            return DateTimeFormatter.ISO_DATE.format(date)
        }

        fun dateFromString(dateString: String): LocalDate {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
        }
    }
}