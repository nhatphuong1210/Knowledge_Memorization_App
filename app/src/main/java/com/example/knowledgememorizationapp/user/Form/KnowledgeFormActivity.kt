package com.example.knowledgememorizationapp.user.Form

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityKnowledgeFormBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class KnowledgeFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKnowledgeFormBinding
    private val database by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var folderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        val isEdit = intent.getBooleanExtra("isEdit", false)
        folderId = intent.getStringExtra("folderId") ?: ""
        val knowledgeId = intent.getStringExtra("knowledgeId")
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""

        if (isEdit) {
            binding.topAppBar.title = "Edit Knowledge"
            binding.saveButton.text = "Update"
            binding.wordEditText.setText(title)
            binding.descriptionEditText.setText(content)
        } else {
            binding.topAppBar.title = "Add Knowledge"
            binding.saveButton.text = "Save"
        }

        binding.saveButton.setOnClickListener {
            val inputTitle = binding.wordEditText.text.toString().trim()
            val inputContent = binding.descriptionEditText.text.toString().trim()

            if (inputTitle.isEmpty() || inputContent.isEmpty()) {
                Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val finalKnowledgeId = knowledgeId ?: database.push().key ?: return@setOnClickListener
            val knowledge = KnowledgeFlashcardModel(folderId, finalKnowledgeId, inputTitle, inputContent)

            if (isEdit) {
                saveKnowledge(userId, folderId, finalKnowledgeId, knowledge, isEdit)
            } else {
                // Chỉ trừ flashcard_limit khi thêm mới
                val limitRef = database.child("Users").child(userId).child("flashcard_limit")
                limitRef.get().addOnSuccessListener { snapshot ->
                    val currentLimit = snapshot.getValue(Int::class.java) ?: 0
                    if (currentLimit <= 0) {
                        Toast.makeText(this, "Bạn đã hết lượt tạo thẻ Flashcard! Hãy quay thưởng để nhận thêm.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Lưu và trừ giới hạn
                    saveKnowledge(userId, folderId, finalKnowledgeId, knowledge, isEdit)
                    limitRef.setValue(currentLimit - 1)
                }.addOnFailureListener {
                    Toast.makeText(this, "Không kiểm tra được giới hạn!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveKnowledge(
        userId: String,
        folderId: String,
        knowledgeId: String,
        knowledge: KnowledgeFlashcardModel,
        isEdit: Boolean
    ) {
        database.child("Users")
            .child(userId)
            .child("flashcard_folders")
            .child(folderId)
            .child("knowledges")
            .child(knowledgeId)
            .setValue(knowledge)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    if (isEdit) "Knowledge updated" else "Knowledge saved",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
