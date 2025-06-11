package com.example.knowledgememorizationapp

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.knowledgememorizationapp.R

open class BaseActivity : AppCompatActivity() {
    private var pb: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showProgressBar() {
        if (pb == null) {
            pb = Dialog(this)
            pb?.setContentView(R.layout.progress_bar)
            pb?.setCancelable(false)
        }
        if (!pb!!.isShowing) {
            pb?.show()
        }
    }

    fun hideProgressBar() {
        pb?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}