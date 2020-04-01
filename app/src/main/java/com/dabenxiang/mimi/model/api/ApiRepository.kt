package com.dabenxiang.mimi.model.api

class ApiRepository(private val apiService: ApiService) {

    companion object {
        const val MEDIA_TYPE_JSON = "application/json"
    }

    suspend fun fetchHomeCategories() = apiService.fetchHomeCategories()
}

