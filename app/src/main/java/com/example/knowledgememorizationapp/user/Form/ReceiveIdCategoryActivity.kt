package com.example.knowledgememorizationapp.user.Form

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityReceiveIdCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ReceiveIdCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiveIdCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveIdCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReceive.setOnClickListener {
            val catid = binding.edtCategoryId.text.toString().trim()
            if (catid.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã danh mục", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val allUsersRef = FirebaseDatabase.getInstance().getReference("Users")
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            allUsersRef.get().addOnSuccessListener { usersSnapshot ->
                var found = false
                for (userSnap in usersSnapshot.children) {
                    val categorySnap = userSnap.child("quiz_categories").child(catid)
                    if (categorySnap.exists()) {
                        val fullCategoryData = categorySnap.value
                        val targetRef = allUsersRef.child(currentUid).child("quiz_categories").child(catid)
                        targetRef.setValue(fullCategoryData).addOnSuccessListener {
                            Toast.makeText(this, "Nhận danh mục thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Lỗi ghi dữ liệu!", Toast.LENGTH_SHORT).show()
                        }
                        found = true
                        break
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Không tìm thấy danh mục với mã đã nhập!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Lỗi khi duyệt dữ liệu người dùng!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

}