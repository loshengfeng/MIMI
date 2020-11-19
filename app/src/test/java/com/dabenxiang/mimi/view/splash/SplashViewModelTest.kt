package com.dabenxiang.mimi.view.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer

import com.dabenxiang.mimi.di.apiModule
import com.dabenxiang.mimi.di.appModule
import com.dabenxiang.mimi.di.managerModule
import com.dabenxiang.mimi.di.viewModelModule
import com.dabenxiang.mimi.view.main.MainViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import tw.gov.president.manager.submanager.update.VersionManager
import tw.gov.president.manager.submanager.update.data.VersionStatus

class SplashViewModelTest: KoinTest {

    private val splashViewModel: SplashViewModel by inject()

    private lateinit var versionManager: VersionManager
    @Mock
    lateinit var versionObserver: Observer<VersionStatus>


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

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(mainThreadSurrogate)
        versionManager = mock()
        runBlocking {
            whenever(versionManager.checkVersion()).thenReturn(VersionStatus.DEFAULT)
        }
        versionObserver = mock()
    }

    @After
    fun after() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }


    @Test
    fun testCheckVersion() {
        runBlocking {
            `when`(versionManager.checkVersion()).thenReturn(VersionStatus.DEFAULT)
            splashViewModel.versionStatus.observeForever(versionObserver)
            splashViewModel.checkVersion()


            Mockito.verify(versionObserver).onChanged(VersionStatus.DEFAULT)
            Mockito.verify(versionObserver).onChanged(VersionStatus.UNCHANGED)
            Mockito.verify(versionObserver).onChanged(VersionStatus.FORCE_UPDATE)
            Mockito.verify(versionObserver).onChanged(VersionStatus.UPDATE)
        }

    }
}