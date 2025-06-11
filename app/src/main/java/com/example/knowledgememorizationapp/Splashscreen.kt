package com.example.knowledgememorizationapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.admin.AdminActivity
import com.example.knowledgememorizationapp.user.OptionActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            FirebaseDatabase.getInstance().reference.child("Users")
                .child(currentUser.uid)
                .get().addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").getValue(String::class.java)
                    when (role) {
                        "admin" -> {
                            startActivity(Intent(this, AdminActivity::class.java))
                        }
                        "user" -> {
                            startActivity(Intent(this, OptionActivity::class.java))
                        }
                        else -> {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                    finish()
                }.addOnFailureListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        } else {
            // Chưa đăng nhập
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}