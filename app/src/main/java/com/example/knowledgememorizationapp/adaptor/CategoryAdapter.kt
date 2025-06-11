package com.example.knowledgememorizationapp.adaptor

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.knowledgememorizationapp.*
import com.example.knowledgememorizationapp.databinding.CategoryitemBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel

class CategoryAdapter(
    private var categoryList: List<CategoryAdminModel>,
    private val requireActivity: FragmentActivity,
    private val isUserCategory: Boolean, // Kiểm tra danh mục thuộc Users hay QuizCategories
    private val isAdmin: Boolean // Kiểm tra nếu là admin
) : RecyclerView.Adapter<CategoryAdapter.MyCategoryViewHolder>() {

    class MyCategoryViewHolder(val binding: CategoryitemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCategoryViewHolder {
        val binding = CategoryitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyCategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: MyCategoryViewHolder, position: Int) {
        val data = categoryList[position]
        holder.binding.category.text = data.catName
        Glide.with(holder.itemView.context)
            .load(data.imageUrl)
            .placeholder(R.drawable.default_category_icon) // Tránh lỗi nếu ảnh chưa tải kịp
            .into(holder.binding.categoryImage)

        // Khi nhấn vào danh mục, mở QuizActivity
        holder.binding.categorybtn.setOnClickListener {
            val intent = Intent(requireActivity, QuizActivity::class.java)
            intent.putExtra("categoryId", data.catid)
            intent.putExtra("categoryName", data.catName)
            requireActivity.startActivity(intent)
        }

        // Nếu là User và danh mục thuộc `category_user_item.xml`, hiển thị menu khi nhấn lâu
        if (!isAdmin && isUserCategory) {
            holder.binding.categorybtn.setOnLongClickListener { view ->
                Log.d("LongClick", "User nhấn lâu vào danh mục") // Kiểm tra log
                showContextMenu(view, data)
                true
            }
        }

        // Nếu là Admin và danh mục thuộc `categoryitem.xml`, hiển thị menu khi nhấn lâu
        if (isAdmin && !isUserCategory) {
            holder.binding.categorybtn.setOnLongClickListener { view ->
                Log.d("LongClick", "Admin nhấn lâu vào danh mục") // Kiểm tra log
                showContextMenu(view, data)
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
                requireActivity.startActivity(this)
            }

            Log.d("PopupMenu", "Menu hiển thị") // Kiểm tra log
            true
        }

        popupMenu.show()
    }

    fun setData(newList: List<CategoryAdminModel>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}