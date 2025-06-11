package com.example.knowledgememorizationapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityDeleteCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DeleteCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteCategoryBinding
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    private var categoryId: String? = null
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("isAdmin", false)
        categoryId = intent.getStringExtra("categoryId")

        binding.deleteButton.setOnClickListener {
            deleteCategory()
        }
    }

    private fun deleteCategory() {
        userId?.let { uid ->
            categoryId?.let { catId ->
                val categoryRef = if (isAdmin) {
                    FirebaseDatabase.getInstance().getReference("QuizCategories").child(catId)
                } else {
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(uid).child("quiz_categories").child(catId)
                }

                categoryRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        categoryRef.removeValue()
                            .addOnSuccessListener { showToast("Xóa danh mục thành công!") }
                            .addOnFailureListener { showToast("Lỗi khi xóa danh mục.") }
                    } else {
                        showToast("Danh mục không tồn tại!")
                    }
                }.addOnFailureListener {
                    showToast("Lỗi khi kiểm tra danh mục.")
                }
            } ?: showToast("Lỗi! Không tìm thấy ID danh mục.")
        } ?: showToast("Lỗi! Không tìm thấy ID người dùng.")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
