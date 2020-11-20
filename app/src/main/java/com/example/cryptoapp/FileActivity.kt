package com.example.cryptoapp

import FileCrypto
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class FileActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_CODE_FOR_LOAD_FILE = 1
    private val REQUEST_CODE_FOR_CREATE_FILE = 2
    private var fromFileUri: Uri? = null
    private var toFileUri: Uri? = null
    private val startProgress = 1
    private val finishProgress = 2
    private val cryptoFinishToastMessage = 3
    private val cryptoErrorToastMessage = 4
    private val handler = MyHandler(this)

    //防止Handler造成的内存泄露，使用内部类
    private class MyHandler(activity: FileActivity) : Handler() {
        private val mActivity: WeakReference<FileActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            if (mActivity.get() == null) {
                return
            }
            val activity = mActivity.get()
            if (activity != null) {
                when (msg.what) {
                    activity.startProgress -> activity.progressBar.visibility = View.VISIBLE
                    activity.finishProgress -> activity.progressBar.visibility = View.GONE
                    activity.cryptoFinishToastMessage -> Toast.makeText(activity, "任务已完成", Toast.LENGTH_SHORT).show()
                    activity.cryptoErrorToastMessage -> Toast.makeText(activity, "任务发生错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                fileCryptoTask("ENCRYPT")
            }
            R.id.decryptButton -> {
                fileCryptoTask("DECRYPT")
            }
        }
    }

    private fun fileCryptoTask(option: String) {
        if (toFileUri != null && fromFileUri != null) {
            thread {
                val startMsg = Message()
                startMsg.what = startProgress
                handler.sendMessage(startMsg)
                val flag = fileHandle(fromFileUri!!, option)
                println(flag)
                val finishMsg = Message()
                finishMsg.what = finishProgress
                handler.sendMessage(finishMsg)
                if (flag) {
                    val cryptoFinishMsg = Message()
                    cryptoFinishMsg.what = cryptoFinishToastMessage
                    handler.sendMessage(cryptoFinishMsg)
                } else {
                    val cryptoErrorMsg = Message()
                    cryptoErrorMsg.what = cryptoErrorToastMessage
                    handler.sendMessage(cryptoErrorMsg)
                }
            }
        } else {
            Toast.makeText(this, "输入或输出路径错误", Toast.LENGTH_SHORT).show()
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

    private fun fileHandle(uri: Uri, option: String): Boolean {
        val inputFileResolver = contentResolver.openFileDescriptor(uri, "r")
        val outputFileResolver = contentResolver.openFileDescriptor(toFileUri!!, "w")
        val password = passwordEditText.text.toString()
        var successFlag = false
        if (outputFileResolver != null) {
            val outputFileStream = FileOutputStream(outputFileResolver.fileDescriptor)
            inputFileResolver?.fileDescriptor?.let {
                val fileInputStream = FileInputStream(it)
                if (option == "ENCRYPT") {
                    successFlag = FileCrypto(password).encrypt(fileInputStream, outputFileStream)
                } else if (option == "DECRYPT"){
                    successFlag = FileCrypto(password).decrypt(fileInputStream, outputFileStream)
                }
                fileInputStream.close()
            }
            outputFileStream.close()
        }
        inputFileResolver?.close()
        outputFileResolver?.close()
        return successFlag
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
