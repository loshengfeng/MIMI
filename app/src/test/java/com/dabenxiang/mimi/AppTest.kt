package com.dabenxiang.mimi
import com.dabenxiang.mimi.di.apiModule
import com.dabenxiang.mimi.di.appModule
import com.dabenxiang.mimi.di.managerModule
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import org.junit.Test

import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.inject

class AppTest : KoinTest {

    val accountManager: AccountManager by inject()
    val domainManager: DomainManager by inject()

    @Test
    fun doTest() {

        val module = listOf(
            appModule,
            apiModule,
            managerModule
        )

        startKoin {
            modules(module)
        }

        val keep = accountManager.keepAccount

    }
}
