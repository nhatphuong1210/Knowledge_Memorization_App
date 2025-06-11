// 📄 ImportExcelQuizActivity.kt
package com.example.knowledgememorizationapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityImportExcelQuizBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

class ImportExcelQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportExcelQuizBinding
    private var categoryId: String? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    importFromExcel(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportExcelQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra("categoryId")

        binding.btnChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            }
            filePickerLauncher.launch(intent)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun importFromExcel(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val categoryRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("quiz_categories")
                .child(categoryId ?: return)
                .child("questions")

            var successCount = 0

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row != null) {
                    val question = row.getCell(0)?.toString()?.trim() ?: continue
                    val a = row.getCell(1)?.toString()?.trim() ?: ""
                    val b = row.getCell(2)?.toString()?.trim() ?: ""
                    val c = row.getCell(3)?.toString()?.trim() ?: ""
                    val d = row.getCell(4)?.toString()?.trim() ?: ""
                    val correctLetter = row.getCell(5)?.toString()?.trim()?.uppercase() ?: ""

                    val correctAnswer = when (correctLetter) {
                        "A" -> a
                        "B" -> b
                        "C" -> c
                        "D" -> d
                        else -> ""
                    }

                    if (question.isNotEmpty() && correctAnswer.isNotEmpty()) {
                        val map = mapOf(
                            "question" to question,
                            "answers" to listOf(a, b, c, d),
                            "correctAnswer" to correctAnswer
                        )

                        categoryRef.push().setValue(map)
                        successCount++
                    }
                }
            }

            Toast.makeText(this, "Đã thêm $successCount câu hỏi từ Excel!", Toast.LENGTH_SHORT).show()
            workbook.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
