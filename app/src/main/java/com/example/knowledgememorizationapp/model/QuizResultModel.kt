package com.example.knowledgememorizationapp.model

data class QuizResultModel(
    var categoryId: String = "",
    var correctCount: Int = 0,
    var wrongCount: Int = 0,
    var total: Int = 0,
    var timestamp: Long = 0L
) {
    constructor() : this("", 0, 0, 0, 0L)
}