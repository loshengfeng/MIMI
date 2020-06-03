package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.pref.Pref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.net.HttpURLConnection

class AuthInterceptor(private val pref: Pref) : Interceptor, KoinComponent {

    private val accountManager: AccountManager by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        //Timber.d(chain.request().url.toString())
        if (chain.request().url.toString().endsWith("/token")) {
            return chain.proceed(chain.request())
        }

        val isAutoLogin = accountManager.isAutoLogin()
        if (isAutoLogin) {
            when (accountManager.getMemberTokenResult()) {
                TokenResult.Expired -> {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            accountManager.refreshToken().collect()
                        }
                    }
                }
            }
        } else {
            when (val r = accountManager.getPublicTokenResult()) {
                TokenResult.Empty, TokenResult.Expired -> {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            accountManager.getPublicToken().collect()
                        }
                    }
                }
            }
        }

        val response = chain.proceed(chain.addAuthorization(isAutoLogin))
        return when (response.code) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        accountManager.refreshToken().collect()
                    }
                    // Prod crash when not call close
                    response.close()
                    chain.proceed(chain.addAuthorization(isAutoLogin))
                }
            }
            else -> response
        }
    }

    private fun Interceptor.Chain.addAuthorization(isLogin: Boolean): Request {
        val requestBuilder = request().newBuilder()
        if (isLogin) {
            requestBuilder.addHeader("Authorization", "Bearer ${pref.memberToken.accessToken}")
        } else {
            requestBuilder.addHeader("Authorization", "Bearer ${pref.publicToken.accessToken}")
        }

        return requestBuilder.build()
    }
}
