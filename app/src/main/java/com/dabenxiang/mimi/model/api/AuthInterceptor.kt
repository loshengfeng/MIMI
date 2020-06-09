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
        //Timber.d(chain.request().url.toString())
        val urlString = chain.request().url.toString()
        if (urlString.endsWith("/token")) {
            return chain.proceed(chain.request())
        }

        var userMemberToken = accountManager.hasMemberToken()
        if (userMemberToken) {
            userMemberToken = when {
                urlString.endsWith("/signin") -> false
                else -> true
            }
        }

        if (userMemberToken) {
            when (accountManager.getMemberTokenResult()) {
                TokenResult.Expired -> doRefreshToken()
            }
        } else {
            when (accountManager.getPublicTokenResult()) {
                TokenResult.Empty, TokenResult.Expired -> getPublicToken()
            }
        }

        var response: Response? = null

        try {
            response = chain.proceed(chain.addAuthorization(userMemberToken))

            return when (response.code) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    response.close()
                    if (userMemberToken) {
                        doRefreshToken()
                    } else {
                        getPublicToken()
                    }

                    chain.proceed(chain.addAuthorization(userMemberToken))
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
                    chain.proceed(chain.addAuthorization(userMemberToken))
                }
                else -> chain.proceed(chain.addAuthorization(userMemberToken))
            }
        }
    }

    private fun Interceptor.Chain.addAuthorization(userMember: Boolean): Request {
        val requestBuilder = request().newBuilder()

        val auth = ApiRepository.BEARER +
                if (userMember) {
                    pref.memberToken.accessToken
                } else {
                    pref.publicToken.accessToken
                }

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
