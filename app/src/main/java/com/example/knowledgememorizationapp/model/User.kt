package com.example.knowledgememorizationapp.model
data class User(
    var uid: String? = null,
    var name: String? = null,
    var age: Int? = null,
    var email: String? = null,
    var role: String? = null
) {
    constructor() : this(null, null, null, null, null)
}
