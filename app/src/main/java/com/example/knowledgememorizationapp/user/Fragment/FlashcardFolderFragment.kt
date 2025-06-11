package com.example.knowledgememorizationapp.user.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.knowledgememorizationapp.R
import com.example.knowledgememorizationapp.databinding.FragmentFlashcardFolderBinding
import com.example.knowledgememorizationapp.model.FolderFlashcardModel
import com.example.knowledgememorizationapp.user.Form.FolderFormFlashcardActivity
import com.example.knowledgememorizationapp.user.Form.ReceiveFlashcardActivity
import com.example.knowledgememorizationapp.user.flashcard.foldersFlashcard.FolderListAdapter
import com.example.knowledgememorizationapp.user.flashcard.foldersFlashcard.ReceiveSharedFolderActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class FlashcardFolderFragment : Fragment(), FolderListAdapter.OnFolderDeletedListener {

    private var _binding: FragmentFlashcardFolderBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseReference
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userFolderList = ArrayList<FolderFlashcardModel>()
    private lateinit var userAdapter: FolderListAdapter

    private var chancesLeft = 0  // Lượt quay còn lại

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.purple_500)

        val uid = auth.currentUser?.uid ?: return
        db = FirebaseDatabase.getInstance().reference

        userAdapter = FolderListAdapter(userFolderList, requireContext(), this, true)
        binding.userFolderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.userFolderRecyclerView.adapter = userAdapter

        loadFolderList()
        fetchUserName()
        fetchSpinChances()
        fetchSpinInfo() // Thêm gọi hàm cập nhật spin info

        binding.fabAdd.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.fab_menu_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_create_folder -> {
                        startActivity(Intent(requireContext(), FolderFormFlashcardActivity::class.java))
                    }
                    R.id.menu_receive_shared_folder -> {
                        startActivity(Intent(requireContext(), ReceiveSharedFolderActivity::class.java))
                    }
                    R.id.menu_receive_folder_by_id -> {
                        startActivity(Intent(requireContext(), ReceiveFlashcardActivity::class.java))
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun loadFolderList() {
        val uid = auth.currentUser?.uid ?: return
        val folderRef = db.child("Users").child(uid).child("flashcard_folders")

        folderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userFolderList.clear()
                snapshot.children.mapNotNullTo(userFolderList) {
                    it.getValue(FolderFlashcardModel::class.java)
                }
                userAdapter.updateData(userFolderList)
                userAdapter.notifyDataSetChanged()

                binding.emptyFolderListMessage.visibility =
                    if (userFolderList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Lỗi Firebase: ${error.message}")
            }
        })
    }

    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.child("Users").child(uid).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java) ?: "Người dùng"
                    binding.userName.text = name
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.userName.text = "Không tìm thấy tên"
                }
            })
    }

    private fun fetchSpinChances() {
        val uid = auth.currentUser?.uid ?: return
        db.child("SpinHistory").child(uid).child("spinChancesLeft")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    binding.coinCount.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.coinCount.text = "0"
                }
            })
    }

    // Hàm lấy và cập nhật thông tin lượt spin, tương tự HomeFragment
    private fun fetchSpinInfo() {
        val uid = auth.currentUser?.uid ?: return
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val userSpinRef = db.child("SpinHistory").child(uid)

        userSpinRef.get().addOnSuccessListener { snapshot ->
            val lastSpinDate = snapshot.child("lastSpinDate").getValue(String::class.java)
            val oldChances = snapshot.child("spinChancesLeft").getValue(Int::class.java) ?: 0

            val lastDate = lastSpinDate?.let { sdf.parse(it) }
            val today = sdf.parse(todayDate)

            val daysSkipped = if (lastDate != null && today != null) {
                val diff = today.time - lastDate.time
                (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            } else {
                1
            }

            chancesLeft = oldChances + daysSkipped
            updateChanceDisplay()
        }.addOnFailureListener {
            chancesLeft = 1
            updateChanceDisplay()
        }
    }

    private fun updateChanceDisplay() {
        binding.coinCount.text = chancesLeft.toString()
    }

    override fun onFolderDeleted(updatedData: ArrayList<FolderFlashcardModel>) {
        userFolderList.clear()
        userFolderList.addAll(updatedData)
        userAdapter.updateData(userFolderList)
        userAdapter.notifyDataSetChanged()

        binding.emptyFolderListMessage.visibility =
            if (userFolderList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
