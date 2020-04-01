package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("/v1/Home/Categories")
    suspend fun fetchHomeCategories(): Response<CategoriesItem>

}