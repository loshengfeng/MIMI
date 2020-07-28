package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.AdInfo
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface AdService {

    @Headers("Authorization: bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzNDY0ODUxMTU5ODAwMDAwIiwidHQiOiIxIiwicnQiOiIwIiwidW5pcXVlX25hbWUiOiIzNzc1MjQ2MTU1OTM2NDMyMTI4Iiwic2NvcGUiOiIxMDAwMDAxIiwibmJmIjoxNTg3OTg2OTY3LCJleHAiOjE5MDMzNDY5NjcsImlzcyI6IlNpbGtyb2RlIiwiYXVkIjoiU2lsa3JvZGUifQ.eRnlQD8Ri8agJMuEanW1kq2ODCVRX1Xig5O9F4bg4qE")
    @GET("/v1/Business/Ads")
    suspend fun getAD(
        @Query("w") width: Int,
        @Query("h") height: Int
    ): Response<ApiBaseItem<AdInfo>>

}