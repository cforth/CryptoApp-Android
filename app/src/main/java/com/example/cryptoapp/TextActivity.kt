package com.example.cryptoapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navView
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.android.synthetic.main.activity_text.decryptButton
import kotlinx.android.synthetic.main.activity_text.encryptButton
import kotlinx.android.synthetic.main.activity_text.passwordEditText

class TextActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }
//        navView.setCheckedItem(R.id.navTextLayout)
        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navTextLayout -> {
                    val intent = Intent(this, TextActivity::class.java)
                    this.finish()
                    startActivity(intent)
                }
                R.id.navFileLayout -> {
                    val intent = Intent(this, FileActivity::class.java)
                    this.finish()
                    startActivity(intent)
                }
                R.id.navPicLayout -> {
                    val intent = Intent(this, PicActivity::class.java)
                    this.finish()
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }
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
                } catch (e: Exception) {
                    Log.d("Encrypt Error", "encrypt text exception")
                    outputEditText.setText("")
                    Snackbar.make(textView, "加密文本时出现错误", Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.decryptButton -> {
                val password = passwordEditText.text.toString()
                val input = inputEditText.text.toString()
                try {
                    val d = StringCrypto(password).decrypt(input)
                    outputEditText.setText(d)
                } catch (e: Exception) {
                    Log.d("Decrypt Error", "decrypt text exception")
                    outputEditText.setText("")
                    Snackbar.make(textView, "解密文本时出现错误", Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.copyOutputButton -> {
                try {
                    val textToCopy = outputEditText.text.toString()
                    copyToClipboard(textToCopy)
                } catch (e: Exception) {
                    Log.d("Copy Output Error", "copy text exception")
                    outputEditText.setText("")
                    Snackbar.make(textView, "复制输出文本时出现错误", Snackbar.LENGTH_SHORT).show()
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
        Snackbar.make(textView, "输出文本已复制", Snackbar.LENGTH_SHORT).show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}