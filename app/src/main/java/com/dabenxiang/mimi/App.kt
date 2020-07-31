package com.dabenxiang.mimi

import android.app.Application
import android.util.Log
import com.dabenxiang.mimi.di.apiModule
import com.dabenxiang.mimi.di.appModule
import com.dabenxiang.mimi.di.managerModule
import com.dabenxiang.mimi.widget.log.DebugLogTree
import com.facebook.stetho.Stetho
import com.flurry.android.FlurryAgent
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import tw.gov.president.manager.data.ConfigData
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager
import tw.gov.president.manager.submanager.update.di.UpdateManagerProvider

class App : Application() {

    companion object {
        lateinit var self: Application
        fun applicationContext(): Application {
            return self
        }
    }

    init {
        self = this
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugLogTree())
            Stetho.initializeWithDefaults(this)
        } else {
            FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                .withContinueSessionMillis(10000)
                .withLogLevel(Log.VERBOSE)
                .build(this, FLURRY_API_KEY)
        }

        val module = listOf(
            appModule,
            apiModule,
            managerModule
        )

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(module)
        }

        val configData = ConfigData(
            BuildConfig.API_HOST,
            BuildConfig.FLAVOR,
            BuildConfig.BUILD_TYPE,
            BuildConfig.DEBUG,
            BuildConfig.APPLICATION_ID,
            BuildConfig.VERSION_CODE.toString(),
            BuildConfig.VERSION_CODE.toLong()
        )
        SendLogManager.init(configData)
        UpdateManagerProvider.init(configData)
    }
}