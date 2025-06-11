package com.example.knowledgememorizationapp.user.flashcard.knowledgeListFlashcard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.FragmentKnowledgeCardBinding
import com.example.knowledgememorizationapp.model.KnowledgeFlashcardModel

class KnowledgeListAdapter(
    private val knowledgeList: List<KnowledgeFlashcardModel>,
    private val context: Context,
    private val onEditClick: (KnowledgeFlashcardModel) -> Unit,
    private val onDeleteClick: (KnowledgeFlashcardModel) -> Unit,
    private val isPublicFolder: Boolean // thêm biến này
) : RecyclerView.Adapter<KnowledgeListAdapter.KnowledgeViewHolder>() {

    inner class KnowledgeViewHolder(private val binding: FragmentKnowledgeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KnowledgeFlashcardModel) {
            binding.textTitle.text = item.title

            // Ẩn menu icon nếu là folder public
            if (isPublicFolder) {
                binding.menuIcon.visibility = View.GONE
            } else {
                binding.menuIcon.visibility = View.VISIBLE
                binding.menuIcon.setOnClickListener {
                    showPopupMenu(item)
                }
            }
        }

        private fun showPopupMenu(item: KnowledgeFlashcardModel) {
            val popupMenu = PopupMenu(context, binding.menuIcon)
            popupMenu.menuInflater.inflate(R.menu.context_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        onEditClick(item)
                        true
                    }
                    R.id.menu_delete -> {
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
