package com.example.knowledgememorizationapp.user.flashcard.card
import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityCardFlipBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel
import com.example.knowledgememorizationapp.user.ScoreResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CardFlipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardFlipBinding
    private lateinit var setRightOut: AnimatorSet
    private lateinit var setLeftIn: AnimatorSet
    private var isBackVisible = false
    private var currentIndex = 0
    private var correctCount = 0
    private var incorrectCount = 0
    private var knowledgeList = ArrayList<KnowledgeFlashcardModel>()
    private var isFlipping = false

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
        val scale = resources.displayMetrics.density * 4000 // giảm độ sâu
        binding.cardFront.cameraDistance = scale
        binding.cardBack.cameraDistance = scale
    }

    private fun loadAnimations() {
        setRightOut = AnimatorInflater.loadAnimator(this, R.animator.card_out_animation) as AnimatorSet
        setLeftIn = AnimatorInflater.loadAnimator(this, R.animator.card_in_animation) as AnimatorSet
    }

    private fun flipCard() {
        if (!isBackVisible) {
            binding.cardBack.visibility = View.VISIBLE
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
            // Sau khi lật về mặt trước thì ẩn mặt sau để tránh bị đè
            Handler(Looper.getMainLooper()).postDelayed({
                binding.cardBack.visibility = View.GONE
            }, 300) // delay đúng thời gian animation
        }
    }


    private fun checkIfFlippedCard() {
        if (isBackVisible) {
            flipCard()
        }
    }
    private fun getIsPublicFolder(): Boolean {
        val prefs = getSharedPreferences("FolderPreferences", MODE_PRIVATE)
        return prefs.getBoolean("isPublicFolder", false)
    }

    private fun loadKnowledgeData() {
        val folderId = getFolderId()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val isPublicFolder = getIsPublicFolder()

        val ref = if (isPublicFolder) {
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

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                knowledgeList.clear()
                for (snap in snapshot.children) {
                    val flashcard = snap.getValue(KnowledgeFlashcardModel::class.java)
                    if (flashcard != null) {
                        flashcard.folderId = folderId
                        flashcard.knowledgeId = snap.key ?: ""
                        knowledgeList.add(flashcard)
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
        val current = knowledgeList[currentIndex]
        markAsLearned(current) // <-- Ghi nhận đã học
// Delay 100ms để tránh xung đột Firebase và UI
        Handler(Looper.getMainLooper()).postDelayed({
            markAsLearned(current)
        }, 100)

        if (currentIndex < knowledgeList.size - 1) {
            if (isBackVisible) {
                flipToFrontThenNext()
            } else {
                currentIndex++
                showKnowledge()
            }
        } else {
            showScores()
        }
    }


    private fun flipToFrontThenNext() {
        if (isFlipping) return
        isFlipping = true

        setRightOut.setTarget(binding.cardBack)
        setLeftIn.setTarget(binding.cardFront)

        setLeftIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                setLeftIn.removeAllListeners()
                isBackVisible = false
                currentIndex++
                showKnowledge()
                isFlipping = false
            }
        })

        setRightOut.start()
        setLeftIn.start()

    }



    private fun showScores() {
        val isWinner = correctCount > incorrectCount
        val intent = Intent(this, ScoreResultActivity::class.java)
        intent.putExtra("correctCount", correctCount)
        intent.putExtra("incorrectCount", incorrectCount)
        intent.putExtra("isWinner", isWinner)
        startActivity(intent)
        finish()
    }

    private fun getFolderId(): String {
        val prefs = getSharedPreferences("FolderPreferences", MODE_PRIVATE)
        return prefs.getString("currentFolderId", "") ?: ""
    }

    private fun getFolderName(): String {
        val prefs = getSharedPreferences("FolderPreferences", MODE_PRIVATE)
        return prefs.getString("currentFolderName", "Flashcards") ?: "Flashcards"
    }
    private fun markAsLearned(knowledge: KnowledgeFlashcardModel) {
        val isPublic = getIsPublicFolder()
        val ref = if (isPublic) {
            FirebaseDatabase.getInstance()
                .getReference("Flashcard_Folders_Admin")
                .child(knowledge.folderId)
                .child("knowledges")
                .child(knowledge.knowledgeId)
        } else {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("flashcard_folders")
                .child(knowledge.folderId)
                .child("knowledges")
                .child(knowledge.knowledgeId)
        }

        val updates = mapOf<String, Any>(
            "learned" to true,
            "doneTimestamp" to System.currentTimeMillis()
        )
        ref.updateChildren(updates)
    }

}


