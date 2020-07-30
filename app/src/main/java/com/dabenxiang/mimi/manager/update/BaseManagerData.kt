package com.dabenxiang.mimi.manager.update

import com.github.florent37.application.provider.ApplicationProvider
import tw.gov.president.manager.data.ConfigData

object BaseManagerData {
    val App = ApplicationProvider.application
    var configData: ConfigData? = null
}


enum class AppType {
    COMMON, NONE_KOIN, NONE_KOIN_JAVA
}
