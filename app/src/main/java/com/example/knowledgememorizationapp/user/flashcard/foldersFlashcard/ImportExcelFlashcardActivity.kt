package com.example.knowledgememorizationapp.user.flashcard.foldersFlashcard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityImportExcelFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

class ImportExcelFlashcardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImportExcelFlashcardBinding
    private var folderId: String? = null

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
        binding = ActivityImportExcelFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        folderId = intent.getStringExtra("folderId")

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
            val cardRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("flashcard_folders")
                .child(folderId ?: return)
                .child("knowledges")

            var successCount = 0

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row != null) {
                    val question = row.getCell(0)?.toString()?.trim() ?: continue
                    val answer = row.getCell(1)?.toString()?.trim() ?: ""
                    if (question.isNotEmpty() && answer.isNotEmpty()) {
                        val map = mapOf(
                            "title" to question,
                            "content" to answer,
                            "learned" to false,
                            "doneTimestamp" to null
                        )
                        cardRef.push().setValue(map)
                        successCount++
                    }
                }
            }

            Toast.makeText(this, "Đã thêm $successCount flashcard từ Excel!", Toast.LENGTH_SHORT).show()
            workbook.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}