package com.example.knowledgememorizationapp.user
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.QuizActivity
import com.example.knowledgememorizationapp.user.RetryIncorrectQuestionsActivity
import com.example.knowledgememorizationapp.user.flashcard.card.CardFlipActivity

import com.example.knowledgememorizationapp.databinding.ActivityScoreResultBinding

class ScoreResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Lấy dữ liệu từ intent
        val correctCount = intent.getIntExtra("correctCount", 0)
        val incorrectCount = intent.getIntExtra("incorrectCount", 0)
        val type = intent.getStringExtra("type") ?: "flashcard" // hoặc "quiz"
        val categoryId = intent.getStringExtra("categoryId") // 👈 lấy categoryId nếu là quiz
        if (type == "quiz" && incorrectCount > 0) {
            binding.retryIncorrectButton.visibility = View.VISIBLE
        } else {
            binding.retryIncorrectButton.visibility = View.GONE
        }
        // Gán dữ liệu
        binding.correctCount.text = correctCount.toString()
        binding.incorrectCount.text = incorrectCount.toString()

        // Tùy tiêu đề
        title = when (type) {
            "quiz" -> "Quiz Results"
            "flashcard" -> getFolderNameFromSharedPreferences()
            else -> "Results"
        }

        // Tùy hành động nút
        binding.reviewAgainButton.setOnClickListener {
            val intent = when (type) {
                "quiz" -> {
                    val quizIntent = Intent(this, QuizActivity::class.java)
                    quizIntent.putExtra("categoryId", categoryId) // 👈 truyền lại categoryId
                    quizIntent
                }
                else -> Intent(this, CardFlipActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
        binding.retryIncorrectButton.setOnClickListener {
            val intent = Intent(this, RetryIncorrectQuestionsActivity::class.java)
            intent.putExtra("categoryId", categoryId)
            startActivity(intent)
        }

        val timeUsedMillis = intent.getLongExtra("timeUsedMillis", -1L)
        val totalTimeMillis = intent.getLongExtra("totalTimeMillis", -1L)

        if (timeUsedMillis >= 0 && totalTimeMillis > 0) {
            val timeUsedFormatted = formatMillisToTime(timeUsedMillis)
            val totalTimeFormatted = formatMillisToTime(totalTimeMillis)
            binding.timeUsedTextView.text = "Thời gian làm bài: $timeUsedFormatted / $totalTimeFormatted"
        } else {
            binding.timeUsedTextView.text = ""
        }



        val isWinner = intent.getBooleanExtra("isWinner", false)

// Hiển thị layout Winner hoặc Sorry
        if (isWinner) {
            binding.Winner.visibility = View.VISIBLE
            binding.Sorry.visibility = View.GONE
        } else {
            binding.Winner.visibility = View.GONE
            binding.Sorry.visibility = View.VISIBLE
        }


    }
    private fun formatMillisToTime(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun getFolderNameFromSharedPreferences(): String {
        val prefs = getSharedPreferences("folder_preferences", MODE_PRIVATE)
        return prefs.getString("current_folder_name", "Knowledges") ?: "Knowledges"
    }
}