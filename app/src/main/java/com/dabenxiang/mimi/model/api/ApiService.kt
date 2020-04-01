package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.VideoSearchItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/v1/Home/Categories")
    suspend fun fetchHomeCategories(): Response<CategoriesItem>

    @GET("/v1/Home/Videos/Search")
    suspend fun homeVideosSearch(
        @Query("category") category: String,
        @Query("q") q: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<VideoSearchItem>
}