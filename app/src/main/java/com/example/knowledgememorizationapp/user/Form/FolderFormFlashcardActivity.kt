package com.example.knowledgememorizationapp.user.Form

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.ActivityFolderFormFlashcardBinding
import com.example.knowledgememorizationapp.user.Fragment.FlashcardFolderFragment
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FolderFormFlashcardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFolderFormFlashcardBinding
    private lateinit var database: DatabaseReference
    private var folderIdToUpdate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityFolderFormFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        database = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("flashcard_folders")


        val intent = intent
        if (intent.hasExtra("folderId") && intent.hasExtra("folderName")) {
            folderIdToUpdate = intent.getStringExtra("folderId")
            val currentFolderName = intent.getStringExtra("folderName")
            binding.folderNameEditText.setText(currentFolderName)
            binding.topAppBar.title = "Edit Folder"
            binding.submitFolderButton.text = "Update"
        } else {
            binding.topAppBar.title = "Create Folder"
        }

        binding.submitFolderButton.setOnClickListener {
            val folderName = binding.folderNameEditText.text.toString().trim()

            if (folderName.isEmpty()) {
                Toast.makeText(this, "Please enter folder name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (folderIdToUpdate != null) {
                val updates = mapOf("folderName" to folderName)
                database.child(folderIdToUpdate!!).updateChildren(updates)
                Toast.makeText(this, "Folder has been updated", Toast.LENGTH_SHORT).show()
            } else {
                val folderId = database.push().key!!
                val folder = FolderFlashcardModel(folderId, folderName)
                database.child(folderId).setValue(folder)

                Toast.makeText(this, "Folder has been created", Toast.LENGTH_SHORT).show()
            }

            redirectToFolderList()
        }
    }

    private fun redirectToFolderList() {
        val intent = Intent(this, FlashcardFolderFragment::class.java)
        startActivity(intent)
        finish()
    }
}