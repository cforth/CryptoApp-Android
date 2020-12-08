package com.example.cryptoapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navView
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_pic.*
import kotlinx.android.synthetic.main.activity_pic.passwordEditText
import kotlinx.android.synthetic.main.activity_text.*
import java.io.FileInputStream
import java.nio.ByteBuffer


class PicActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_CODE_FOR_LOAD_FILE = 1
    private var fromFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }

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

        readPicButton.setOnClickListener(this)
        decryptPicButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.readPicButton -> {
                setFromFileUri()
            }
            R.id.decryptPicButton -> {
                picDecryptShow()
            }
        }
    }

    private fun setFromFileUri() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_FOR_LOAD_FILE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_FOR_LOAD_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        readPicEditText.setText(FileUtil.uriToFileName(uri, this))
                        fromFileUri = uri
                    }
                }
            }
        }
    }

    private fun picDecryptShow() {
        if (fromFileUri != null) {
            val inputFileResolver = contentResolver.openFileDescriptor(fromFileUri!!, "r")
            val password = passwordEditText.text.toString()
            inputFileResolver?.fileDescriptor?.let {
                val fileInputStream = FileInputStream(it)
                // 设置图片缓冲区，最大为20M
                val fromFileByteArray = ByteArray(20971520)
                val len = fileInputStream.read(fromFileByteArray)
                try {
                    val decryptPicByteArray = BytesCrypto(password).decrypt(fromFileByteArray.sliceArray(0 until len))
                    // 将字节数组转为Bitmap
                    val decryptPicBitmap = BitmapFactory.decodeByteArray(decryptPicByteArray, 0, decryptPicByteArray.size)
                    decryptImageView.setImageBitmap(decryptPicBitmap)
                } catch (e: Exception) {
                    Log.d("Decrypt Error", "decrypt img exception")
                    decryptImageView.setImageBitmap(BitmapFactory.decodeResource(this.resources, R.drawable.error_img))
                    Snackbar.make(decryptImageView, "解密图片时出现错误", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
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