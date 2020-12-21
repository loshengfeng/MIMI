package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import com.dabenxiang.mimi.model.api.vo.BlockAdItem
import com.dabenxiang.mimi.model.api.vo.StatisticsRequest
import retrofit2.Response
import retrofit2.http.*

interface AdService {

    @Headers("Authorization: bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzNDY0ODUxMTU5ODAwMDAwIiwidHQiOiIxIiwicnQiOiIwIiwidW5pcXVlX25hbWUiOiIzNzc1MjQ2MTU1OTM2NDMyMTI4Iiwic2NvcGUiOiIxMDAwMDAxIiwibmJmIjoxNTg3OTg2OTY3LCJleHAiOjE5MDMzNDY5NjcsImlzcyI6IlNpbGtyb2RlIiwiYXVkIjoiU2lsa3JvZGUifQ.eRnlQD8Ri8agJMuEanW1kq2ODCVRX1Xig5O9F4bg4qE")
    @GET("/v1/Business/Ads")
    suspend fun getAD(
        @Query("w") width: Int,
        @Query("h") height: Int
    ): Response<ApiBaseItem<AdItem>>

    @Headers("Authorization: bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzNDY0ODUxMTU5ODAwMDAwIiwidHQiOiIxIiwicnQiOiIwIiwidW5pcXVlX25hbWUiOiIzNzc1MjQ2MTU1OTM2NDMyMTI4Iiwic2NvcGUiOiIxMDAwMDAxIiwibmJmIjoxNTg3OTg2OTY3LCJleHAiOjE5MDMzNDY5NjcsImlzcyI6IlNpbGtyb2RlIiwiYXVkIjoiU2lsa3JvZGUifQ.eRnlQD8Ri8agJMuEanW1kq2ODCVRX1Xig5O9F4bg4qE")
    @GET("/v1/business/ads/block/{code}-{w}x{h}-{count}")
    suspend fun getAD(
        @Path("code") code: String,
        @Path("w") width: Int,
        @Path("h") height: Int,
        @Path("count") count: Int
    ): Response<ApiBaseItem<ArrayList<BlockAdItem>>>

    /**********************************************************
     *
     *                  Business 統計接口
     *
     ***********************************************************/
    @Headers("Authorization: bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzNDY0ODUxMTU5ODAwMDAwIiwidHQiOiIxIiwicnQiOiIwIiwidW5pcXVlX25hbWUiOiIzNzc1MjQ2MTU1OTM2NDMyMTI4Iiwic2NvcGUiOiIxMDAwMDAxIiwibmJmIjoxNTg3OTg2OTY3LCJleHAiOjE5MDMzNDY5NjcsImlzcyI6IlNpbGtyb2RlIiwiYXVkIjoiU2lsa3JvZGUifQ.eRnlQD8Ri8agJMuEanW1kq2ODCVRX1Xig5O9F4bg4qE")
    @POST("/v1/Business/Statistics")
    suspend fun statistics(@Body request: StatisticsRequest): Response<ApiBaseItem<Void>>

}