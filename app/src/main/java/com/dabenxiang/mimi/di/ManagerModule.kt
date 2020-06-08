package com.dabenxiang.mimi.di

import android.content.Context
import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.widget.utility.EnumTypeAdapterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.dsl.module

val managerModule = module {
    single { provideDomainManager(get()) }
    single { provideMQTTManager(get()) }
    single { AccountManager(get(), get()) }
}

fun provideDomainManager(okHttpClient: OkHttpClient): DomainManager {
    val gson = GsonBuilder().registerTypeAdapterFactory(EnumTypeAdapterFactory()).create()
    return DomainManager(gson, okHttpClient)
}

fun provideMQTTManager(context: Context): MQTTManager {
    return MQTTManager(context)
}
