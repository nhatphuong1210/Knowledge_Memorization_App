package com.example.knowledgememorizationapp.model

data class KnowledgeFlashcardModel(
    var folderId: String = "",
    var knowledgeId: String = "",
    var title: String = "",
    var content: String = "",
    var learned: Boolean = false,   // ✅ đơn giản nhất
    var doneTimestamp: Long? = null
)
