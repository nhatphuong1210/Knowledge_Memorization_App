package com.example.knowledgememorizationapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityAddQuestionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddQuestionBinding
    private var categoryId: String? = null
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("isAdmin", false)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        categoryId = intent.getStringExtra("categoryId")

        binding.saveButton.setOnClickListener {
            val questionText = binding.questionInput.text.toString()
            val answers = listOf(
                binding.optionA.text.toString(),
                binding.optionB.text.toString(),
                binding.optionC.text.toString(),
                binding.optionD.text.toString()
            )
            val correctAnswer = binding.correctAnswer.text.toString()

            if (questionText.isNotEmpty() && !categoryId.isNullOrEmpty()) {
                val questionData = mapOf(
                    "question" to questionText,
                    "answers" to answers,
                    "correctAnswer" to correctAnswer
                )

                val questionRef = if (isAdmin) {
                    FirebaseDatabase.getInstance().getReference("QuizCategories")
                        .child(categoryId!!).child("questions")
                } else {
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(uid).child("quiz_categories")
                        .child(categoryId!!).child("questions")
                }
                if (isAdmin) {
                    // Admin được phép tạo không giới hạn
                    questionRef.push().setValue(questionData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Lỗi khi thêm câu hỏi.", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    // Người dùng thường bị giới hạn
                    val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
                    userRef.child("quiz_limit").get().addOnSuccessListener { snapshot ->
                        val currentLimit = snapshot.getValue(Int::class.java) ?: 0
                        if (currentLimit > 0) {
                            userRef.child("quiz_limit").setValue(currentLimit - 1)

                            questionRef.push().setValue(questionData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Lỗi khi thêm câu hỏi.", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(this, "Bạn đã hết lượt tạo câu hỏi quiz! Hãy quay thưởng để nhận thêm.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập câu hỏi đầy đủ!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backButton.setOnClickListener {
            finish() // Đóng Activity hiện tại để quay về trang trước
        }
    }
}