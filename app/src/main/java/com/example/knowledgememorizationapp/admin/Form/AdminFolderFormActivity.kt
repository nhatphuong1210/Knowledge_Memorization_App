package com.example.knowledgememorizationapp.admin.Form

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityAdminFolderFormBinding
import com.example.knowledgememorizationapp.databinding.ActivityFolderFormFlashcardBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.google.firebase.database.FirebaseDatabase

class AdminFolderFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFolderFormFlashcardBinding
    private val dbRef = FirebaseDatabase.getInstance().getReference("Flashcard_Folders_Admin")
    private var folderIdToUpdate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderFormFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Kiểm tra Intent để xác định nếu đang chỉnh sửa folder
        folderIdToUpdate = intent.getStringExtra("folderId")
        if (folderIdToUpdate != null) {
            binding.topAppBar.title = "Edit Folder"
            binding.submitFolderButton.text = "Update"
            val currentFolderName = intent.getStringExtra("folderName")
            binding.folderNameEditText.setText(currentFolderName)
        } else {
            binding.topAppBar.title = "Create Folder"
        }

        // Sự kiện cho nút Create/Update Folder
        binding.submitFolderButton.setOnClickListener {
            val folderName = binding.folderNameEditText.text.toString().trim()

            if (folderName.isEmpty()) {
                Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                if (folderIdToUpdate != null) {
                    // Cập nhật folder
                    val updates = mapOf("folderName" to folderName)
                    dbRef.child(folderIdToUpdate!!).updateChildren(updates)
                    Toast.makeText(this, "Folder updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Tạo folder mới
                    val folderId = dbRef.push().key ?: return@setOnClickListener
                    val folder = FolderFlashcardModel(folderId, folderName)
                    dbRef.child(folderId).setValue(folder)
                    Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show()
                }
                finish() // Quay lại danh sách folder sau khi tạo/xuất bản
            }
        }
    }

}