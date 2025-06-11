package com.example.knowledgememorizationapp.admin.flashcard.knowledgeListFlashcard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.admin.Form.AdminKnowledgeFormActivity
import com.example.knowledgememorizationapp.admin.flashcard.card.AdminCardFlipActivity
import com.example.knowledgememorizationapp.databinding.ActivityKnowledgeListBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.example.knowledgememorizationapp.user.flashcard.card.CardFlipActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminKnowledgeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKnowledgeListBinding
    private lateinit var database: DatabaseReference
    private lateinit var knowledgeList: ArrayList<KnowledgeFlashcardModel>
    private var folderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        folderId = intent.getStringExtra("folderId") // Lấy folderId từ Intent
        val folderName = intent.getStringExtra("folderName") ?: "Knowledge List"
        binding.topAppBar.title = folderName

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnReview.setOnClickListener {
            // Lưu folderId và folderName vào SharedPreferences
            val prefs = getSharedPreferences("AdminFolderPreferences", MODE_PRIVATE)
            prefs.edit()
                .putString("currentFolderId", folderId)
                .putString("currentFolderName", binding.topAppBar.title.toString())
                .apply()

            // Mở màn hình flip card
            startActivity(Intent(this, AdminCardFlipActivity::class.java))
        }

        knowledgeList = ArrayList()
        binding.knowledgeListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Khởi tạo Firebase reference
        database = FirebaseDatabase.getInstance()
            .getReference("Flashcard_Folders_Admin")
            .child(folderId!!)
            .child("knowledges")

        loadKnowledgeFromFirebase()

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AdminKnowledgeFormActivity::class.java)
            intent.putExtra("folderId", folderId)
            startActivity(intent)
        }
    }

    private fun loadKnowledgeFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                knowledgeList.clear()
                for (itemSnap in snapshot.children) {
                    val title = itemSnap.child("title").getValue(String::class.java)
                    val content = itemSnap.child("content").getValue(String::class.java)
                    val id = itemSnap.key

                    if (title != null && content != null && id != null) {
                        knowledgeList.add(KnowledgeFlashcardModel(folderId!!, id, title, content))
                    }
                }

                if (knowledgeList.isEmpty()) {
                    binding.emptyListMessage.visibility = View.VISIBLE
                } else {
                    binding.emptyListMessage.visibility = View.GONE
                }

                binding.knowledgeListRecyclerView.adapter = AdminKnowledgeListAdapter(
                    knowledgeList,
                    this@AdminKnowledgeListActivity,
                    onEditClick = { knowledge ->
                        val intent = Intent(this@AdminKnowledgeListActivity, AdminKnowledgeFormActivity::class.java)
                        intent.putExtra("isEdit", true)
                        intent.putExtra("folderId", knowledge.folderId)
                        intent.putExtra("knowledgeId", knowledge.knowledgeId)
                        intent.putExtra("title", knowledge.title)
                        intent.putExtra("content", knowledge.content)
                        startActivity(intent)
                    },
                    onDeleteClick = { knowledge ->
                        val ref = database.child(knowledge.knowledgeId)
                        ref.removeValue().addOnSuccessListener {
                            knowledgeList.remove(knowledge)
                            binding.knowledgeListRecyclerView.adapter?.notifyDataSetChanged()

                            if (knowledgeList.isEmpty()) {
                                binding.emptyListMessage.visibility = View.VISIBLE
                            }
                        }
                    }
                )
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}