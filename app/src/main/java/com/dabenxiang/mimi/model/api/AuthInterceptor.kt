package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.pref.Pref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class AuthInterceptor(private val pref: Pref) : Interceptor, KoinComponent {

    private val domainManager: DomainManager by inject()
    private val accountManager: AccountManager by inject()

    override fun intercept(chain: Interceptor.Chain): Response {

        val urlString = chain.request().url.toString()
        if (urlString.endsWith("/token")) {
            return chain.proceed(chain.request())
        }

        var hasMemberToken = accountManager.hasMemberToken()
        if (hasMemberToken) {
            hasMemberToken = when {
                urlString.endsWith("/signin") -> false
                else -> true
            }
        }

        if (hasMemberToken) {
            when (accountManager.getMemberTokenResult()) {
                TokenResult.EXPIRED -> doRefreshToken()
                else -> {
                }
            }
        } else {
            when (accountManager.getPublicTokenResult()) {
                TokenResult.EMPTY, TokenResult.EXPIRED -> getPublicToken()
                else -> {
                }
            }
        }

        var response: Response? = null

        try {
            response = chain.proceed(chain.addAuthorization(hasMemberToken))

            return when (response.code) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    response.close()
                    if (hasMemberToken) {
                        doRefreshToken()
                    } else {
                        getPublicToken()
                    }

                    chain.proceed(chain.addAuthorization(hasMemberToken))
                }
                else -> response
            }

        } catch (e: Exception) {
            response?.close()
            return when (e) {
                is UnknownHostException,
                is SocketTimeoutException,
                is SSLHandshakeException -> {
                    domainManager.changeApiDomainIndex()
                    chain.proceed(chain.addAuthorization(hasMemberToken))
                }
                else -> chain.proceed(chain.addAuthorization(hasMemberToken))
            }
        }
    }

    private fun Interceptor.Chain.addAuthorization(hasMemberToken: Boolean): Request {
        val requestBuilder = request().newBuilder()
        val accessToken = when {
            hasMemberToken -> pref.memberToken.accessToken
            else -> pref.publicToken.accessToken
        }
        val auth = StringBuilder(ApiRepository.BEARER).append(accessToken).toString()
        requestBuilder.addHeader(ApiRepository.AUTHORIZATION, auth)
        return requestBuilder.build()
    }

    private fun doRefreshToken() {
        runBlocking(Dispatchers.IO) {
            accountManager.refreshToken()
                .collect {
                    when (it) {
                        is ApiResult.Empty -> Timber.d("Refresh token successful!")
                        is ApiResult.Error -> Timber.e("Refresh token error: $it")
                    }
                }
        }
    }

    private fun getPublicToken() {
        runBlocking(Dispatchers.IO) {
            accountManager.getPublicToken()
                .collect {
                    when (it) {
                        is ApiResult.Empty -> Timber.d("Get token successful!")
                        is ApiResult.Error -> Timber.e("Get token error: $it")
                    }
                }
        }
    }
}
