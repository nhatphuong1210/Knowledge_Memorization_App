package com.example.knowledgememorizationapp.admin.Form

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityKnowledgeFormBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.google.firebase.database.FirebaseDatabase

class AdminKnowledgeFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKnowledgeFormBinding
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    private lateinit var folderId: String
    private var isEdit: Boolean = false
    private var knowledgeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Nhận dữ liệu từ Intent
        folderId = intent.getStringExtra("folderId") ?: ""
        isEdit = intent.getBooleanExtra("isEdit", false)

        if (isEdit) {
            binding.topAppBar.title = "Edit Knowledge"
            knowledgeId = intent.getStringExtra("knowledgeId")
            binding.wordEditText.setText(intent.getStringExtra("title"))
            binding.descriptionEditText.setText(intent.getStringExtra("content"))
        } else {
            binding.topAppBar.title = "Add Knowledge"
        }

        binding.topAppBar.setNavigationOnClickListener { finish() }

        binding.saveButton.setOnClickListener {
            val title = binding.wordEditText.text.toString().trim()
            val content = binding.descriptionEditText.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (folderId.isNotEmpty()) {
                val id = knowledgeId ?: database.push().key ?: return@setOnClickListener
                val knowledge = KnowledgeFlashcardModel(
                    folderId = folderId,
                    knowledgeId = id,
                    title = title,
                    content = content
                )

                database.child("Flashcard_Folders_Admin")
                    .child(folderId)
                    .child("knowledges")
                    .child(id)
                    .setValue(knowledge)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Knowledge saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Missing folder ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
