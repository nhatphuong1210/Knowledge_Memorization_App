package com.example.knowledgememorizationapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityQuestionDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionDetailBinding
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    private val categoryId: String? by lazy { intent.getStringExtra("categoryId") }
    private val questionId: String? by lazy { intent.getStringExtra("questionId") }
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("isAdmin", false)

        Log.d("QuestionDetail", "categoryId: $categoryId, questionId: $questionId")

        if (categoryId.isNullOrBlank() || questionId.isNullOrBlank()) {
            showToast("Lỗi! Không tìm thấy dữ liệu câu hỏi.")
            finish()
            return
        }

        // Hiển thị dữ liệu được truyền từ Intent
        binding.questionText.setText(intent.getStringExtra("questionText") ?: "")
        binding.optionA.setText(intent.getStringExtra("optionA") ?: "")
        binding.optionB.setText(intent.getStringExtra("optionB") ?: "")
        binding.optionC.setText(intent.getStringExtra("optionC") ?: "")
        binding.optionD.setText(intent.getStringExtra("optionD") ?: "")
        binding.correctAnswer.setText(intent.getStringExtra("correctAnswer") ?: "")

        // Gán sự kiện cập nhật và xóa
        binding.updateButton.setOnClickListener { updateQuestion() }
        binding.deleteButton.setOnClickListener { deleteQuestion() }
    }

    private fun updateQuestion() {
        userId?.let { uid ->
            categoryId?.let { catId ->
                questionId?.let { quesId ->
                    val updatedData = mapOf(
                        "question" to binding.questionText.text.toString(),
                        "answers" to listOf(
                            binding.optionA.text.toString(),
                            binding.optionB.text.toString(),
                            binding.optionC.text.toString(),
                            binding.optionD.text.toString()
                        ),
                        "correctAnswer" to binding.correctAnswer.text.toString()
                    )

                    val ref = if (isAdmin) {
                        FirebaseDatabase.getInstance().getReference("QuizCategories")
                            .child(catId).child("questions").child(quesId)
                    } else {
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(uid).child("quiz_categories")
                            .child(catId).child("questions").child(quesId)
                    }

                    ref.updateChildren(updatedData)
                        .addOnSuccessListener {
                            showToast("Cập nhật câu hỏi thành công!")
                        }
                        .addOnFailureListener {
                            showToast("Lỗi khi cập nhật câu hỏi.")
                        }
                }
            }
        }
    }

    private fun deleteQuestion() {
        userId?.let { uid ->
            categoryId?.let { catId ->
                questionId?.let { quesId ->
                    val ref = if (isAdmin) {
                        FirebaseDatabase.getInstance().getReference("QuizCategories")
                            .child(catId).child("questions").child(quesId)
                    } else {
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(uid).child("quiz_categories")
                            .child(catId).child("questions").child(quesId)
                    }

                    ref.removeValue()
                        .addOnSuccessListener {
                            showToast("Xóa câu hỏi thành công!")
                            finish()
                        }
                        .addOnFailureListener {
                            showToast("Lỗi khi xóa câu hỏi.")
                        }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}