package com.dabenxiang.mimi.manager.update

import com.dabenxiang.mimi.manager.update.api.UpdateApiRepository
import com.dabenxiang.mimi.manager.update.api.UpdateApiService
import com.dabenxiang.mimi.manager.update.api.UpdateAuthInterceptor
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

class UpdateDomainManager() : BaseDomainManager(), KoinComponent {
    override val projectId: String = DOWNLOAD_SERVER_PROJECT_ID
    override val defaultDomain: String = "weishanglt.com"
    private val interceptor: UpdateAuthInterceptor by inject()
    private val httpLoggingInterceptor: HttpLoggingInterceptor by inject()
    private val updateOkHttpClient: OkHttpClient = updateOkHttpClient(interceptor, httpLoggingInterceptor)

    fun getApiRepository(): UpdateApiRepository {
        val apiService = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(updateOkHttpClient)
            .baseUrl(getApiDomain())
            .build()
            .create(UpdateApiService::class.java)
        Timber.i("apiService= $apiService")
        return UpdateApiRepository(
            apiService
        )
    }


    fun getApiDomain(): String {
        val result = StringBuilder()
        result.append("https://")
            .append("api")
            .append(".")
            .append(if(getLibEnv()=="d") defaultDomain else getDomain())
            .toString()
        return result.toString()
    }

    private fun updateOkHttpClient(
        interceptor: Interceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(httpLoggingInterceptor)

        if (BaseManagerData.configData?.debug == true) {
            builder.addNetworkInterceptor(StethoInterceptor())
        }

        return builder.build()
    }

}