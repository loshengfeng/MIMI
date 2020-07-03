package com.dabenxiang.mimi.widget.utility

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.widget.Toast
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.vo.error.ErrorItem
import com.dabenxiang.mimi.model.api.vo.error.HttpExceptionItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.util.regex.Pattern

object GeneralUtils {

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("HardwareIds")
    fun getAndroidID(): String {
        return Settings.Secure.getString(
            App.applicationContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun getHttpExceptionData(httpException: HttpException): HttpExceptionItem {
        val oriResponse = httpException.response()

        val url = oriResponse?.raw()?.request?.url.toString()

        val errorBody = oriResponse?.errorBody()
        val jsonStr = errorBody?.string()
        val type = object : TypeToken<ErrorItem>() {}.type

        val errorItem: ErrorItem = try {
            Gson().fromJson(jsonStr, type)
        } catch (e: Exception) {
            e.printStackTrace()
            ErrorItem(null, null, null)
        }

        val responseBody = Gson().toJson(
            ErrorItem(
                errorItem.code,
                errorItem.message,
                null
            )
        )
            .toResponseBody(ApiRepository.MEDIA_TYPE_JSON.toMediaTypeOrNull())

        val rawResponse = okhttp3.Response.Builder()
            .code(httpException.code())
            .message(httpException.message())
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url(url).build())
            .build()

        val response = Response.error<ErrorItem>(responseBody, rawResponse)

        val httpExceptionClone = HttpException(response)
        return HttpExceptionItem(
            errorItem,
            httpExceptionClone,
            url
        )
    }

    fun getLibEnv(): String {
        return when (BuildConfig.FLAVOR) {
            DomainManager.FLAVOR_DEV -> "d"
            DomainManager.FLAVOR_SIT -> "s"
            else -> "p"
        }
    }

    fun isFriendlyNameValid(name: String): Boolean {
        return Pattern.matches(
            "^[a-zA-Z0-9-\\u4e00-\\u9fa5-`\\[\\]~!@#\$%^&*()_+{}|:”<>?`\\[\\];’,./\\\\]{1,20}+$",
            name
        )
    }

    fun isEmailValid(email: String): Boolean {
        return Pattern.matches(
            "^[A-Za-z0-9_\\-\\.\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*$",
            email
        )
    }

    fun isAccountValid(account: String): Boolean {
        return Pattern.matches("^[a-zA-Z0-9]{5,20}$", account)
    }

    fun isPasswordValid(pwd: String): Boolean {
        return Pattern.matches(
            "^[a-zA-Z0-9-`\\[\\]~!@#\$%^&*()_+\\-=;',./?<>{}|:\"\\\\]{8,20}+$",
            pwd
        )
    }
}