package com.example.knowledgememorizationapp.admin.flashcard.knowledgeListFlashcard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.databinding.FragmentKnowledgeCardBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel

class AdminKnowledgeListAdapter(
    private val knowledgeList: List<KnowledgeFlashcardModel>,
    private val context: Context,
    private val onEditClick: (KnowledgeFlashcardModel) -> Unit,
    private val onDeleteClick: (KnowledgeFlashcardModel) -> Unit
) : RecyclerView.Adapter<AdminKnowledgeListAdapter.KnowledgeViewHolder>() {

    inner class KnowledgeViewHolder(private val binding: FragmentKnowledgeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KnowledgeFlashcardModel) {
            binding.textTitle.text = item.title

            // Bắt sự kiện click vào icon để hiện menu
            binding.menuIcon.setOnClickListener {
                showPopupMenu(item)
            }
        }

        private fun showPopupMenu(item: KnowledgeFlashcardModel) {
            val popupMenu = android.widget.PopupMenu(context, binding.menuIcon)
            popupMenu.menuInflater.inflate(com.example.knowledgememorizationapp.R.menu.context_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    com.example.knowledgememorizationapp.R.id.menu_edit -> {
                        onEditClick(item)
                        true
                    }
                    com.example.knowledgememorizationapp.R.id.menu_delete -> {
                        onDeleteClick(item)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowledgeViewHolder {
        val binding = FragmentKnowledgeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KnowledgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KnowledgeViewHolder, position: Int) {
        holder.bind(knowledgeList[position])
    }

    override fun getItemCount(): Int = knowledgeList.size
}