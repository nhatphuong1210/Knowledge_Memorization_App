package com.example.knowledgememorizationapp.user.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.knowledgememorizationapp.databinding.FragmentOptionBinding
import com.example.knowledgememorizationapp.user.HomeActivity
import com.example.knowledgememorizationapp.user.Home_Flashcard_Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OptionFragment : Fragment() {
    private var _binding: FragmentOptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    // Biến lưu lượt quay còn lại, sẽ cập nhật sau khi gọi hàm fetchSpinInfo
    private var chancesLeft = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        fetchUserName()
        fetchSpinInfo()  // Thay thế fetchSpinChances bằng hàm fetchSpinInfo để cập nhật lượt quay chính xác

        binding.btnQuiz.setOnClickListener {
            startActivity(Intent(requireContext(), HomeActivity::class.java))
        }

        binding.btnFlashcard.setOnClickListener {
            startActivity(Intent(requireContext(), Home_Flashcard_Activity::class.java))
        }
    }

    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.child("Users").child(uid).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.getValue(String::class.java) ?: "Người dùng"
                    _binding?.let {
                        it.Name.text = userName
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _binding?.let {
                        it.Name.text = "Không tìm thấy tên"
                    }
                }
            })
    }

    // Thay đổi hàm này thành fetchSpinInfo với logic cập nhật lượt quay theo ngày
    private fun fetchSpinInfo() {
        val uid = auth.currentUser?.uid ?: return
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val userSpinRef = db.child("SpinHistory").child(uid)

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
        }.addOnFailureListener {
            chancesLeft = 1
            updateChanceDisplay()
        }
    }

    private fun updateChanceDisplay() {
        _binding?.let {
            it.CoinWithdrawal1.text = chancesLeft.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
