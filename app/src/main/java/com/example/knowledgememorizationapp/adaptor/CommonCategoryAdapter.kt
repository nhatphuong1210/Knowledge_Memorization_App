package com.example.knowledgememorizationapp.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.knowledgememorizationapp.databinding.CategoryitemBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel

class CommonCategoryAdapter(
    private var categoryList: List<CategoryAdminModel>,
    private val onSelect: (CategoryAdminModel) -> Unit
) : RecyclerView.Adapter<CommonCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CategoryitemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.apply {
            holder.binding.category.text = category.catName
            Glide.with(root.context).load(category.imageUrl).into(categoryImage)
            root.setOnClickListener { onSelect(category) }
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun setData(filteredList: List<CategoryAdminModel>) {
        categoryList = filteredList
        notifyDataSetChanged()
    }
}