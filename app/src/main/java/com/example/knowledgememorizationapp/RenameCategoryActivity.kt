package com.example.knowledgememorizationapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityRenameCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RenameCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRenameCategoryBinding
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    private var categoryId: String? = null
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRenameCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("isAdmin", false)
        categoryId = intent.getStringExtra("categoryId")

        binding.updateCategoryButton.setOnClickListener {
            renameCategory()
        }
    }

    private fun renameCategory() {
        val newCategoryName = binding.categoryNameInput.text.toString()

        userId?.let { uid ->
            categoryId?.let { catId ->
                if (newCategoryName.isNotEmpty()) {
                    val categoryRef = if (isAdmin) {
                        FirebaseDatabase.getInstance().getReference("QuizCategories")
                            .child(catId).child("catName")
                    } else {
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(uid).child("quiz_categories")
                            .child(catId).child("catName")
                    }

                    categoryRef.setValue(newCategoryName)
                        .addOnSuccessListener { showToast("Cập nhật tên danh mục thành công!") }
                        .addOnFailureListener { showToast("Lỗi khi cập nhật danh mục.") }
                } else {
                    showToast("Vui lòng nhập tên danh mục hợp lệ!")
                }
            } ?: showToast("Lỗi! Không tìm thấy ID danh mục.")
        } ?: showToast("Lỗi! Không tìm thấy ID người dùng.")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
