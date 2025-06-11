package com.example.knowledgememorizationapp.user.flashcard.foldersFlashcard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.FragmentFolderCardBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.example.knowledgememorizationapp.user.Form.FolderFormFlashcardActivity
import com.example.knowledgememorizationapp.user.flashcard.knowledgeListFlashcard.KnowledgeListActivity
import com.example.knowledgememorizationapp.user.Form.ShowFlashcardIdActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FolderListAdapter(
    private var folderList: ArrayList<FolderFlashcardModel>,
    private val context: Context,
    private val folderDeletedListener: OnFolderDeletedListener?,
    private val isEditable: Boolean = true
) : RecyclerView.Adapter<FolderListAdapter.FolderViewHolder>() {

    interface OnFolderDeletedListener {
        fun onFolderDeleted(updatedData: ArrayList<FolderFlashcardModel>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = FragmentFolderCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folderList[position])
    }

    override fun getItemCount(): Int = folderList.size

    fun updateData(newData: ArrayList<FolderFlashcardModel>) {
        folderList = newData
        notifyDataSetChanged()
    }

    inner class FolderViewHolder(private val binding: FragmentFolderCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        private var folderId: String = ""

        fun bind(folder: FolderFlashcardModel) {
            folderId = folder.folderId
            binding.textTitle.text = folder.folderName

            loadKnowledgeCount(folderId)

            binding.cardView.setOnClickListener {
                saveFolderPreferences(folderId, folder.folderName, isPublic = !isEditable)
                context.startActivity(Intent(context, KnowledgeListActivity::class.java))
            }

            binding.menuIcon.visibility = if (isEditable) View.VISIBLE else View.GONE

            binding.menuIcon.setOnClickListener {
                showContextMenu(folder)
            }
        }

        private fun loadKnowledgeCount(folderId: String) {
            val knowledgeRef = if (isEditable) {
                FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("flashcard_folders")
                    .child(folderId)
                    .child("knowledges")
            } else {
                FirebaseDatabase.getInstance()
                    .getReference("Flashcard_Folders_Admin")
                    .child(folderId)
                    .child("knowledges")
            }

            knowledgeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    binding.totalWords.text = "$count kiến thức"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun showContextMenu(folder: FolderFlashcardModel) {
            val popup = PopupMenu(context, binding.menuIcon)
            popup.inflate(R.menu.context_menu_folder)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_rename_folder -> {
                        editFolder()
                        true
                    }
                    R.id.menu_delete_folder -> {
                        confirmDelete()
                        true
                    }
                    R.id.menu_show_folder_id -> {
                        val intent = Intent(context, ShowFlashcardIdActivity::class.java).apply {
                            putExtra("folderId", folder.folderId)
                            putExtra("folderName", folder.folderName)
                        }
                        context.startActivity(intent)
                        true
                    }
                    R.id.menu_import_excel_flashcard -> {
                        val intent = Intent(context, ImportExcelFlashcardActivity::class.java).apply {
                            putExtra("folderId", folder.folderId)
                            putExtra("folderName", folder.folderName)
                        }
                        context.startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun editFolder() {
            val intent = Intent(context, FolderFormFlashcardActivity::class.java)
            intent.putExtra("folderId", folderId)
            intent.putExtra("folderName", binding.textTitle.text.toString())
            context.startActivity(intent)
        }

        private fun confirmDelete() {
            AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa folder này?")
                .setPositiveButton("Có") { _, _ ->
                    FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(userId)
                        .child("flashcard_folders")
                        .child(folderId)
                        .removeValue()
                        .addOnSuccessListener { fetchUpdatedFolderList() }
                }
                .setNegativeButton("Không", null)
                .show()
        }

        private fun fetchUpdatedFolderList() {
            val ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("flashcard_folders")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updatedList = ArrayList<FolderFlashcardModel>()
                    for (folderSnap in snapshot.children) {
                        val folder = folderSnap.getValue(FolderFlashcardModel::class.java)
                        folder?.let { updatedList.add(it) }
                    }
                    folderDeletedListener?.onFolderDeleted(updatedList)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun saveFolderPreferences(folderId: String, folderName: String, isPublic: Boolean) {
            val prefs: SharedPreferences = context.getSharedPreferences("FolderPreferences", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("currentFolderId", folderId)
                putString("currentFolderName", folderName)
                putBoolean("isPublicFolder", isPublic)
                apply()
            }
        }
    }
}
