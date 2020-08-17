package com.dabenxiang.mimi.di

import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.PREFS_NAME
import com.dabenxiang.mimi.model.pref.Pref
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.dsl.module

val appModule = module {
    single { provideGson() }
    single { providePref(get()) }
}

fun provideGson(): Gson {
    return GsonBuilder().create()
}

fun providePref(gson: Gson): Pref {
    return Pref(gson, PREFS_NAME, BuildConfig.DEBUG)
}
