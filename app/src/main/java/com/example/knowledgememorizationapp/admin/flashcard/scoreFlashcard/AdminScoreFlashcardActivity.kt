package com.example.knowledgememorizationapp.admin.flashcard.scoreFlashcard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.R

import com.example.knowledgememorizationapp.admin.flashcard.card.AdminCardFlipActivity
import com.example.knowledgememorizationapp.databinding.ActivityScoreResultBinding

class AdminScoreFlashcardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getFolderNameFromSharedPreferences()

        val correctCount = intent.getIntExtra("correctCount", 0)
        val incorrectCount = intent.getIntExtra("incorrectCount", 0)
        val isWinner = intent.getBooleanExtra("isWinner", false)
        binding.correctCount.text = correctCount.toString()
        binding.incorrectCount.text = incorrectCount.toString()
        if (isWinner) {
            binding.Winner.visibility = View.VISIBLE
            binding.Sorry.visibility = View.GONE
        } else {
            binding.Winner.visibility = View.GONE
            binding.Sorry.visibility = View.VISIBLE
        }

        binding.reviewAgainButton.setOnClickListener {
            val intent = Intent(this, AdminCardFlipActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getFolderNameFromSharedPreferences(): String {
        val prefs = getSharedPreferences("AdminFolderPreferences", MODE_PRIVATE)
        return prefs.getString("currentFolderName", "Knowledges") ?: "Knowledges"
    }
}