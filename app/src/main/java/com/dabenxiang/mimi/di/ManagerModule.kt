package com.dabenxiang.mimi.di

import android.content.Context
import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.manager.LruCacheManager
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.widget.factory.EnumTypeAdapterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.dsl.module

val managerModule = module {
    single { provideDomainManager(get()) }
    single { provideMQTTManager(get(), get()) }
    single { provideAccountManager(get(), get()) }
    single { provideLruCacheManager() }
}

fun provideDomainManager(okHttpClient: OkHttpClient): DomainManager {
    val gson = GsonBuilder().registerTypeAdapterFactory(EnumTypeAdapterFactory()).create()
    return DomainManager(gson, okHttpClient)
}

fun provideMQTTManager(context: Context, pref: Pref): MQTTManager {
    return MQTTManager(context, pref)
}

fun provideAccountManager(pref: Pref, domainManager: DomainManager): AccountManager {
    return AccountManager(pref, domainManager)
}

fun provideLruCacheManager(): LruCacheManager {
    return LruCacheManager()
}
