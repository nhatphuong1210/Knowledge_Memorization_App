package com.example.knowledgememorizationapp.admin.adminFragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.adaptor.UserManagementAdapter
import com.example.knowledgememorizationapp.databinding.DialogEditUserBinding
import com.example.knowledgememorizationapp.databinding.FragmentUserManagementBinding
import com.example.knowledgememorizationapp.model.User
import com.example.knowledgememorizationapp.R
import com.google.firebase.database.*

class UserManagementFragment : Fragment() {

    private lateinit var binding: FragmentUserManagementBinding
    private lateinit var userAdapter: UserManagementAdapter
    private lateinit var userList: ArrayList<User>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userList = ArrayList()
        userAdapter = UserManagementAdapter(userList)

        binding.recyclerViewUser.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewUser.adapter = userAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        loadUsersFromFirebase()

        // Tìm kiếm người dùng khi nhập văn bản
        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUsersFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    if (userSnapshot.value is Map<*, *>) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            user.uid = userSnapshot.key ?: ""
                            userList.add(user)
                        }
                    }
                }
                userAdapter = UserManagementAdapter(userList).apply {
                    onEditUser = { user -> showEditUserDialog(user) }
                    onDeleteUser = { user -> showDeleteUserDialog(user) }  // thêm xử lý xóa
                }
                binding.recyclerViewUser.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    private fun showDeleteUserDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc muốn xóa người dùng \"${user.name}\" không?")
            .setPositiveButton("Xóa") { _, _ ->
                databaseReference.child(user.uid ?: return@setPositiveButton)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Xóa thành công", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun showEditUserDialog(user: User) {
        val dialogBinding = DialogEditUserBinding.inflate(layoutInflater)

        dialogBinding.etName.setText(user.name)
        dialogBinding.etEmail.setText(user.email)

        val roles = resources.getStringArray(R.array.roles_array)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRole.adapter = adapter
        val roleIndex = roles.indexOf(user.role)
        if (roleIndex >= 0) dialogBinding.spinnerRole.setSelection(roleIndex)

        AlertDialog.Builder(requireContext())
            .setTitle("Cập nhật người dùng")
            .setView(dialogBinding.root)
            .setPositiveButton("Lưu") { _, _ ->
                val updatedUser = mapOf(
                    "name" to dialogBinding.etName.text.toString().trim(),
                    "email" to dialogBinding.etEmail.text.toString().trim(),
                    "role" to dialogBinding.spinnerRole.selectedItem.toString()
                )

                FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.uid ?: return@setPositiveButton)
                    .updateChildren(updatedUser)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
