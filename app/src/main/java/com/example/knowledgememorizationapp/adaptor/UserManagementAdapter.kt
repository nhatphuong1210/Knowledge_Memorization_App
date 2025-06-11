package com.example.knowledgememorizationapp.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.knowledgememorizationapp.databinding.ItemManagementUserBinding
import com.example.knowledgememorizationapp.model.User
import java.util.Locale
import android.widget.Filter
import android.widget.PopupMenu
import com.example.knowledgememorizationapp.R


class UserManagementAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserManagementAdapter.UserViewHolder>(), Filterable {

    private var filteredList = ArrayList<User>()
    var onEditUser: ((User) -> Unit)? = null
    var onDeleteUser: ((User) -> Unit)? = null

    init {
        filteredList = ArrayList(userList)
    }

    inner class UserViewHolder(val binding: ItemManagementUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            binding.tvRole.text = "Vai trò: ${user.role}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemManagementUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(filteredList[position])
        holder.binding.menuIcon.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.binding.menuIcon)
            popup.inflate(R.menu.context_menu) // menu_edit + menu_delete
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit -> {
                        onEditUser?.invoke(filteredList[position])
                        true
                    }
                    R.id.menu_delete -> {
                        onDeleteUser?.invoke(filteredList[position])
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keyword = constraint?.toString()?.lowercase(Locale.getDefault()) ?: ""
                val results = FilterResults()
                results.values = if (keyword.isEmpty()) {
                    userList
                } else {
                    userList.filter {
                        (it.name ?: "").lowercase(Locale.getDefault()).contains(keyword) ||
                                (it.email ?: "").lowercase(Locale.getDefault()).contains(keyword)

                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = ArrayList(results?.values as List<User>)
                notifyDataSetChanged()
            }
        }
    }

}