package com.example.knowledgememorizationapp.model

data class UserCategoryModel(
    var catid: String = "",
    var catName: String = "",
    var imageUrl: String = ""
) {
    constructor() : this("", "", "")
}