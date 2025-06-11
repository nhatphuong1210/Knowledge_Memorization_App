package com.example.knowledgememorizationapp.user.Form


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityReceiveFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ReceiveFlashcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiveFlashcardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReceive.setOnClickListener {
            val folderId = binding.edtFolderId.text.toString().trim()
            if (folderId.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã thư mục", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val allUsersRef = FirebaseDatabase.getInstance().getReference("Users")
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            allUsersRef.get().addOnSuccessListener { usersSnapshot ->
                var found = false
                for (userSnap in usersSnapshot.children) {
                    val folderSnap = userSnap.child("flashcard_folders").child(folderId)
                    if (folderSnap.exists()) {
                        val fullFolderData = folderSnap.value
                        val targetRef = allUsersRef.child(currentUid).child("flashcard_folders").child(folderId)
                        targetRef.setValue(fullFolderData).addOnSuccessListener {
                            Toast.makeText(this, "Nhận thư mục thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Lỗi ghi dữ liệu!", Toast.LENGTH_SHORT).show()
                        }
                        found = true
                        break
                    }
                }
                if (!found) {
                    Toast.makeText(this, "Không tìm thấy thư mục với mã đã nhập!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Lỗi duyệt dữ liệu người dùng!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}