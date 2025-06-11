package com.example.knowledgememorizationapp.admin.flashcard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.adaptor.AdminFlashcardFolderAdapter
import com.example.knowledgememorizationapp.admin.Form.AdminFolderFormActivity
import com.example.knowledgememorizationapp.admin.flashcard.knowledgeListFlashcard.AdminKnowledgeListActivity
import com.example.knowledgememorizationapp.databinding.FragmentAdminFlashcardFolderBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.google.firebase.database.*

class AdminFlashcardFolderFragment : Fragment(), AdminFlashcardFolderAdapter.OnFolderActionListener {

    private var _binding: FragmentAdminFlashcardFolderBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private val folderList = mutableListOf<FolderFlashcardModel>()
    private val filteredList = mutableListOf<FolderFlashcardModel>()
    private lateinit var adapter: AdminFlashcardFolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminFlashcardFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbRef = FirebaseDatabase.getInstance().getReference("Flashcard_Folders_Admin")

        adapter = AdminFlashcardFolderAdapter(filteredList, this)
        binding.foldersListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.foldersListRecyclerView.adapter = adapter

        // Đổi nút “Tạo Folder” thành FloatingActionButton (fabAdd)
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AdminFolderFormActivity::class.java))
        }

        // Tìm kiếm theo tên folder
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterFolders(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadFolders()
    }

    private fun loadFolders() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                folderList.clear()
                for (snap in snapshot.children) {
                    snap.getValue(FolderFlashcardModel::class.java)?.let { folderList.add(it) }
                }
                filterFolders(binding.searchEt.text.toString())
                binding.emptyFolderListMessage.visibility =
                    if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterFolders(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(
            folderList.filter {
                it.folderName?.lowercase()?.contains(lowerQuery) == true
            }
        )
        adapter.notifyDataSetChanged()
        binding.emptyFolderListMessage.visibility =
            if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onEdit(folder: FolderFlashcardModel) {
        val intent = Intent(requireContext(), AdminFolderFormActivity::class.java)
        intent.putExtra("folderId", folder.folderId)
        intent.putExtra("folderName", folder.folderName)
        startActivity(intent)
    }

    override fun onClick(folder: FolderFlashcardModel) {
        val intent = Intent(requireContext(), AdminKnowledgeListActivity::class.java)
        intent.putExtra("folderId", folder.folderId)
        intent.putExtra("folderName", folder.folderName)
        startActivity(intent)
    }

    override fun onDelete(folder: FolderFlashcardModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa folder “${folder.folderName}” không?")
            .setPositiveButton("Xóa") { _, _ ->
                dbRef.child(folder.folderId).removeValue()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
