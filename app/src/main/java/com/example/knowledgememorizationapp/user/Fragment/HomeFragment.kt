package com.example.knowledgememorizationapp.user.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.knowledgememorizationapp.*
import com.example.knowledgememorizationapp.adaptor.UserCategoryAdapter
import com.example.knowledgememorizationapp.databinding.FragmentHomeBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel
import com.example.knowledgememorizationapp.user.Form.CategoryFormActivity
import com.example.knowledgememorizationapp.user.Form.ReceiveIdCategoryActivity
import com.example.knowledgememorizationapp.user.ReceiveSharedCategoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var userAdapter: UserCategoryAdapter
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private var chancesLeft = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        fetchUserCategories()
        fetchUserName()
        fetchSpinChances()
        fetchSpinInfo()
        binding.fabAdd.setOnClickListener { showPopupMenu() }
    }

    private fun fetchUserCategories() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.child("Users").child(uid).child("quiz_categories")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCategories = snapshot.children.mapNotNull { it.getValue(CategoryAdminModel::class.java) }
                userAdapter.setData(userCategories)
                binding.userCategoryRecyclerView.visibility = if (userCategories.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.userCategoryRecyclerView.visibility = View.GONE
            }
        })
    }

    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.child("Users").child(uid).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java) ?: "Người dùng"
                binding.Name.text = userName
            }

            override fun onCancelled(error: DatabaseError) {
                binding.Name.text = "Không tìm thấy tên"
            }
        })
    }

    private fun fetchSpinChances() {
        val uid = auth.currentUser?.uid ?: return
        val spinRef = db.child("SpinHistory").child(uid).child("spinChancesLeft")

        spinRef.get().addOnSuccessListener { snapshot ->
            val chancesLeft = snapshot.getValue(Int::class.java) ?: 0
            binding.CoinWithdrawal1.text = chancesLeft.toString()
        }.addOnFailureListener {
            binding.CoinWithdrawal1.text = "0"
        }
    }

    private fun setupAdapter() {
        userAdapter = UserCategoryAdapter(emptyList(), requireActivity(), ::onEditClick, ::onDeleteClick)
        binding.userCategoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.userCategoryRecyclerView.adapter = userAdapter
    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.fabAdd)
        popupMenu.menu.add("Tạo danh mục mới")
        popupMenu.menu.add("Nhận Category đã được chia sẻ")
        popupMenu.menu.add("Nhận Category từ mã") // ✅ thêm dòng này

        popupMenu.setOnMenuItemClickListener {
            when (it.title.toString()) {
                "Tạo danh mục mới" -> startActivity(Intent(requireContext(), CategoryFormActivity::class.java))
                "Nhận Category đã được chia sẻ" -> startActivity(Intent(requireContext(), ReceiveSharedCategoryActivity::class.java))
                "Nhận Category từ mã" -> startActivity(Intent(requireContext(), ReceiveIdCategoryActivity::class.java)) // ✅ xử lý mục mới
            }
            true
        }
        popupMenu.show()
    }


    private fun onEditClick(category: CategoryAdminModel) {
        val intent = Intent(requireContext(), RenameCategoryActivity::class.java).apply {
            putExtra("categoryId", category.catid)
            putExtra("categoryName", category.catName)
        }
        startActivity(intent)
    }

    private fun onDeleteClick(category: CategoryAdminModel) {
        val uid = auth.currentUser?.uid ?: return
        db.child("Users").child(uid).child("quiz_categories").child(category.catid ?: "")
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Xóa danh mục thành công!", Toast.LENGTH_SHORT).show()
                fetchUserCategories()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi xóa danh mục.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchSpinInfo() {
        val uid = auth.currentUser?.uid ?: return
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val userSpinRef = db.child("SpinHistory").child(uid)  // Sửa ở đây

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
        binding.CoinWithdrawal1.text = chancesLeft.toString()
    }
}
