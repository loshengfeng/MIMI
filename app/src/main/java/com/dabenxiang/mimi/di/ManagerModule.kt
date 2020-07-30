package com.dabenxiang.mimi.di

import android.content.Context
import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.manager.update.UpdateDomainManager
import com.dabenxiang.mimi.manager.update.VersionManager
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.widget.factory.EnumTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.dsl.module

val managerModule = module {
    single { provideDomainManager(get()) }
    single { provideMQTTManager(get(), get()) }
    single { provideAccountManager(get(), get()) }
    single { provideUpdateApiDomainManager() }
    single { provideVersionManager() }
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

fun provideUpdateApiDomainManager(): UpdateDomainManager {
    return UpdateDomainManager()
}

fun provideVersionManager(): VersionManager {
    return VersionManager()
}
