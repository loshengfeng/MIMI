package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.pref.Pref
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import timber.log.Timber

class AuthInterceptor(private val pref: Pref) : Interceptor, KoinComponent {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        Timber.d("Response code: ${response.code}")
        return response
    }
}
