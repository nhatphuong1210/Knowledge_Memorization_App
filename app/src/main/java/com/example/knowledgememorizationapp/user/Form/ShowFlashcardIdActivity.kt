package com.example.knowledgememorizationapp.user.Form
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.databinding.ActivityShowFlashcardIdBinding

class ShowFlashcardIdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowFlashcardIdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowFlashcardIdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getStringExtra("folderId") ?: "Không có mã"
        val folderName = intent.getStringExtra("folderName") ?: "Không rõ"

        binding.flashcardIdText.text = "Mã chia sẻ: $folderId"
        binding.flashcardNameText.text = "Tên thư mục: $folderName"

        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Folder ID", folderId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Đã sao chép mã!", Toast.LENGTH_SHORT).show()
        }
    }
}
