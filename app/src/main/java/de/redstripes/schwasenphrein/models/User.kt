package de.redstripes.schwasenphrein.models

import androidx.annotation.Keep

@Keep
class User {

    var username: String?=null

    constructor(){
        // used for database snapshot
    }

    constructor(username: String?) {
        this.username = username
    }
}