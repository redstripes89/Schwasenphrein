package de.redstripes.schwasenphrein.models

import android.text.TextUtils
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import de.redstripes.schwasenphrein.helpers.Helper
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import java.util.*

@IgnoreExtraProperties
class Post {

    var uid: String? = null
    var person: String? = null
    var text: String? = null
    var date: String? = null
    var starCount = 0f
    var stars: MutableMap<String, Float> = HashMap()
    var year: Int? = null
    var month: Int? = null
    var monthName: String? = null
    var formattedDate: String? = null

    @Suppress("unused")
    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    constructor(uid: String, person: String, text: String, date: String) {
        this.uid = uid
        this.person = person
        this.text = text
        this.date = date
    }

    fun parseDate() {
        if (date == null)
            return

        val internalDate = Helper.dateFromString(date!!)

        year = internalDate.year
        month = internalDate.monthValue
        monthName = internalDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        formattedDate = internalDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["uid"] = uid.orEmpty()
        result["person"] = person.orEmpty()
        result["text"] = text.orEmpty()
        result["date"] = date.orEmpty()
        result["starCount"] = starCount
        result["stars"] = stars

        return result
    }
}
