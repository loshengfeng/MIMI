package com.dabenxiang.mimi.manager.update.api

import com.dabenxiang.mimi.manager.update.BaseDomainManager.Companion.TOKEN_PREFIX
import retrofit2.Response
import timber.log.Timber
import tw.gov.president.manager.UPDATE_API_CLIENT_ID
import tw.gov.president.manager.UPDATE_API_CLIENT_SECRET
import com.dabenxiang.mimi.manager.update.data.*
import tw.gov.president.utils.general.extension.toBase64String
import java.util.*

class UpdateApiRepository(private val apiService: UpdateApiService) {

    suspend fun authToken(): Response<OAutn2TokenItem> {
        val auth =
            "$TOKEN_PREFIX${"$UPDATE_API_CLIENT_ID:$UPDATE_API_CLIENT_SECRET".toBase64String()}"
        Timber.i("updateToken auth=$auth ")
        return apiService.authToken(auth = auth, request = OAuth2TokenRequest())
    }

    // 檢查更新
    suspend fun getPackagesInfo(
        uniqueId: String,
        deviceId: String
    ): Response<UpdateBaseItem<ArrayList<PackagesItem>>> {
        return apiService.getPackagesInfo(uniqueId, deviceId)
    }

    // 綁定邀請碼
    suspend fun bindingInvitationCodes(
        uniqueId: String,
        deviceId: String
    ): Response<UpdateBaseItem<ResultItem>> {
        return apiService.bindingInvitationCodes(uniqueId, deviceId)
    }
}

