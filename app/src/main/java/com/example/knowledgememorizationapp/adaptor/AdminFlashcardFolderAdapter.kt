package com.example.knowledgememorizationapp.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.FragmentFolderCardBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.google.firebase.database.*

class AdminFlashcardFolderAdapter(
    private val folderList: List<FolderFlashcardModel>,
    private val listener: OnFolderActionListener
) : RecyclerView.Adapter<AdminFlashcardFolderAdapter.FolderViewHolder>() {

    interface OnFolderActionListener {
        fun onEdit(folder: FolderFlashcardModel)
        fun onDelete(folder: FolderFlashcardModel)
        fun onClick(folder: FolderFlashcardModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = FragmentFolderCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folderList[position])
    }

    override fun getItemCount(): Int = folderList.size

    inner class FolderViewHolder(private val binding: FragmentFolderCardBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(folder: FolderFlashcardModel) {
            binding.textTitle.text = folder.folderName
            loadKnowledgeCount(folder.folderId)
            binding.cardView.setOnClickListener {
                listener.onClick(folder)
            }

            binding.menuIcon.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.context_menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            listener.onEdit(folder)
                            true
                        }

                        R.id.menu_delete -> {
                            listener.onDelete(folder)
                            true
                        }

                        else -> false
                    }
                }
                popup.show()
            }
        }

        private fun loadKnowledgeCount(folderId: String) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("Flashcard_Folders_Admin")
                .child(folderId)
                .child("knowledges")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    binding.totalWords.text = "$count kiến thức"
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.totalWords.text = "0 kiến thức"
                }
            })
        }
    }
}
