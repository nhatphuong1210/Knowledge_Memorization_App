package com.example.knowledgememorizationapp.user.Form

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityShowQuizIdBinding

class ShowQuizIdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowQuizIdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowQuizIdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val quizId = intent.getStringExtra("categoryId") ?: "Không có mã"
        val quizName = intent.getStringExtra("categoryName") ?: "Không rõ"

        binding.quizIdText.text = "Mã chia sẻ: $quizId"
        binding.quizNameText.text = "Tên danh mục: $quizName"

        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Quiz ID", quizId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Đã sao chép mã!", Toast.LENGTH_SHORT).show()
        }
    }
}