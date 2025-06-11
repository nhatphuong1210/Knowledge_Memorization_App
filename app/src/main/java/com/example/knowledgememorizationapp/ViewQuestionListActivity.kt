package com.example.knowledgememorizationapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.adaptor.QuestionAdapter
import com.example.knowledgememorizationapp.databinding.ActivityViewQuestionListBinding
import com.example.knowledgememorizationapp.model.QuestionModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewQuestionListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewQuestionListBinding
    private val db = FirebaseDatabase.getInstance().getReference("Users")
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
    private val categoryId by lazy { intent.getStringExtra("categoryId").orEmpty() }
    private val questionList = ArrayList<QuestionModel>()
    private lateinit var questionAdapter: QuestionAdapter
    private val isAdmin by lazy { intent.getBooleanExtra("isAdmin", false) }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityViewQuestionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        Log.d("DebugCheck", "userId: $userId, categoryId: $categoryId") // Kiểm tra log

        if (userId.isEmpty()) {
            showToast("Lỗi! Không tìm thấy ID người dùng.")
            finish()
            return
        }

        if (categoryId.isEmpty()) {
            showToast("Không tìm thấy danh mục câu hỏi!")
            finish()
            return
        }

        setupRecyclerView()
        fetchQuestions()
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter(questionList, categoryId, this@ViewQuestionListActivity)
        binding.recyclerViewQuestions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewQuestions.adapter = questionAdapter
    }

    private fun fetchQuestions() {
        val questionRef = if (isAdmin) {
            FirebaseDatabase.getInstance().getReference("QuizCategories")
                .child(categoryId).child("questions")
        } else {
            db.child(userId).child("quiz_categories").child(categoryId).child("questions")
        }

        questionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionList.clear()

                for (questionSnap in snapshot.children) {
                    val question = questionSnap.getValue(QuestionModel::class.java)
                    val questionId = questionSnap.key

                    if (question != null && questionId != null) {
                        question.questionId = questionId // ⚠️ Bạn cần cho phép setter nếu đang dùng var
                        questionList.add(question)
                    }
                }

                Log.d("FirebaseCheck", "Số câu hỏi tải được: ${questionList.size}")
                questionAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Lỗi khi tải câu hỏi: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}