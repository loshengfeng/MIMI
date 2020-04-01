package com.dabenxiang.mimi.di

import android.content.Context
import com.dabenxiang.mimi.model.mqtt.MQTTManager
import org.koin.dsl.module

val managerModule = module {
    single { provideMQTTManager(get()) }
}

fun provideMQTTManager(context: Context): MQTTManager {
    return MQTTManager(context)
}
