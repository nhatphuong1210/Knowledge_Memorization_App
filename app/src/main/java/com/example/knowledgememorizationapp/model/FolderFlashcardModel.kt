package com.example.knowledgememorizationapp.model

data class FolderFlashcardModel(
    var folderId: String = "",
    var folderName: String = "",
    var totalWords: String = "0" // ⚡ Thêm giá trị mặc định
) {
    constructor() : this("", "", "0") // ⚡ Constructor rỗng bắt buộc cho Firebase
}