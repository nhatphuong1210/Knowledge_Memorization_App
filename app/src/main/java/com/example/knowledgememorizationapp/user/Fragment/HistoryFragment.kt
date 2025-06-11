package com.example.knowledgememorizationapp.user.Fragment

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.adaptor.HistoryAdaptor
import com.example.knowledgememorizationapp.databinding.FragmentHistoryBinding
import com.example.knowledgememorizationapp.model.HistoryModelClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {
    private var binding: FragmentHistoryBinding? = null
    private val listHistory = ArrayList<HistoryModelClass>()
    private lateinit var spinHistoryAdaptor: HistoryAdaptor
    private lateinit var dbRef: DatabaseReference

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private var chancesLeft = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSpinHistory()
        loadUserInfo()
        fetchSpinInfo()  // Gọi lấy info lượt spin để hiển thị số lượt spin còn lại
    }

    private fun setupRecyclerView() {
        spinHistoryAdaptor = HistoryAdaptor(listHistory)
        binding?.recyclerViewSpinHistory?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = spinHistoryAdaptor
            setHasFixedSize(true)
            addItemDecoration(MarginItemDecoration(1)) // Giảm khoảng cách giữa các mục còn 1px
        }
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.child("Users").child(uid)

        userRef.child("name").get().addOnSuccessListener { snapshot ->
            val userName = snapshot.getValue(String::class.java) ?: "Người dùng"
            binding?.Name?.text = userName
        }
    }

    private fun loadSpinHistory() {
        val userId = auth.currentUser?.uid ?: return
        dbRef = db.child("SpinHistory").child(userId).child("spinList")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listHistory.clear()
                for (item in snapshot.children) {
                    val date = item.child("lastSpinDate").getValue(String::class.java)
                    val reward = item.child("reward").getValue(String::class.java)
                    if (date != null && reward != null) {
                        listHistory.add(HistoryModelClass(date, reward))
                    }
                }
                spinHistoryAdaptor.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Hàm lấy thông tin lượt quay giống trong HomeFragment
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
        binding?.CoinWithdrawal1?.text = chancesLeft.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.top = spaceHeight / 2
            outRect.bottom = spaceHeight / 2
        }
    }
}
