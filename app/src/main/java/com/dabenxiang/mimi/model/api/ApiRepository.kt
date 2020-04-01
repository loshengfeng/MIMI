package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.VideoSearchItem
import retrofit2.Response

class ApiRepository(private val apiService: ApiService) {

    companion object {
        const val MEDIA_TYPE_JSON = "application/json"
    }

    suspend fun fetchHomeCategories() = apiService.fetchHomeCategories()

    suspend fun homeVideosSearch(category: String, q: String, offset: Int, limit: Int): Response<VideoSearchItem> {
        return apiService.homeVideosSearch(category, q, offset, limit)
    }
}

