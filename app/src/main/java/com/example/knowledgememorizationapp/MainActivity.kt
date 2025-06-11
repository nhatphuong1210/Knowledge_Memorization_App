package com.example.knowledgememorizationapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.admin.AdminActivity
import com.example.knowledgememorizationapp.databinding.ActivityMainBinding
import com.example.knowledgememorizationapp.model.User
import com.example.knowledgememorizationapp.user.OptionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.signUp.setOnClickListener {
            val name = binding.name.text.toString()
            val age = binding.age.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (name.isEmpty() || age.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast("Please fill all the details")
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val user = User(uid, name, age.toInt(), email, role = "user")

                        val userMap = mapOf(
                            "uid" to user.uid,
                            "name" to user.name,
                            "age" to user.age,
                            "email" to user.email,
                            "role" to user.role,
                            "flashcard_limit" to 100,
                            "quiz_limit" to 100
                        )

                        database.child("Users").child(uid).setValue(userMap)
                            .addOnSuccessListener {
                            startActivity(Intent(this, OptionActivity::class.java))
                            finish()
                        }
                    } else {
                        showToast(it.exception?.message ?: "Error during sign-up")
                    }
                }
            }
        }

        binding.goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onStart() {

            super.onStart()
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                Firebase.database.reference.child("Users").child(currentUser.uid)
                    .get().addOnSuccessListener { dataSnapshot ->
                        val role = dataSnapshot.child("role").getValue(String::class.java)
                        when (role) {
                            "admin" -> {
                                startActivity(Intent(this, AdminActivity::class.java))
                            }

                            "user" -> {
                                startActivity(Intent(this, OptionActivity::class.java))
                            }

                            else -> {
                                Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                            }
                        }
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
            }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}