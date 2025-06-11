package com.example.knowledgememorizationapp.model

data class QuestionModel(
    var questionId: String = "",  // ID câu hỏi
    var categoryId: String = "",  // ID danh mục chứa câu hỏi
    var question: String = "",
    var answers: List<String> = listOf("", "", "", ""), // Đảm bảo danh sách không bị null
    var correctAnswer: String = ""
) {
    constructor() : this("", "", "", listOf("", "", "", ""), "")
}
