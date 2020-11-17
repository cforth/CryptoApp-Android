package com.example.cryptoapp

import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_text.*
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class FileCrypto(_password: String, _useMD5:Boolean=true, _useUrlSafe:Boolean=true) {
    private val useUrlSafe = _useUrlSafe
    //根据参数选择是否转为MD5值
    private val passByteArray = if (_useMD5) MessageDigestUtils.md5(_password) else _password.toByteArray()
    //初始化:加密/解密
    private val keySpec: SecretKeySpec = SecretKeySpec(passByteArray,"AES")
    //创建cipher对象
    private val cipher = Cipher.getInstance("AES")

    //文件处理
    private fun fileHandler(inputFile:File, outputFile:File,
                    eachBlockHandler:(ByteArray)-> ByteArray,
                    endBlockHandler:(ByteArray)-> ByteArray,
                    blockSize:Int=10*1024*1024): Boolean {

        val fileProgress = FileHandlerProgress(inputFile.length())

        if (inputFile.exists()) {
            if (!outputFile.exists()) {
                try {
                    inputFile.forEachBlock (blockSize) { fileBytes: ByteArray, bytesRead: Int ->
                        if (bytesRead == blockSize) {
                            outputFile.appendBytes(eachBlockHandler(fileBytes))
                        } else {
                            outputFile.appendBytes(endBlockHandler(fileBytes.sliceArray(0 until bytesRead)))
                        }
                        fileProgress.update(bytesRead.toLong())
                        // fileProgress.getStatus()
                    }
                } catch (e: Exception) {
                    Log.d("Encrypt Error", "encrypt text exception")
                    return false
                }

            } else {
                println("outputFile is exists!")
                return false
            }
        } else {
            println("inputFile is not exists!")
            return false
        }
        return true
    }

    private fun update(fileBytes: ByteArray):ByteArray {
        return cipher.update(fileBytes)
    }

    private fun doFinal(fileBytes: ByteArray):ByteArray {
        return cipher.doFinal(fileBytes)
    }

    //加密
    fun encrypt(inputFile:File, outputFile:File): Boolean {
        cipher.init(Cipher.ENCRYPT_MODE,keySpec)
        return fileHandler(inputFile, outputFile, ::update, ::doFinal)
    }

    //解密
    fun decrypt(inputFile:File, outputFile:File): Boolean {
        cipher.init(Cipher.DECRYPT_MODE,keySpec)
        return fileHandler(inputFile, outputFile, ::update, ::doFinal)
    }
}
