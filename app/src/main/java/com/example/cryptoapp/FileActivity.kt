package com.example.cryptoapp

import FileCrypto
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.activity_file.decryptButton
import kotlinx.android.synthetic.main.activity_file.encryptButton
import kotlinx.android.synthetic.main.activity_file.passwordEditText
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navView
import kotlinx.android.synthetic.main.activity_main.toolbar
import java.io.*

class FileActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_CODE_FOR_LOAD_FILE = 1
    private val REQUEST_CODE_FOR_CREATE_FILE = 2
    private var fromFileUri: Uri? = null
    private var toFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }
//        navView.setCheckedItem(R.id.navFileLayout)
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
        readFileButton.setOnClickListener(this)
        saveFileButton.setOnClickListener(this)
        encryptButton.setOnClickListener(this)
        decryptButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.readFileButton -> {
                setFromFileUri()
            }
            R.id.saveFileButton -> {
                setToFileUri()
            }
            R.id.encryptButton -> {
                if (toFileUri != null) {
                    fromFileUri?.let { fileEncrypt(it) }
                } else {
                    Toast.makeText(this, "加密输出文件路径不存在", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.decryptButton -> {
                if (toFileUri != null) {
                    fromFileUri?.let { fileDecrypt(it) }
                } else {
                    Toast.makeText(this, "解密输出文件路径不存在", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setFromFileUri() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE_FOR_LOAD_FILE)

    }

    private fun setToFileUri() {
        val intent =Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_TITLE,"saveFileName")
        startActivityForResult(intent, REQUEST_CODE_FOR_CREATE_FILE)
    }


    override fun onActivityResult(requestCode: Int,resultCode: Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_FOR_LOAD_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        readPathEditText.setText(FileUtil.uriToFileName(uri, this))
                        fromFileUri = uri
                        //Toast.makeText(this, fromFileUri.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            REQUEST_CODE_FOR_CREATE_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        savePathEditText.setText(FileUtil.uriToFileName(uri, this))
                        toFileUri = uri
                        //Toast.makeText(this, toFileUri.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun fileEncrypt(uri: Uri){
        val inputFileResolver = contentResolver.openFileDescriptor(uri, "r")
        val outputFileResolver = contentResolver.openFileDescriptor(toFileUri!!, "w")
        val password = passwordEditText.text.toString()
        if (outputFileResolver != null) {
            val outputFileStream = FileOutputStream(outputFileResolver.fileDescriptor)
            inputFileResolver?.fileDescriptor?.let {
                val fileInputStream = FileInputStream(it)
                val successFlag = FileCrypto(password).encrypt(fileInputStream, outputFileStream)
                if (successFlag) {
                    Toast.makeText(this, "文件加密已完成", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "文件加密发生错误", Toast.LENGTH_SHORT).show()
                }
                fileInputStream.close()
            }
            outputFileStream.close()
        }
        inputFileResolver?.close()
        outputFileResolver?.close()
    }

    private fun fileDecrypt(uri: Uri){
        val inputFileResolver = contentResolver.openFileDescriptor(uri, "r")
        val outputFileResolver = contentResolver.openFileDescriptor(toFileUri!!, "w")
        val password = passwordEditText.text.toString()
        if (outputFileResolver != null) {
            val outputFileStream = FileOutputStream(outputFileResolver.fileDescriptor)
            inputFileResolver?.fileDescriptor?.let {
                val fileInputStream = FileInputStream(it)
                val successFlag = FileCrypto(password).decrypt(fileInputStream, outputFileStream)
                if (successFlag) {
                    Toast.makeText(this, "文件解密已完成", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "文件解密发生错误", Toast.LENGTH_SHORT).show()
                }
                fileInputStream.close()
            }
            outputFileStream.close()
        }
        inputFileResolver?.close()
        outputFileResolver?.close()
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