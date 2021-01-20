package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.coremedia.iso.Hex
import io.ktor.util.*
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.io.File
import java.security.InvalidKeyException
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptUtils {
    external fun cEcbEncrypt(str: String?): String?
    external fun cEcbDecrypt(str: String?): String?
    external fun cEncrypt(str: String?): String?
    external fun cDecrypt(str: String?): String?
    external fun cIsVerify(): Boolean

    external fun jniencrypt(bytes: ByteArray): String?

    external fun jnidecrypt(str: String): ByteArray?

//    external fun pwdMD5(str: String?): String?

    fun encrypt(str: String): String? {
        return jniencrypt(str.toByteArray())
            ?.let { base64Str->
            val decoded: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getDecoder().decode(base64Str)
            } else {
                Base64.decode(base64Str, Base64.DEFAULT)
            }
            Hex.encodeHex(decoded)
        }
    }

    @OptIn(InternalAPI::class)
    fun decrypt(str: String): String? {
        val decryptBase64 =str.chunked(2).map {
            it.toInt(16).toByte()
        }.toByteArray().encodeBase64()
//        val decryptBase64 = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//             Hex.decodeHex(str).encodeBase64()
//        } else {
//            Base64.encode(Hex.decodeHex(str), Base64.DEFAULT)
//        }

        Timber.i("Encryption intercept: decryptBase64:$decryptBase64")

        return String(jnidecrypt(decryptBase64)!!)
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun encodeBase64(text: String): String {
        return Base64.encodeToString(text.toByteArray(), Base64.DEFAULT)
    }

    fun decodeBase64(encode: String): String {
        return String(Base64.decode(encode, Base64.DEFAULT))
    }

    fun encryptFile(context: Context, filesDir: String, fileName: String, data: String): Boolean {
        return try {
            deleteFile(filesDir, fileName)
            val encryptedFile = getEncryptedFile(context, filesDir, fileName)
            encryptedFile.openFileOutput().use { output ->
                output.write(data.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decryptFile(context: Context, filesDir: String, fileName: String): ByteArray {
        return try {
            val encryptedFile = getEncryptedFile(context, filesDir, fileName)
            encryptedFile.openFileInput().use { input ->
                input.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "".toByteArray()
        }
    }

    fun getEncryptedPref(context: Context, fileName: String): SharedPreferences {
        return EncryptedSharedPreferences.create(
            fileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getEncryptedFile(
        context: Context,
        filesDir: String,
        fileName: String
    ): EncryptedFile {
        return EncryptedFile.Builder(
            File(filesDir, fileName),
            context,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    private fun deleteFile(filesDir: String, fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) file.delete()
    }

    fun decryptWithCEBNoPadding(byteArray: ByteArray, key: ByteArray= "1234567890123456".toByteArray()): ByteArray {
        val cipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
        return try {
            val secretKey = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            cipher.doFinal(byteArray)
        } catch (e: InvalidKeyException) {
            byteArray
        }
    }

    fun decryptWithCEBNoPadding(byteArray: ByteArray, key: String): ByteArray {
        return decryptWithCEBNoPadding(byteArray, key.toByteArray())
    }

}
