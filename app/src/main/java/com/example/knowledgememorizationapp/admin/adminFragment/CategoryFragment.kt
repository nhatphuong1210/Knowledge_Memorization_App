package com.example.knowledgememorizationapp.admin.adminFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.knowledgememorizationapp.adaptor.CategoryAdminAdapter
import com.example.knowledgememorizationapp.admin.Form.CategoryAdminFormActivity
import com.example.knowledgememorizationapp.databinding.FragmentCategoryBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel
import com.google.firebase.database.FirebaseDatabase

class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdminAdapter
    private val categoryList = ArrayList<CategoryAdminModel>()
    private val allCategories = ArrayList<CategoryAdminModel>() // NEW


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = CategoryAdminAdapter(
            requireActivity(),
            arrayListOf(),
            onEditClick = { category ->
                val intent = Intent(requireContext(), CategoryAdminFormActivity::class.java)
                intent.putExtra("categoryId", category.catid)
                intent.putExtra("categoryName", category.catName)
                startActivity(intent)
            },
            onDeleteClick = { category ->
                deleteCategory(category)
            }
        )
        binding.rvQuizCategories.adapter = categoryAdapter

        binding.searchEt.doOnTextChanged { text, _, _, _ ->
            val query = text.toString().lowercase()
            val filtered = allCategories.filter { it.catName?.lowercase()?.contains(query) == true }
            categoryAdapter.setData(filtered)
        }


        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), CategoryAdminFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchCategories()
    }

    private fun fetchCategories() {
        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.rvQuizCategories.visibility = View.GONE
        binding.tvEmptyQuestionMessage.visibility = View.GONE

        val ref = FirebaseDatabase.getInstance().getReference("QuizCategories")
        ref.get().addOnSuccessListener { snapshot ->
            val list = snapshot.children.mapNotNull {
                it.getValue(CategoryAdminModel::class.java)
            }

            allCategories.clear()
            allCategories.addAll(list)

            categoryAdapter.setData(list)

            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE

            if (list.isEmpty()) {
                binding.tvEmptyQuestionMessage.visibility = View.VISIBLE
            } else {
                binding.rvQuizCategories.visibility = View.VISIBLE
            }
        }.addOnFailureListener {
            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE
            binding.tvEmptyQuestionMessage.visibility = View.VISIBLE
        }
    }


    private fun deleteCategory(category: CategoryAdminModel) {
        val ref = FirebaseDatabase.getInstance().getReference("QuizCategories")
        ref.child(category.catid ?: return).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Đã xóa danh mục", Toast.LENGTH_SHORT).show()
            fetchCategories()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Lỗi khi xóa danh mục", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}