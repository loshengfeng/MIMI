package com.dabenxiang.mimi.manager.update.api

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.manager.update.BaseDomainManager
import com.dabenxiang.mimi.manager.update.BaseDomainManager.Companion.AUTHORIZATION
import com.dabenxiang.mimi.manager.update.BaseDomainManager.Companion.BEARER_PREFIX
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import tw.gov.president.manager.BaseManagerData
import tw.gov.president.manager.pref.ManagersPref
import com.dabenxiang.mimi.manager.update.UpdateDomainManager
import java.net.UnknownHostException

class UpdateAuthInterceptor() : Interceptor, KoinComponent {
    private val updateDomainManager: UpdateDomainManager by inject()
    private val pref: ManagersPref by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        var response: Response? = null
        val request = chain.request()
        val url = request.url

        val newRequest = when {
            checkNotAuthApiPath(url.toString()) -> request
            else -> buildRequest(request)
        }

        try {
            response = chain.proceed(newRequest)
            Timber.d("Response code: ${response.code}")
            return response
        } catch (e: Exception) {
            response?.close()
            return when (e) {
                is UnknownHostException -> {
                    updateDomainManager.changeApiDomainIndex()
                    if (checkNotAuthApiPath(url.toString())) {
                        chain.proceed(newRequest)
                    } else {
                        chain.proceed(buildRequest(newRequest))
                    }
                }
                else -> chain.proceed(newRequest)
            }
        }
    }

    private fun buildRequest(request: Request): Request {
        val host = request.url.host
        val newDomain = updateDomainManager.getApiDomain()
        Timber.d("newDomain: $newDomain")
        val newHost = Uri.parse(newDomain)?.host.toString()
        Timber.d("newHost: $newHost")
        val requestBuilder = request.newBuilder()
        val token = pref.updateToken
        Timber.d("token: $token")
        if (!TextUtils.isEmpty(token.accessToken)) {

            val auth = StringBuilder(BEARER_PREFIX)
                .append(token.accessToken)
                .toString()
            requestBuilder.addHeader(AUTHORIZATION, auth)
        }

        if (!TextUtils.isEmpty(newHost) && !TextUtils.equals(host, newHost)) {
            val newUrl = request.url.newBuilder().host(newHost).build()
            Timber.d("newUrl: $newUrl")
            requestBuilder.url(newUrl)
        }

        return requestBuilder.build()
    }

    private fun checkNotAuthApiPath(url: String): Boolean {
        return checkApiPath(BaseManagerData.App!!, url, R.array.service_not_auth_api_path)
    }

    private fun checkApiPath(context: Context, url: String, id: Int): Boolean {
        val notAuthApiPaths = context.resources.getStringArray(id)
        for (notAuthApiPath in notAuthApiPaths) {
            if (url.contains(notAuthApiPath)) {
                return true
            }
        }
        return false
    }
}
