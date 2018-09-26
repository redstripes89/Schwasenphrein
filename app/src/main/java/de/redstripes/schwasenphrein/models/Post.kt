package de.redstripes.schwasenphrein.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
class Post {

    var uid: String? = null
    var person: String? = null
    var text: String? = null
    var date: String? = null
    var starCount = 0f
    var stars: MutableMap<String, Float> = HashMap()

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    constructor(uid: String, person: String, text: String, date: String) {
        this.uid = uid
        this.person = person
        this.text = text
        this.date = date
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["uid"] = uid.orEmpty()
        result["person"] = person.orEmpty()
        result["text"] = text.orEmpty()
        result["date"] =  date.orEmpty()
        result["starCount"] = starCount
        result["stars"] = stars

        return result
    }

}
