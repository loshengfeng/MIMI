package com.dabenxiang.mimi.di

import com.dabenxiang.mimi.API_HOST_URL
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.model.api.*
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.widget.factory.EnumTypeAdapterFactory
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import io.ktor.util.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@OptIn(InternalAPI::class)
val apiModule = module {
    single { provideAuthInterceptor(get()) }
    single { provideHttpLoggingInterceptor() }
    single { provideOkHttpClient(get(), get(), get(), get()) }
    single { provideApiService(get()) }
    single { provideApiRepository(get()) }
    single { provideApiLogInterceptor() }
    single { provideEncryptionInterceptor() }
}

fun provideAuthInterceptor(pref: Pref): AuthInterceptor {
    return AuthInterceptor(pref)
}

fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val httpLoggingInterceptor = HttpLoggingInterceptor {
        if (!it.contains("�")) HttpLoggingInterceptor.Logger.DEFAULT.log(it)
        else HttpLoggingInterceptor.Logger.DEFAULT.log("base64 image")
    }
    httpLoggingInterceptor.level = when (BuildConfig.DEBUG) {
        true -> HttpLoggingInterceptor.Level.BODY
        else -> HttpLoggingInterceptor.Level.NONE
    }
    return httpLoggingInterceptor
}

@InternalAPI
fun provideOkHttpClient(
    authInterceptor: AuthInterceptor,
    encryptionInterceptor  :EncryptionInterceptor,
    httpLoggingInterceptor: HttpLoggingInterceptor,
    apiLogInterceptor: ApiLogInterceptor
): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(encryptionInterceptor)
        .addInterceptor(apiLogInterceptor)
        .addInterceptor(httpLoggingInterceptor)

    if (BuildConfig.DEBUG) {
        builder.addNetworkInterceptor(StethoInterceptor())
    }

    return builder.build()
}

fun provideApiService(okHttpClient: OkHttpClient): ApiService {
    val gson = GsonBuilder().registerTypeAdapterFactory(EnumTypeAdapterFactory()).create()
    return Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .baseUrl(API_HOST_URL)
        .build()
        .create(ApiService::class.java)
}

fun provideApiRepository(apiService: ApiService): ApiRepository {
    return ApiRepository(apiService)
}

fun provideApiLogInterceptor(): ApiLogInterceptor {
    return ApiLogInterceptor()
}

@OptIn(InternalAPI::class)
fun provideEncryptionInterceptor(): EncryptionInterceptor {
    return EncryptionInterceptor()
}

fun provideMemberPostItemDBRepository(apiService: ApiService): ApiRepository {
    return ApiRepository(apiService)
}