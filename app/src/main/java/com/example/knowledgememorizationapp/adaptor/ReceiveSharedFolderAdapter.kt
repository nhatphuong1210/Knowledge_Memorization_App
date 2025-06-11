package com.example.knowledgememorizationapp.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.databinding.FragmentFolderCardBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel

class ReceiveSharedFolderAdapter(
    private val folderList: List<FolderFlashcardModel>,
    private val onSelect: (FolderFlashcardModel) -> Unit
) : RecyclerView.Adapter<ReceiveSharedFolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(val binding: FragmentFolderCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: FolderFlashcardModel) {
            binding.textTitle.text = folder.folderName
            binding.totalWords.text = "${folder.totalWords} kiến thức"

            // Khi nhấn vào folder, lưu folder vào Firebase
            binding.cardView.setOnClickListener {
                onSelect(folder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = FragmentFolderCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folderList[position])
    }

    override fun getItemCount(): Int = folderList.size
}