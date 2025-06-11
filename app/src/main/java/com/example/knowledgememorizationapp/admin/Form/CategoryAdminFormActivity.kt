package com.example.knowledgememorizationapp.admin.Form

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.knowledgememorizationapp.databinding.FragmentCategoryAdminFormBinding
import com.example.knowledgememorizationapp.model.CategoryAdminModel
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class CategoryAdminFormActivity : AppCompatActivity() {

    private lateinit var binding: FragmentCategoryAdminFormBinding
    private val database = FirebaseDatabase.getInstance().getReference("QuizCategories")
    private var categoryId: String? = null
    private var imageUrl: String? = null

    private val client = OkHttpClient()

    // 👇 Đổi thành thông tin thật của bạn
    private val CLOUD_NAME = "datl2beso" // <--- thay bằng cloud_name của bạn
    private val UPLOAD_PRESET = "KnowledgeApp" // <--- preset cho unsigned upload

    private val CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadImageToCloudinary(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCategoryAdminFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.topAppBar.setNavigationOnClickListener { finish() }

        categoryId = intent.getStringExtra("categoryId")

        if (categoryId != null) {
            binding.topAppBar.title = "Edit Category"
            binding.btnAddCategory.text = "Update Category"
            loadCategoryForEdit()
        } else {
            binding.topAppBar.title = "Create Category"
            binding.btnAddCategory.text = "Add Category"
        }

        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnAddCategory.setOnClickListener {
            val title = binding.etCategoryTitle.text.toString().trim()
            if (title.isEmpty()) {
                binding.etCategoryTitle.error = "Title required"
                return@setOnClickListener
            }
            if (imageUrl == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoryId == null) {
                addCategory(title, imageUrl!!)
            } else {
                updateCategory(title, imageUrl!!)
            }
        }
    }

    private fun loadCategoryForEdit() {
        database.child(categoryId!!).get().addOnSuccessListener { snapshot ->
            val category = snapshot.getValue(CategoryAdminModel::class.java)
            category?.let {
                binding.etCategoryTitle.setText(it.catName)
                imageUrl = it.imageUrl
                Glide.with(this).load(it.imageUrl).into(binding.btnSelectImage)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        val file = File(cacheDir, "temp_image.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(CLOUDINARY_UPLOAD_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CategoryAdminFormActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    imageUrl = json.getString("secure_url")
                    runOnUiThread {
                        Glide.with(this@CategoryAdminFormActivity)
                            .load(imageUrl)
                            .into(binding.btnSelectImage)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CategoryAdminFormActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun addCategory(title: String, imageUrl: String) {
        val newId = UUID.randomUUID().toString()
        val category = CategoryAdminModel(newId, title, imageUrl)

        database.child(newId).setValue(category)
            .addOnSuccessListener {
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCategory(title: String, imageUrl: String) {
        val category = CategoryAdminModel(categoryId!!, title, imageUrl)

        database.child(categoryId!!).setValue(category)
            .addOnSuccessListener {
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show()
            }
    }
}
