package com.dabenxiang.mimi

import android.app.Application
import android.util.Log
import com.dabenxiang.mimi.Constant.Companion.FLURRY_API_KEY
import com.dabenxiang.mimi.di.apiModule
import com.dabenxiang.mimi.di.appModule
import com.dabenxiang.mimi.di.managerModule
import com.dabenxiang.mimi.di.viewModelModule
import com.facebook.stetho.Stetho
import com.flurry.android.FlurryAgent
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

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
            managerModule,
            viewModelModule
        )

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(module)
        }
    }

}

class DebugLogTree : Timber.DebugTree() {

    companion object {
        const val TAG_GLOBAL = "mimi"
        const val FORMAT_MESSAGE = "%s: %s"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        var priority = priority
        var tag = tag
        var message = message

        val logFormatMsg = StringBuilder("[")
            .append(Thread.currentThread().name)
            .append(" Thread")
            .append("] ")
            .append(message)
            .toString()

        message = String.format(FORMAT_MESSAGE, tag, logFormatMsg)

        tag = TAG_GLOBAL

        super.log(priority, tag, message, t)
    }

    override fun createStackElementTag(element: StackTraceElement): String? {
        return "(${element.fileName}:${element.lineNumber})"
    }
}