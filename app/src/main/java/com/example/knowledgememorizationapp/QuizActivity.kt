package com.example.knowledgememorizationapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityQuizBinding
import com.example.knowledgememorizationapp.model.Question
import com.example.knowledgememorizationapp.user.ScoreResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private var questionList = arrayListOf<Question>()
    private var currentQuestion = 0
    private var score = 0
    private var categoryId: String? = null
    private var chancesLeft = 0
    private var countdownTimeInMillis: Long = 5 * 60 * 1000L
    private var timer: CountDownTimer? = null
    private var isTimeEnabled = false

    // Thêm list lưu câu hỏi sai
    private val incorrectQuestions = arrayListOf<Question>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        categoryId = intent.getStringExtra("categoryId")

        binding.categoryimg.setImageResource(R.drawable.default_category_icon)

        val uid = auth.currentUser?.uid
        if (uid != null && categoryId != null) {
            fetchQuestions(uid, categoryId!!)
            fetchUserName()
            fetchSpinInfo()
        } else {
            showToast("Lỗi! Không thể lấy danh mục.")
        }

        showTimePickerDialog()

        listOf(binding.option1, binding.option2, binding.option3, binding.option4).forEach { button ->
            button.setOnClickListener {
                nextQuestionAndScoreUpdate(button.text.toString())
            }
        }
    }

    private fun fetchQuestions(userId: String, categoryId: String) {
        db.child("Users").child(userId).child("quiz_categories").child(categoryId).child("questions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    questionList.clear()
                    snapshot.children.mapNotNullTo(questionList) {
                        it.getValue(Question::class.java)
                    }

                    if (questionList.isNotEmpty()) {
                        if (!isTimeEnabled) loadQuestion() // Nếu không dùng thời gian thì load ngay
                    } else {
                        showToast("Không có câu hỏi nào trong danh mục này!")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("QuizActivity", "Lỗi Firebase: ${error.message}")
                }
            })
    }

    private fun showTimePickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)
        val secondPicker = dialogView.findViewById<NumberPicker>(R.id.secondPicker)

        hourPicker.minValue = 0
        hourPicker.maxValue = 12
        hourPicker.value = 0

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.value = 1

        secondPicker.minValue = 0
        secondPicker.maxValue = 59
        secondPicker.value = 0

        AlertDialog.Builder(this)
            .setTitle("Chọn thời gian làm bài")
            .setView(dialogView)
            .setPositiveButton("Bắt đầu") { _, _ ->
                val totalMillis =
                    (hourPicker.value * 3600 + minutePicker.value * 60 + secondPicker.value) * 1000L

                if (totalMillis < 1000L) {
                    showToast("Thời gian phải lớn hơn 0 giây!")
                } else {
                    countdownTimeInMillis = totalMillis
                    isTimeEnabled = true
                    startCountdownTimer()
                    binding.timerTextView.visibility = View.VISIBLE
                    loadQuestion()
                }
            }
            .setNegativeButton("Hủy") { _, _ ->
                isTimeEnabled = false
                binding.timerTextView.visibility = View.GONE
                loadQuestion()
            }
            .show()
    }

    private fun startCountdownTimer() {
        timer = object : CountDownTimer(countdownTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / 3600000
                val minutes = (millisUntilFinished % 3600000) / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.timerTextView.text = "Thời gian: %02d:%02d:%02d".format(hours, minutes, seconds)
            }

            override fun onFinish() {
                showToast("Hết giờ!")
                showResult()
            }
        }.start()
    }

    private fun loadQuestion() {
        if (currentQuestion >= questionList.size) return

        val question = questionList[currentQuestion]
        binding.question.text = question.question
        binding.option1.text = question.answers.getOrNull(0) ?: ""
        binding.option2.text = question.answers.getOrNull(1) ?: ""
        binding.option3.text = question.answers.getOrNull(2) ?: ""
        binding.option4.text = question.answers.getOrNull(3) ?: ""

        enableOptions()
    }

    private fun nextQuestionAndScoreUpdate(selectedAnswer: String) {
        if (currentQuestion >= questionList.size) return

        val question = questionList[currentQuestion]
        val correctAnswer = question.correctAnswer
        val buttons = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        buttons.forEach { button ->
            button.isEnabled = false
            when (button.text.toString()) {
                correctAnswer -> button.setBackgroundColor(getColor(R.color.correctAnswerColor))
                selectedAnswer -> {
                    if (selectedAnswer != correctAnswer) {
                        button.setBackgroundColor(getColor(R.color.wrongAnswerColor))
                    }
                }
                else -> button.setBackgroundColor(getColor(R.color.defaultOptionColor))
            }
        }

        // Nếu sai thì thêm câu hỏi vào list câu hỏi sai
        if (selectedAnswer != correctAnswer) {
            incorrectQuestions.add(question)
        } else {
            score += 10
        }

        binding.root.postDelayed({
            currentQuestion++
            if (currentQuestion >= questionList.size) {
                showResult()
            } else {
                resetOptions()
                loadQuestion()
            }
        }, 1500)
    }

    private fun showResult() {
        disableOptions()
        timer?.cancel()

        val correctCount = score / 10
        val incorrectCount = questionList.size - correctCount
        val timeUsedMillis = if (isTimeEnabled) {
            countdownTimeInMillis - (binding.timerTextView.text.toString().toMillis())
        } else 0L
        val isWinner = correctCount >= incorrectCount

        saveIncorrectQuestionsToFirebase()

        val intent = Intent(this, ScoreResultActivity::class.java).apply {
            putExtra("correctCount", correctCount)
            putExtra("incorrectCount", incorrectCount)
            putExtra("type", "quiz")
            putExtra("categoryId", categoryId)
            putExtra("timeUsedMillis", timeUsedMillis)
            putExtra("totalTimeMillis", if (isTimeEnabled) countdownTimeInMillis else 0L)
            putExtra("isWinner", isWinner)
        }
        startActivity(intent)
        finish()

        saveQuizResultToFirebase((correctCount * 100) / questionList.size)
    }
    private fun String.toMillis(): Long {
        val parts = this.replace("Thời gian: ", "").split(":").map { it.toInt() }
        return when (parts.size) {
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000L
            else -> 0L
        }
    }

    private fun saveQuizResultToFirebase(percentage: Int) {
        val userId = auth.currentUser?.uid ?: return
        val resultRef = db.child("Users").child(userId).child("quiz_results").push()

        val resultData = mapOf(
            "categoryId" to categoryId,
            "score" to score,
            "totalQuestions" to questionList.size,
            "percentage" to percentage,
            "timestamp" to System.currentTimeMillis()
        )

        resultRef.setValue(resultData)
    }

    // Hàm mới: Lưu câu hỏi sai vào Firebase
    private fun saveIncorrectQuestionsToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val incorrectRef = db.child("Users").child(userId)
            .child("incorrect_questions").child(categoryId!!)

        val incorrectMapList = incorrectQuestions.map { question ->
            mapOf(
                "question" to question.question,
                "answers" to question.answers,
                "correctAnswer" to question.correctAnswer
            )
        }

        incorrectRef.setValue(incorrectMapList).addOnSuccessListener {
            Log.d("QuizActivity", "Lưu câu hỏi sai thành công")
        }.addOnFailureListener {
            Log.e("QuizActivity", "Lỗi khi lưu câu hỏi sai: ${it.message}")
        }
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

    private fun resetOptions() {
        val defaultColor = getColor(R.color.buttonWhite)
        listOf(binding.option1, binding.option2, binding.option3, binding.option4).forEach { button ->
            button.setBackgroundColor(defaultColor)
            button.isEnabled = true
        }
    }

    private fun disableOptions() {
        listOf(binding.option1, binding.option2, binding.option3, binding.option4).forEach { it.isEnabled = false }
    }

    private fun enableOptions() {
        listOf(binding.option1, binding.option2, binding.option3, binding.option4).forEach { it.isEnabled = true }
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
