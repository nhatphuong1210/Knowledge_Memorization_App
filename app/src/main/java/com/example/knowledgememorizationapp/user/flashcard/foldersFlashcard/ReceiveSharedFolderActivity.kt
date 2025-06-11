package com.example.knowledgememorizationapp.user.flashcard.foldersFlashcard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.adaptor.ReceiveSharedFolderAdapter
import com.example.knowledgememorizationapp.databinding.ActivityReceiveSharedFolderBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReceiveSharedFolderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiveSharedFolderBinding
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val adminFolderRef = FirebaseDatabase.getInstance().reference.child("Flashcard_Folders_Admin")
    private val userFolderRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("flashcard_folders")
    private var folderList = listOf<FolderFlashcardModel>()
    private lateinit var adapter: ReceiveSharedFolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveSharedFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.publicFolderRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchAdminFolders()
        setupSearchView()
    }

    private fun fetchAdminFolders() {
        adminFolderRef.get().addOnSuccessListener { snapshot ->
            folderList = snapshot.children.mapNotNull { it.getValue(FolderFlashcardModel::class.java) }
            setupAdapter(folderList)
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi khi tải danh sách folder!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdapter(folders: List<FolderFlashcardModel>) {
        adapter = ReceiveSharedFolderAdapter(folders) { selectedFolder ->
            checkAndSaveFullFolder(selectedFolder.folderId)
        }
        binding.publicFolderRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterFolders(it) }
                return true
            }
        })
    }

    private fun filterFolders(query: String) {
        val filteredList = folderList.filter { it.folderName.contains(query, ignoreCase = true) }
        setupAdapter(filteredList)
    }

    private fun checkAndSaveFullFolder(folderId: String) {
        userFolderRef.child(folderId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Folder đã tồn tại trong danh sách của bạn!", Toast.LENGTH_SHORT).show()
            } else {
                saveFullFolderToUser(folderId)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi kiểm tra folder: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFullFolderToUser(folderId: String) {
        adminFolderRef.child(folderId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                userFolderRef.child(folderId).setValue(snapshot.value)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nhận folder thành công!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi lưu folder: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Không tìm thấy folder trong dữ liệu admin!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi tải dữ liệu folder: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
