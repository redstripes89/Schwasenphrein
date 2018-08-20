package de.redstripes.schwasenphrein.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
class Post {

    var uid: String? = null
    var author: String? = null
    var title: String? = null
    var body: String? = null
    var starCount = 0
    var stars: HashMap<String, Boolean> = HashMap()

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    constructor(uid: String, author: String, title: String, body: String) {
        this.uid = uid
        this.author = author
        this.title = title
        this.body = body
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["uid"] = uid.orEmpty()
        result["author"] = author.orEmpty()
        result["title"] = title.orEmpty()
        result["body"] = body.orEmpty()
        result["starCount"] = starCount
        result["stars"] = stars

        return result
    }

}
