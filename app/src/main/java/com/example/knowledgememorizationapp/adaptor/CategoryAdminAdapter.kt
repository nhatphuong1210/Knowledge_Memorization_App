package com.example.knowledgememorizationapp.adaptor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.knowledgememorizationapp.*
import com.example.knowledgememorizationapp.databinding.ItemViewCategoryadminBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel

class CategoryAdminAdapter(
    private val requireActivity: FragmentActivity,
    private val categoryList: ArrayList<CategoryAdminModel>,
    private val onEditClick: (CategoryAdminModel) -> Unit,
    private val onDeleteClick: (CategoryAdminModel) -> Unit
) : RecyclerView.Adapter<CategoryAdminAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(val binding: ItemViewCategoryadminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemViewCategoryadminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.apply {
            tvCategoryTitle.text = category.catName
            Glide.with(root.context)
                .load(category.imageUrl)
                .into(ivCategoryImage)

            tvEdit.setOnClickListener { onEditClick(category) }
            tvDelete.setOnClickListener { onDeleteClick(category) }

            root.setOnLongClickListener { view ->
                showContextMenu(view, category)
                true
            }
        }
    }

    private fun showContextMenu(view: View, category: CategoryAdminModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.context_menu_category, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val intent = when (menuItem.itemId) {
                R.id.menu_add_question -> Intent(requireActivity, AddQuestionActivity::class.java)
                R.id.menu_view_questions -> Intent(requireActivity, ViewQuestionListActivity::class.java)
                R.id.menu_rename_category -> Intent(requireActivity, RenameCategoryActivity::class.java)
                R.id.menu_delete_category -> Intent(requireActivity, DeleteCategoryActivity::class.java)
                else -> null
            }

            intent?.apply {
                putExtra("categoryId", category.catid)
                putExtra("categoryName", category.catName)
                putExtra("isAdmin", true)
                requireActivity.startActivity(this)
            }

            true
        }

        popupMenu.show()
    }
    fun setData(newList: List<CategoryAdminModel>) {
        categoryList.clear()
        categoryList.addAll(newList)
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = categoryList.size
}