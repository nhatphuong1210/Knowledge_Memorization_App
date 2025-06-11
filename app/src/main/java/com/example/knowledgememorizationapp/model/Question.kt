package com.example.knowledgememorizationapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    var question: String = "",
    var answers: List<String> = listOf("", "", "", ""), // Đảm bảo danh sách có đúng 4 phần tử
    var correctAnswer: String = ""
): Parcelable {
    constructor() : this("", listOf("", "", "", ""), "")
}
