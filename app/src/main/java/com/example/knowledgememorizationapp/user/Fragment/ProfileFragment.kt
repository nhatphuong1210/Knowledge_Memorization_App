package com.example.knowledgememorizationapp.user.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.knowledgememorizationapp.LoginActivity
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.FragmentProfileBinding
import com.example.knowledgememorizationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference.child("Users") }
    private var isExpan = true
    private lateinit var userListener: ValueEventListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.imageButton.setOnClickListener {
            isExpan = !isExpan
            binding.expadableconstraintlayout.visibility = if (isExpan) View.VISIBLE else View.GONE
            binding.imageButton.setImageResource(if (isExpan) R.drawable.arrowup else R.drawable.downarrow)
        }

        auth.currentUser?.uid?.let { userId ->
            userListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let { user ->
                        binding.Name.text = user.name
                        binding.nameUp.text = user.name
                        binding.Email.text = user.email
                        binding.age.text = user.age.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching user data: ${error.message}")
                }
            }
            db.child(userId).addValueEventListener(userListener)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        auth.currentUser?.uid?.let { userId ->
            db.child(userId).removeEventListener(userListener) // Ngắt lắng nghe Firebase
        }
        _binding = null // Giải phóng binding khi Fragment bị hủy
    }
}