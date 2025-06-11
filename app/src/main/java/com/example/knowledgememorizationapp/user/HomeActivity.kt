package com.example.knowledgememorizationapp.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.knowledgememorizationapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // ← bỏ padding bottom
            insets
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Kiểm tra nếu fragmentContainerView tồn tại
        val navControllerSafe = runCatching { findNavController(R.id.fragmentContainerView) }
        navControllerSafe.getOrNull()?.let {
            navController = it
            bottomNav.setupWithNavController(navController)
        }
    }
}