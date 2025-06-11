package com.example.knowledgememorizationapp.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.knowledgememorizationapp.LoginActivity
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityAdminBinding // Import ViewBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() ,NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup ViewBinding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Áp dụng padding để hiển thị edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set toolbar làm ActionBar
        setSupportActionBar(binding.topAppBar)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)



        // Khởi tạo toggle cho Navigation Drawer
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.topAppBar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Gán listener cho NavigationView
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Mặc định load Home fragment

    }

    // Bắt sự kiện khi click item trong Navigation Drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.fragmentContainerView2)
        val handled = NavigationUI.onNavDestinationSelected(item, navController)
        binding.drawerLayout.closeDrawers()
        return handled
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_admin, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuLogout -> {
                // Đăng xuất khỏi Firebase
                FirebaseAuth.getInstance().signOut()

                // Chuyển về màn hình đăng nhập
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
