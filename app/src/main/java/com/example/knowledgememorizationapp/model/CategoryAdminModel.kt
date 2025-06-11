package com.example.knowledgememorizationapp.model

data class CategoryAdminModel(
    var catid: String = "",          // ID duy nhất của danh mục (Firebase push key)
    var catName: String = "",     // Tên danh mục
    var imageUrl: String = ""     // Link ảnh lưu trên Firebase Storage hoặc URL ảnh
)
