package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import com.dabenxiang.mimi.model.api.vo.BlockAdItem
import com.dabenxiang.mimi.model.api.vo.StatisticsRequest
import retrofit2.Response

class AdRepository(private val adService: AdService) {

    suspend fun getAD(width: Int, height: Int): Response<ApiBaseItem<AdItem>> {
        return adService.getAD(width, height)
    }

    suspend fun getAD(code: String, width: Int, height: Int, count: Int = 1): Response<ApiBaseItem<ArrayList<BlockAdItem>>> {
        return adService.getAD(code, width, height, count)
    }

    /**
     * 統計接口
     */
    suspend fun statistics(request: StatisticsRequest) = adService.statistics(request)
}