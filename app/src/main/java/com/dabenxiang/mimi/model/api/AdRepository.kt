package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.AdInfo
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import retrofit2.Response

class AdRepository(private val adService: AdService) {

    suspend fun getAD(width: Int, height: Int): Response<ApiBaseItem<AdInfo>> {
        return adService.getAD(width, height)
    }
}