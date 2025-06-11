package com.example.knowledgememorizationapp.user.Fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.knowledgememorizationapp.databinding.FragmentSpinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SpinFragment : Fragment() {

    private lateinit var _binding: FragmentSpinBinding
    private val binding get() = _binding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private var chancesLeft = 0
    private var isSpinning = false
    private var lastAngle = 0f

    private val itemTitles = listOf(
        "Try Again", "+5 thẻ flashcard", "Try Again", "+5 lượt tạo câu hỏi quiz",
        "Try Again", "+10 thẻ flashcard", "Try Again", "+10 lượt tạo câu hỏi quiz"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpinBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        fetchUserName()
        fetchSpinInfo()

        binding.Spin.setOnClickListener {
            if (isSpinning) return@setOnClickListener
            if (chancesLeft <= 0) {
                Toast.makeText(requireContext(), "Hết lượt quay!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            chancesLeft--
            updateChanceDisplay()
            spinWheel()
        }

        return binding.root
    }

    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.getReference("Users").child(uid).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.getValue(String::class.java) ?: "Người dùng"
                    binding.Name.text = userName
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.Name.text = "Không tìm thấy tên"
                }
            })
    }

    private fun fetchSpinInfo() {
        val uid = auth.currentUser?.uid ?: return
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val userSpinRef = db.getReference("SpinHistory").child(uid)

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
            // ✅ Cập nhật spinChancesLeft mới lên Firebase
            userSpinRef.child("spinChancesLeft").setValue(chancesLeft)

        }.addOnFailureListener {
            chancesLeft = 1
            updateChanceDisplay()
            // ✅ Cập nhật mặc định nếu chưa có dữ liệu
            userSpinRef.setValue(
                mapOf(
                    "spinChancesLeft" to chancesLeft,
                    "lastSpinDate" to todayDate
                )
            )

        }
    }

    private fun updateChanceDisplay() {
        binding.CoinWithdrawal1.text = chancesLeft.toString()
        binding.chancesLeftText.text = "Cơ hội còn lại: $chancesLeft"
    }


    private fun spinWheel() {
        isSpinning = true

        val randomDegree = Random().nextInt(360)
        val targetDegree = 5 * 360f + randomDegree

        val animator = ObjectAnimator.ofFloat(binding.wheel, View.ROTATION, lastAngle, lastAngle + targetDegree)
        animator.duration = 3500
        animator.interpolator = DecelerateInterpolator()

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                lastAngle = (lastAngle + targetDegree) % 360f
                handleSpinResult()
                isSpinning = false
            }
        })

        animator.start()
    }

    private fun handleSpinResult() {
        val itemCount = itemTitles.size
        val degreesPerItem = 360f / itemCount
        val pointerOffset = 338f // vì mũi tên nằm bên phải
        val correctedAngle = (360f - (lastAngle + pointerOffset) % 360f) % 360f
        val index = (correctedAngle / degreesPerItem).toInt() % itemCount
        val result = itemTitles[index]

        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()

        updateSpinHistory(result)
        updateUserLimit(result)
    }

    private fun updateSpinHistory(result: String) {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val mainData = mapOf(
            "lastSpinDate" to today,
            "spinChancesLeft" to chancesLeft
        )

        val spinRef = db.getReference("SpinHistory").child(uid)
        spinRef.updateChildren(mainData)

        // 🟡 Đây là phần bạn đang thiếu
        val historyItem = mapOf(
            "lastSpinDate" to today,
            "reward" to result
        )

        // ✅ Ghi lịch sử vào spinList
        spinRef.child("spinList").push().setValue(historyItem)
    }



    private fun updateUserLimit(result: String) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.getReference("Users").child(uid)

        when {
            result.contains("thẻ flashcard") -> {
                val amount = if (result.contains("+10")) 10 else 5
                userRef.child("flashcard_limit").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val current = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = current + amount
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
                })
            }
            result.contains("tạo câu hỏi quiz") -> {
                val amount = if (result.contains("+10")) 10 else 5
                userRef.child("quiz_limit").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val current = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = current + amount
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
                })
            }
        }
    }
}
