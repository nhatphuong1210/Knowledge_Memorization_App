package com.example.knowledgememorizationapp.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityHomeFlashcardBinding

class Home_Flashcard_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeFlashcardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Áp dụng WindowInsets để không bị che layout bởi hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Gắn NavController với BottomNavigationView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Kết nối BottomNavigationView với NavController
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}
