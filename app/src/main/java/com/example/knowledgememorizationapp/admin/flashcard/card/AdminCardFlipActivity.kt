package com.example.knowledgememorizationapp.admin.flashcard.card

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.admin.flashcard.scoreFlashcard.AdminScoreFlashcardActivity
import com.example.knowledgememorizationapp.databinding.ActivityCardFlipBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.google.firebase.database.*

class AdminCardFlipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardFlipBinding
    private lateinit var setRightOut: AnimatorSet
    private lateinit var setLeftIn: AnimatorSet
    private var isBackVisible = false
    private var currentIndex = 0
    private var knowledgeList = ArrayList<KnowledgeFlashcardModel>()
    private var correctCount = 0
    private var incorrectCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardFlipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getFolderName()

        loadKnowledgeData()
        loadAnimations()
        changeCameraDistance()

        binding.btnCorrect.setOnClickListener {
            correctCount++
            showNextOrFinish()
        }

        binding.btnIncorrect.setOnClickListener {
            incorrectCount++
            showNextOrFinish()
        }


        binding.cardFront.setOnClickListener { flipCard() }
        binding.cardBack.setOnClickListener { flipCard() }
    }

    private fun changeCameraDistance() {
        val scale = resources.displayMetrics.density * 8000
        binding.cardFront.cameraDistance = scale
        binding.cardBack.cameraDistance = scale
    }

    private fun loadAnimations() {
        setRightOut = AnimatorInflater.loadAnimator(this, R.animator.card_out_animation) as AnimatorSet
        setLeftIn = AnimatorInflater.loadAnimator(this, R.animator.card_in_animation) as AnimatorSet
    }

    private fun flipCard() {
        if (!isBackVisible) {
            setRightOut.setTarget(binding.cardFront)
            setLeftIn.setTarget(binding.cardBack)
            setRightOut.start()
            setLeftIn.start()
            isBackVisible = true
        } else {
            setRightOut.setTarget(binding.cardBack)
            setLeftIn.setTarget(binding.cardFront)
            setRightOut.start()
            setLeftIn.start()
            isBackVisible = false
        }
    }

    private fun loadKnowledgeData() {
        val folderId = getFolderId()
        val ref = FirebaseDatabase.getInstance()
            .getReference("Flashcard_Folders_Admin")
            .child(folderId!!)
            .child("knowledges")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                knowledgeList.clear()
                for (snap in snapshot.children) {
                    val title = snap.child("title").getValue(String::class.java) ?: ""
                    val content = snap.child("content").getValue(String::class.java) ?: ""
                    val knowledgeId = snap.key ?: ""

                    if (title.isNotEmpty() && content.isNotEmpty()) {
                        knowledgeList.add(
                            KnowledgeFlashcardModel(
                                folderId = folderId,
                                knowledgeId = knowledgeId,
                                title = title,
                                content = content
                            )
                        )
                    }
                }
                if (knowledgeList.isNotEmpty()) {
                    showKnowledge()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showKnowledge() {
        val current = knowledgeList[currentIndex]
        binding.includeFront.cardTextFront.text = current.title
        binding.includeBack.cardTextBack.text = current.content
        showIndex()
    }

    private fun showIndex() {
        val indexText = getString(R.string.index_format, currentIndex + 1, knowledgeList.size)
        binding.wordIndex.text = indexText
    }

    private fun showNextOrFinish() {
        if (currentIndex < knowledgeList.size - 1) {
            if (isBackVisible) {
                flipToFrontThenNext()
            } else {
                currentIndex++
                showKnowledge()
            }
        } else {
            showScores()
// Kết thúc khi duyệt xong
        }
    }
    private fun showScores() {
        val isWinner = correctCount > incorrectCount
        val intent = Intent(this, AdminScoreFlashcardActivity::class.java)
        intent.putExtra("correctCount", correctCount)
        intent.putExtra("incorrectCount", incorrectCount)
        intent.putExtra("isWinner", isWinner) // ✅ Đã truyền đúng
        startActivity(intent)
        finish()
    }



    private fun flipToFrontThenNext() {
        setRightOut.setTarget(binding.cardBack)
        setLeftIn.setTarget(binding.cardFront)

        setLeftIn.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                setLeftIn.removeAllListeners()
                isBackVisible = false
                currentIndex++
                showKnowledge()
            }
        })

        setRightOut.start()
        setLeftIn.start()
    }


    private fun getFolderId(): String {
        val prefs = getSharedPreferences("AdminFolderPreferences", MODE_PRIVATE)
        return prefs.getString("currentFolderId", "") ?: ""
    }

    private fun getFolderName(): String {
        val prefs = getSharedPreferences("AdminFolderPreferences", MODE_PRIVATE)
        return prefs.getString("currentFolderName", "Flashcards") ?: "Flashcards"
    }
}