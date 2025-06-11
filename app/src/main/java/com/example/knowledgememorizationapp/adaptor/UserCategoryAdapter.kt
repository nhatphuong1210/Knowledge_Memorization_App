package com.example.knowledgememorizationapp.adaptor

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.knowledgememorizationapp.AddQuestionActivity
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.CategoryUserItemBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel
import com.example.knowledgememorizationapp.QuizActivity
import com.example.knowledgememorizationapp.user.Form.ShowQuizIdActivity
import com.example.knowledgememorizationapp.ViewQuestionListActivity
import com.example.knowledgememorizationapp.ImportExcelQuizActivity
import com.example.knowledgememorizationapp.user.RetryIncorrectQuestionsActivity

class UserCategoryAdapter(
    private var categoryList: List<CategoryAdminModel>,
    private val requireActivity: FragmentActivity,
    private val onEdit: (CategoryAdminModel) -> Unit,
    private val onDelete: (CategoryAdminModel) -> Unit
) : RecyclerView.Adapter<UserCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CategoryUserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = categoryList[position]
        with(holder.binding) {
            category.text = model.catName ?: "Không có danh mục"
            Glide.with(root.context).load(model.imageUrl ?: "").into(categoryImage)

            root.setOnClickListener {
                val intent = Intent(requireActivity, QuizActivity::class.java).apply {
                    putExtra("categoryId", model.catid)
                    putExtra("categoryName", model.catName)
                }
                requireActivity.startActivity(intent)
            }

            root.setOnLongClickListener { view ->
                showContextMenu(view, model)
                true
            }
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun setData(newList: List<CategoryAdminModel>) {
        categoryList = newList
        notifyDataSetChanged()
    }

    private fun showContextMenu(view: View, category: CategoryAdminModel) {
        if (category.catid.isNullOrEmpty()) {
            return
        }

        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.context_menu_category, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_question -> {
                    val intent = Intent(requireActivity, AddQuestionActivity::class.java).apply {
                        putExtra("categoryId", category.catid)
                        putExtra("categoryName", category.catName)
                    }
                    requireActivity.startActivity(intent)
                }
                R.id.menu_view_questions -> {
                    val intent = Intent(requireActivity, ViewQuestionListActivity::class.java).apply {
                        putExtra("categoryId", category.catid)
                        putExtra("categoryName", category.catName)
                    }
                    requireActivity.startActivity(intent)
                }
                R.id.menu_rename_category -> onEdit(category)
                R.id.menu_delete_category -> onDelete(category)
                R.id.menu_show_quiz_id -> {
                    val intent = Intent(requireActivity, ShowQuizIdActivity::class.java).apply {
                        putExtra("categoryId", category.catid)
                        putExtra("categoryName", category.catName)
                    }
                    requireActivity.startActivity(intent)
                }
                R.id.menu_redo_wrong ->{
                    val intent = Intent(requireActivity, RetryIncorrectQuestionsActivity::class.java).apply {
                        putExtra("categoryId", category.catid)
                        putExtra("categoryName", category.catName)
                    }
                    requireActivity.startActivity(intent)

                }
                R.id.menu_import_excel_quiz -> {
                    val intent = Intent(requireActivity, ImportExcelQuizActivity::class.java).apply {
                        putExtra("categoryId", category.catid)
                    }
                    requireActivity.startActivity(intent)
                }

            }
            true
        }

        popupMenu.show()
    }
}