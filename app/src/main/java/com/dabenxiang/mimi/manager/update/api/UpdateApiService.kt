package com.dabenxiang.mimi.manager.update.api

import retrofit2.Response
import retrofit2.http.*
import com.dabenxiang.mimi.manager.update.data.*
import java.util.*

interface UpdateApiService {

    @POST("v1/OAuth2/Token")
    suspend fun authToken(
        @Header("Authorization") auth: String,
        @Body request: OAuth2TokenRequest
    ): Response<OAutn2TokenItem>

    @GET("/v1/Clients/Packages/{uniqueId}/{deviceId}")
    suspend fun getPackagesInfo(
        @Path("uniqueId") uniqueId: String,
        @Path("deviceId") deviceId: String
    ): Response<UpdateBaseItem<ArrayList<PackagesItem>>>

    @POST("/v1/Clients/InvitationCodes/{invitationCodes}/Binding/{deviceId}")
    suspend fun bindingInvitationCodes(
        @Path("invitationCodes") uniqueId: String,
        @Path("deviceId") deviceId: String
    ): Response<UpdateBaseItem<ResultItem>>

}