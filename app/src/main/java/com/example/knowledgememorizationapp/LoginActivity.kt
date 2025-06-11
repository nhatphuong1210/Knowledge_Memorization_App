package com.example.knowledgememorizationapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.admin.AdminActivity
import com.example.knowledgememorizationapp.databinding.ActivityLoginBinding
import com.example.knowledgememorizationapp.user.OptionActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var database: DatabaseReference

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Khởi tạo Google SignIn Options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Xử lý khi nhấn nút Login
        binding.loginButton.setOnClickListener {
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                // Đăng nhập với Firebase Auth
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateUi(auth.currentUser)
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        // Xử lý Google Sign-In
        binding.btnSignInWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        // Xử lý quên mật khẩu
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        // Chuyển sang màn hình đăng ký nếu chưa có tài khoản
        binding.goToSignUp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)) // MainActivity là màn đăng ký
        }
    }

    // Xử lý kết quả từ Google Sign-In
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account: GoogleSignInAccount = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            val userRef = database.child("Users").child(user.uid)

                            // Kiểm tra người dùng đã tồn tại trong database
                            userRef.get().addOnSuccessListener { snapshot ->
                                if (!snapshot.exists()) {
                                    val newUser = hashMapOf(
                                        "uid" to user.uid,
                                        "email" to user.email,
                                        "name" to user.displayName,
                                        "role" to "user" // mặc định là user
                                    )
                                    userRef.setValue(newUser)
                                }

                                // Sau đó kiểm tra lại role để chuyển trang
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val role = dataSnapshot.child("role").getValue(String::class.java)
                                        when (role) {
                                            "admin" -> startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                                            "user" -> startActivity(Intent(this@LoginActivity, OptionActivity::class.java))
                                            else -> Toast.makeText(this@LoginActivity, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                                        }
                                        finish()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@LoginActivity, "Error loading role: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }.addOnFailureListener {
                                Toast.makeText(this, "Failed to check user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, OptionActivity::class.java))
            finish()
        }
    }

    // Cập nhật UI sau khi đăng nhập thành công
    private fun updateUi(user: FirebaseUser?) {
        user?.let {
            val userRef = database.child("Users").child(user.uid)

            userRef.get().addOnSuccessListener { dataSnapshot ->
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
}
