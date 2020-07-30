package com.dabenxiang.mimi.manager.update.di

import org.koin.core.context.loadKoinModules
import tw.gov.president.manager.BaseManagerData
import tw.gov.president.manager.data.ConfigData

object UpdateManagerProvider {
    fun init(configData: ConfigData) {
        BaseManagerData.configData = configData
        listOf(updatemanagerModule).also {
            loadKoinModules(it)
        }
    }
}