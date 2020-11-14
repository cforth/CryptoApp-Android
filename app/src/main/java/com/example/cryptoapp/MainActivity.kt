package com.example.cryptoapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        encryptButton.setOnClickListener(this)
        decryptButton.setOnClickListener(this)
        copyOutputButton.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.encryptButton -> {
                val password = passwordEditText.text.toString()
                val input = inputEditText.text.toString()
                try {
                    val e = StringCrypto(password).encrypt(input)
                    outputEditText.setText(e)
                } catch(e:Exception) {
                    Log.d("Encrypt Error", "encrypt text exception")
                    outputEditText.setText("")
                    Toast.makeText(this, "Encrypt text exception", Toast.LENGTH_SHORT).show()
                }
//                // 测试图片切换
//                imageView.setImageResource(R.drawable.img_2)
                // 测试进度条
//                if (progressBar.visibility == View.VISIBLE) {
//                    progressBar.visibility = View.GONE
//                } else {
//                    progressBar.visibility = View.VISIBLE
//                }
//                progressBar.progress = progressBar.progress + 10
            }

            R.id.decryptButton -> {
                val password = passwordEditText.text.toString()
                val input = inputEditText.text.toString()
                try {
                    val d = StringCrypto(password).decrypt(input)
                    outputEditText.setText(d)
                } catch(e:Exception) {
                    Log.d("Decrypt Error", "decrypt text exception")
                    outputEditText.setText("")
                    Toast.makeText(this, "Decrypt text exception", Toast.LENGTH_SHORT).show()
                }
                // 测试图片切换
//                imageView.setImageResource(R.drawable.img_1)
            }

            R.id.copyOutputButton -> {
                try {
                    val textToCopy = outputEditText.text.toString()
                    copyToClipboard(textToCopy)
                } catch(e:Exception) {
                    Log.d("Copy Output Error", "copy text exception")
                    outputEditText.setText("")
                    Toast.makeText(this, "Copy text exception", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("ServiceCast")
    fun copyToClipboard(textToCopy: String) {
        Log.d("textToCopy", textToCopy)
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Label", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        // Toast 提示
        Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show()
    }
}