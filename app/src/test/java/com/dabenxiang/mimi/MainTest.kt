package com.dabenxiang.mimi

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer

import com.dabenxiang.mimi.di.apiModule
import com.dabenxiang.mimi.di.appModule
import com.dabenxiang.mimi.di.managerModule
import com.dabenxiang.mimi.di.viewModelModule
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.splash.SplashViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tw.gov.president.manager.submanager.update.VersionManager
import tw.gov.president.manager.submanager.update.data.VersionStatus

class MainTest: KoinTest {



    @get:Rule
    val koinTestRule = KoinTestRule.create {
        val module = listOf(
            appModule,
            apiModule,
            managerModule,
            viewModelModule

        )
        modules(module)
    }

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)

    }

    @After
    fun after() {
    }

    private val mainViewModel: MainViewModel by inject()
    private val splashViewModel: SplashViewModel by inject()

    @Mock
    lateinit var version: Observer<VersionStatus>

    @get:Rule
    val rule = InstantTaskExecutorRule()

    val accountManager: AccountManager by inject()
    val domainManager: DomainManager by inject()


    @Test
    fun doTest() {

        splashViewModel.versionStatus.observeForever(version)

        splashViewModel.checkVersion()
//        val status =splashViewModel.versionStatus.value ?: error("No value from checkVersion")
//        print("versionStatus= $status")
        Mockito.verify(version).onChanged(splashViewModel.versionStatus.value)
    }
}