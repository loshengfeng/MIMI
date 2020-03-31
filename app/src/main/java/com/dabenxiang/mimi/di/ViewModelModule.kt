package com.dabenxiang.mimi.di

import com.dabenxiang.mimi.view.login.LoginViewModel
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel() }
    viewModel { SplashViewModel() }
    viewModel { LoginViewModel() }
}
