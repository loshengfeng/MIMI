package com.dabenxiang.mimi.di

import com.dabenxiang.mimi.view.favorite.FavoriteViewModel
import com.dabenxiang.mimi.view.login.LoginViewModel
import com.dabenxiang.mimi.view.home.HomeViewModel
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.messenger.MessengerViewModel
import com.dabenxiang.mimi.view.personal.PersonalViewModel
import com.dabenxiang.mimi.view.topup.TopupViewModel
import com.dabenxiang.mimi.view.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel() }
    viewModel { SplashViewModel() }
    viewModel { MessengerViewModel() }
    viewModel { LoginViewModel() }
    viewModel { HomeViewModel() }
    viewModel { TopupViewModel() }
    viewModel { FavoriteViewModel() }
    viewModel { PersonalViewModel() }
}
