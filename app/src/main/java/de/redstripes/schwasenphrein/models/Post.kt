package de.redstripes.schwasenphrein.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
class Post {

    var uid: String? = null
    var person: String? = null
    var text: String? = null
    var colorIndex: Int?= null
    var starCount = 0
    var stars: HashMap<String, Boolean> = HashMap()

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    constructor(uid: String, title: String, text: String, colorIndex: Int) {
        this.uid = uid
        this.person = title
        this.text = text
        this.colorIndex = colorIndex
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["uid"] = uid.orEmpty()
        result["person"] = person.orEmpty()
        result["text"] = text.orEmpty()
        result["colorIndex"] = colorIndex ?: 0
        result["starCount"] = starCount
        result["stars"] = stars

        return result
    }

}
