package com.example.knowledgememorizationapp.user
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.user.Fragment.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class OptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Mặc định hiển thị OptionFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, OptionFragment())
            .commit()

        // Xử lý điều hướng
        bottomNav.setOnItemSelectedListener {
            val fragment: Fragment = when (it.itemId) {
                R.id.optionFragment -> OptionFragment()
                R.id.spinFragment -> SpinFragment() // Tạo fragment này nếu chưa có
                R.id.historyFragment -> HistoryFragment()
                R.id.statisticsFragment2 -> StatisticsFragment()
                R.id.profileFragment -> ProfileFragment()
                else -> OptionFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit()
            true
        }

    }
}
