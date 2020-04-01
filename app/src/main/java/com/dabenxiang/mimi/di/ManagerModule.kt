package com.dabenxiang.mimi.di

import com.dabenxiang.mimi.model.mqtt.MQTTManager
import org.koin.dsl.module

val managerModule = module {
    single { provideMQTTManager() }
}

fun provideMQTTManager(): MQTTManager {
    return MQTTManager()
}
