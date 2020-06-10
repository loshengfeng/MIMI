package com.dabenxiang.mimi.di

import com.dabenxiang.mimi.view.changepassword.ChangePasswordViewModel
import com.dabenxiang.mimi.view.chathistory.ChatHistoryViewModel
import com.dabenxiang.mimi.view.favorite.FavoriteViewModel
import com.dabenxiang.mimi.view.forgetpassword.ForgetPasswordViewModel
import com.dabenxiang.mimi.view.home.CategoriesViewModel
import com.dabenxiang.mimi.view.home.HomeViewModel
import com.dabenxiang.mimi.view.login.LoginViewModel
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.messenger.MessengerViewModel
import com.dabenxiang.mimi.view.personal.PersonalViewModel
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.dabenxiang.mimi.view.player.ShortPlayerViewModel
import com.dabenxiang.mimi.view.updateprofile.UpdateProfileViewModel
import com.dabenxiang.mimi.view.search.SearchVideoViewModel
import com.dabenxiang.mimi.view.setting.SettingViewModel
import com.dabenxiang.mimi.view.splash.SplashViewModel
import com.dabenxiang.mimi.view.topup.TopupViewModel
import com.dabenxiang.mimi.view.topuphistory.TopupHistoryViewModel
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
    viewModel { ForgetPasswordViewModel() }
    viewModel { SettingViewModel() }
    viewModel { UpdateProfileViewModel() }
    viewModel { ChangePasswordViewModel() }
    viewModel { CategoriesViewModel() }
    viewModel { TopupHistoryViewModel() }
    viewModel { ChatHistoryViewModel() }
    viewModel { PlayerViewModel() }
    viewModel { SearchVideoViewModel() }
    viewModel { ShortPlayerViewModel() }
}
