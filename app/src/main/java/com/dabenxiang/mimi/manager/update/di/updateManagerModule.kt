package com.dabenxiang.mimi.manager.update.di

import org.koin.dsl.module
import com.dabenxiang.mimi.manager.update.APKDownloaderManager
import com.dabenxiang.mimi.manager.update.api.UpdateAuthInterceptor

@JvmField
val updatemanagerModule = module {
    single { provideUpdateAuthInterceptor() }
    single { provideDownloaderManager() }
}

fun provideUpdateAuthInterceptor(): UpdateAuthInterceptor {
    return UpdateAuthInterceptor(
    )
}

fun provideDownloaderManager(): APKDownloaderManager {
    return APKDownloaderManager(
    )
}

