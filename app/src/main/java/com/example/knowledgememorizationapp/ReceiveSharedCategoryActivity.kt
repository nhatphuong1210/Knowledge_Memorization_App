package com.example.knowledgememorizationapp.user

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.knowledgememorizationapp.adaptor.CommonCategoryAdapter
import com.example.knowledgememorizationapp.databinding.ActivityReceiveSharedCategoryBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReceiveSharedCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiveSharedCategoryBinding
    private val dbRef = FirebaseDatabase.getInstance().getReference("QuizCategories")

    private val userRef: DatabaseReference by lazy {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(uid).child("quiz_categories")
        } else {
            throw IllegalStateException("Người dùng chưa đăng nhập.")
        }
    }

    private val categoryList = mutableListOf<CategoryAdminModel>()
    private lateinit var adapter: CommonCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveSharedCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        fetchCategories()
    }

    private fun setupRecyclerView() {
        adapter = CommonCategoryAdapter(categoryList) { selectedCategory ->
            checkAndSaveCategory(selectedCategory)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = categoryList.filter {
                    it.catName.contains(newText.orEmpty(), ignoreCase = true)
                }
                adapter.setData(filteredList)
                return true
            }
        })
    }

    private fun fetchCategories() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (categorySnap in snapshot.children) {
                    val category = categorySnap.getValue(CategoryAdminModel::class.java)
                    if (category != null) {
                        categoryList.add(category)
                    }
                }
                Log.d("ReceiveCategory", "Số danh mục nhận được: ${categoryList.size}")
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReceiveSharedCategoryActivity, "Lỗi tải danh mục!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAndSaveCategory(category: CategoryAdminModel) {
        userRef.child(category.catid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Danh mục đã tồn tại trong danh sách của bạn!", Toast.LENGTH_SHORT).show()
            } else {
                saveCategoryToUser(category.catid)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi kiểm tra danh mục: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCategoryToUser(categoryId: String) {
        // Lấy toàn bộ danh mục gốc từ QuizCategories (bao gồm cả questions)
        dbRef.child(categoryId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                userRef.child(categoryId).setValue(snapshot.value)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nhận danh mục thành công!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi khi lưu danh mục: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Không tìm thấy danh mục!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi tải danh mục từ server: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
