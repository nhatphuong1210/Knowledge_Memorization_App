package com.example.knowledgememorizationapp.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityQuizBinding
import com.example.knowledgememorizationapp.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RetryIncorrectQuestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var databaseRef: DatabaseReference
    private val incorrectQuestions = mutableListOf<Question>()
    private var currentIndex = 0
    private var chancesLeft = 0
    private var correctAnswers = 0
    private val incorrectAnswersList = arrayListOf<Question>()
    private val correctAnsweredList = arrayListOf<Question>()


    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference


        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        categoryId = intent.getStringExtra("categoryId")  // Gán cho biến thành viên

        if (categoryId == null) {
            Toast.makeText(this, "Không tìm thấy categoryId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(uid).child("incorrect_questions").child(categoryId!!)


        fetchUserName()
        fetchSpinInfo()
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                incorrectQuestions.clear()
                for (questionSnap in snapshot.children) {
                    val question = questionSnap.getValue(Question::class.java)
                    question?.let { incorrectQuestions.add(it) }
                }

                if (incorrectQuestions.isNotEmpty()) {
                    displayQuestion(incorrectQuestions[currentIndex])
                } else {
                    Toast.makeText(this@RetryIncorrectQuestionsActivity, "Không có câu hỏi sai!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RetryIncorrectQuestionsActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })

        // Gán sự kiện click cho 4 option
        binding.option1.setOnClickListener { checkAnswer(binding.option1.text.toString()) }
        binding.option2.setOnClickListener { checkAnswer(binding.option2.text.toString()) }
        binding.option3.setOnClickListener { checkAnswer(binding.option3.text.toString()) }
        binding.option4.setOnClickListener { checkAnswer(binding.option4.text.toString()) }
    }

    private fun displayQuestion(question: Question) {
        binding.question.text = question.question
        if (question.answers.size == 4) {
            binding.option1.text = question.answers[0]
            binding.option2.text = question.answers[1]
            binding.option3.text = question.answers[2]
            binding.option4.text = question.answers[3]
        }

        resetOptions()
        enableOptions()
    }

    private fun checkAnswer(selectedAnswer: String) {
        val currentQuestion = incorrectQuestions[currentIndex]
        val correctAnswer = currentQuestion.correctAnswer
        val buttons = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        buttons.forEach { btn ->
            btn.isEnabled = false
            when (btn.text.toString()) {
                correctAnswer -> btn.setBackgroundColor(ContextCompat.getColor(this, R.color.correctAnswerColor))
                selectedAnswer -> {
                    if (selectedAnswer != correctAnswer) {
                        btn.setBackgroundColor(ContextCompat.getColor(this, R.color.wrongAnswerColor))
                    }
                }
                else -> btn.setBackgroundColor(ContextCompat.getColor(this, R.color.defaultOptionColor))
            }
        }

        // Ghi nhận kết quả
        if (selectedAnswer == correctAnswer) {
            correctAnswers++
            correctAnsweredList.add(currentQuestion) // ✅ thêm dòng này
        } else {
            incorrectAnswersList.add(currentQuestion)
        }

        binding.root.postDelayed({
            currentIndex++
            if (currentIndex < incorrectQuestions.size) {
                displayQuestion(incorrectQuestions[currentIndex])
            } else {
                // Chuyển sang màn hình kết quả
                showResult()
            }
        }, 1500)
    }


    private fun resetOptions() {
        val defaultColor = ContextCompat.getColor(this, R.color.defaultOptionColor)
        binding.option1.setBackgroundColor(defaultColor)
        binding.option2.setBackgroundColor(defaultColor)
        binding.option3.setBackgroundColor(defaultColor)
        binding.option4.setBackgroundColor(defaultColor)
    }

    private fun enableOptions() {
        binding.option1.isEnabled = true
        binding.option2.isEnabled = true
        binding.option3.isEnabled = true
        binding.option4.isEnabled = true
    }

    private fun disableOptions() {
        binding.option1.isEnabled = false
        binding.option2.isEnabled = false
        binding.option3.isEnabled = false
        binding.option4.isEnabled = false
    }
    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.child("Users").child(uid).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.Name.text = snapshot.getValue(String::class.java) ?: "Người dùng"
            }

            override fun onCancelled(error: DatabaseError) {
                binding.Name.text = "Không tìm thấy tên"
            }
        })
    }

    private fun fetchSpinInfo() {
        val uid = auth.currentUser?.uid ?: return
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val userSpinRef = db.child("SpinHistory").child(uid)

        userSpinRef.get().addOnSuccessListener { snapshot ->
            val lastSpinDate = snapshot.child("lastSpinDate").getValue(String::class.java)
            val oldChances = snapshot.child("spinChancesLeft").getValue(Int::class.java) ?: 0

            val lastDate = lastSpinDate?.let { sdf.parse(it) }
            val today = sdf.parse(todayDate)

            val daysSkipped = if (lastDate != null && today != null) {
                val diff = today.time - lastDate.time
                (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            } else {
                1
            }

            chancesLeft = oldChances + daysSkipped
            updateChanceDisplay()
        }.addOnFailureListener {
            chancesLeft = 1
            updateChanceDisplay()
        }
    }

    private fun updateChanceDisplay() {
        binding.CoinWithdrawal1.text = chancesLeft.toString()
    }
    private fun showResult() {
        val intent = Intent(this, ScoreResultActivity::class.java).apply {
            putExtra("correctCount", correctAnswers) // ✅ Đổi key
            putExtra("incorrectCount", incorrectQuestions.size - correctAnswers) // ✅ Tính sai
            putExtra("type", "quiz")
            putExtra("categoryId", categoryId)
            //putExtra("timeUsedMillis", -1L)
            //putExtra("totalTimeMillis", -1L)
            putExtra("isWinner", correctAnswers >= (incorrectQuestions.size + 1) / 2)

        }
        val uid = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().reference
            .child("Users").child(uid).child("incorrect_questions").child(categoryId!!)

// ✅ Xoá từng câu đúng khỏi Firebase
        for (question in correctAnsweredList) {
            val query = ref.orderByChild("question").equalTo(question.question)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()  // 🔥 Xoá câu đúng
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        startActivity(intent)

        finish()
    }


}
