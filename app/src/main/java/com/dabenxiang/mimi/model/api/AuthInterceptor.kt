package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.manager.AccountManager
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
        return when {
            chain.request().url.toString().endsWith("/token") -> {
                return chain.proceed(chain.request())
            }
            //TODO: 是否已登入

            !accountManager.isTokenValid() -> {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        accountManager.getPublicToken().collect()
                    }
                    chain.proceed(chain.addAuthorization())
                }
            }
            else -> {
                val response = chain.proceed(chain.addAuthorization())
                return when (response.code) {
                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                accountManager.refreshToken().collect()
                            }
                            // Prod crash when not call close
                            response.close()
                            chain.proceed(chain.addAuthorization())
                        }
                    }
                    else -> response
                }
            }
        }
    }

    private fun Interceptor.Chain.addAuthorization(): Request {
        val requestBuilder = request().newBuilder()
        requestBuilder.addHeader("Authorization", "Bearer ${pref.publicToken.accessToken}")
        return requestBuilder.build()
    }
}
