package com.dabenxiang.mimi.view.dialog.dailycheckin

import com.dabenxiang.mimi.view.base.BaseViewModel

class DailyCheckInDialogViewModel : BaseViewModel() {
    fun getVideoCount():Int{
        return accountManager.getProfile().videoCount
    }
    fun getVideoOnDemandCount():Int{
        return accountManager.getProfile().videoOnDemandCount
    }
}