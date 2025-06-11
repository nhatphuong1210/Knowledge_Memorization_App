package com.example.knowledgememorizationapp.user.flashcard.knowledgeListFlashcard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityKnowledgeListBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.example.knowledgememorizationapp.user.Form.KnowledgeFormActivity
import com.example.knowledgememorizationapp.user.flashcard.card.CardFlipActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class KnowledgeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKnowledgeListBinding
    private lateinit var database: DatabaseReference
    private lateinit var knowledgeList: ArrayList<KnowledgeFlashcardModel>
    private var isPublicFolder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val folderId = getFolderIdFromSharedPreferences()
        val folderName = getFolderNameFromSharedPreferences()
        binding.topAppBar.title = folderName
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        isPublicFolder = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE)
            .getBoolean("isPublicFolder", false)

        // Ẩn nút thêm nếu là folder public
        if (isPublicFolder) {
            binding.fabAdd.visibility = View.GONE
        }

        database = if (isPublicFolder) {
            FirebaseDatabase.getInstance()
                .getReference("Flashcard_Folders_Admin")
                .child(folderId)
                .child("knowledges")
        } else {
            FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("flashcard_folders")
                .child(folderId)
                .child("knowledges")
        }

        knowledgeList = ArrayList()
        binding.knowledgeListRecyclerView.layoutManager = LinearLayoutManager(this)

        loadKnowledgeFromFirebase(folderId)

        binding.btnReview.setOnClickListener {
            startActivity(Intent(this, CardFlipActivity::class.java))
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, KnowledgeFormActivity::class.java)
            intent.putExtra("folderId", folderId)
            startActivity(intent)
        }
    }

    private fun loadKnowledgeFromFirebase(folderId: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                knowledgeList.clear()
                for (itemSnap in snapshot.children) {
                    val title = itemSnap.child("title").getValue(String::class.java)
                    val content = itemSnap.child("content").getValue(String::class.java)
                    val id = itemSnap.key

                    if (title != null && content != null && id != null) {
                        knowledgeList.add(KnowledgeFlashcardModel(folderId, id, title, content))
                    }
                }

                if (knowledgeList.isEmpty()) {
                    binding.btnReview.visibility = View.GONE
                    binding.emptyListMessage.visibility = View.VISIBLE
                } else {
                    binding.btnReview.visibility = View.VISIBLE
                    binding.emptyListMessage.visibility = View.GONE
                }

                // Adapter xử lý logic sửa/xóa dựa trên quyền
                binding.knowledgeListRecyclerView.adapter = KnowledgeListAdapter(
                    knowledgeList,
                    this@KnowledgeListActivity,
                    onEditClick = { knowledge ->
                        if (!isPublicFolder) {
                            val intent = Intent(this@KnowledgeListActivity, KnowledgeFormActivity::class.java)
                            intent.putExtra("isEdit", true)
                            intent.putExtra("folderId", knowledge.folderId)
                            intent.putExtra("knowledgeId", knowledge.knowledgeId)
                            intent.putExtra("title", knowledge.title)
                            intent.putExtra("content", knowledge.content)
                            startActivity(intent)
                        }
                    },
                    onDeleteClick = { knowledge ->
                        if (!isPublicFolder) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@KnowledgeListAdapter
                            val ref = FirebaseDatabase.getInstance()
                                .getReference("Users")
                                .child(userId)
                                .child("flashcard_folders")
                                .child(knowledge.folderId)
                                .child("knowledges")
                                .child(knowledge.knowledgeId)

                            ref.removeValue().addOnSuccessListener {
                                knowledgeList.remove(knowledge)
                                binding.knowledgeListRecyclerView.adapter?.notifyDataSetChanged()

                                if (knowledgeList.isEmpty()) {
                                    binding.btnReview.visibility = View.GONE
                                    binding.emptyListMessage.visibility = View.VISIBLE
                                }
                            }
                        }
                    }, isPublicFolder = isPublicFolder

                )
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getFolderIdFromSharedPreferences(): String {
        val prefs = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE)
        return prefs.getString(getString(R.string.current_folder_id), "") ?: ""
    }

    private fun getFolderNameFromSharedPreferences(): String {
        val prefs = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE)
        return prefs.getString(getString(R.string.current_folder_name), "Knowledges") ?: "Knowledges"
    }
}
