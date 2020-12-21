package com.dabenxiang.mimi.view.dialog.announce

import com.dabenxiang.mimi.view.base.BaseViewModel

class AnnounceDialogViewModel : BaseViewModel() {

    fun getUrl(): String {
        return "${domainManager.getStorageDomain()}/Announcement.html"
    }
}