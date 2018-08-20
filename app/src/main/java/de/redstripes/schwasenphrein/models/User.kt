package de.redstripes.schwasenphrein.models

class User {

    var username: String?=null

    constructor(){
        // used for database snapshot
    }

    constructor(username: String?) {
        this.username = username
    }
}