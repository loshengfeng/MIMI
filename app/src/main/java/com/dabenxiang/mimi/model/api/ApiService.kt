package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @Headers("Authorization: basic ZmFkYWNhaV9mcm9udGVuZDo0NGVkMWJiZDI3NDUwOTZkYTI1MmM5NWM0YTQ0NGVkMWJiZA==")
    @POST("/v1/oauth2/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<TokenItem>

    @FormUrlEncoded
    @Headers("Authorization: basic ZmFkYWNhaV9mcm9udGVuZDo0NGVkMWJiZDI3NDUwOTZkYTI1MmM5NWM0YTQ0NGVkMWJiZA==")
    @POST("/v1/oauth2/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<TokenItem>

    @POST("/v1/auth/signin")
    suspend fun signIn(@Body loginRequest: LoginRequest): Response<ApiBaseItem<LoginItem>>

    @GET("/v1/auth/signout")
    suspend fun signOut(): Response<Void>

    /**********************************************************
     *
     *                  Attachment x 4
     *
     ***********************************************************/
    @POST("/v1/Attachment")
    suspend fun postAttachment(
        @Body file: String
    ): Response<String>

    @GET("/v1/Attachment/{id}")
    suspend fun getAttachment(
        @Path("id") id: String
    ): Response<String>

    @PUT("/v1/Attachment/{id}")
    suspend fun putAttachment(
        @Path("id") id: String,
        @Body file: String
    ): Response<Void>

    @DELETE("/v1/Attachment/{id}")
    suspend fun deleteAttachment(
        @Path("id") id: String
    ): Response<Void>

    /**********************************************************
     *
     *                  Auth x 1
     *
     ***********************************************************/
    @PUT("/v1/Auth/ResetPassword")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<Void>

    /**********************************************************
     *
     *                  Chats x 4
     *
     ***********************************************************/
    @POST("/v1/Chats")
    suspend fun postChat(
        @Body request: ChatRequest
    ): Response<Void>

    @GET("/v1/Chats")
    suspend fun getChat(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<String>>>

    @POST("/v1/Chats/Message")
    suspend fun postMessage(
        @Body request: MsgRequest
    ): Response<ApiBaseItem<List<String>>>

    @GET("/v1/Chats/Message")
    suspend fun getMessage(
        @Query("chatId") chatId: Int,
        @Query("lastReadTime") lastReadTime: String,
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<String>>>

    /**********************************************************
     *
     *                  Functions x 1
     *
     ***********************************************************/
    @GET("/v1/Functions")
    suspend fun getFunctions(): Response<ApiBasePagingItem<List<FunctionItem>>>

    /**********************************************************
     *
     *                  Members x 5
     *
     ***********************************************************/
    @PUT("/v1/Members/ChangePassword")
    suspend fun changePassword(
        @Body password: String
    ): Response<Void>

    @PUT("/v1/Members/ForgetPassword")
    suspend fun forgetPassword(@Body body: ForgetPasswordRequest): Response<Void>

    @POST("/v1/Members/SignUp")
    suspend fun signUp(
        @Body body: MembersAccountItem
    ): Response<Void>

    @POST("/v1/Members/Me/ValidationEmail")
    suspend fun validationEmail(
        @Body request: ValidateEmailRequest
    ): Response<Void>

    @GET("/v1/Members/Me/ValidationEmail/{key}")
    suspend fun validationEmail(
        @Path("key") key: String
    ): Response<Void>

    /**********************************************************
     *
     *                  Members/Home/Categories x 1
     *
     ***********************************************************/
    @GET("/v1/Members/Home/Categories")
    suspend fun fetchHomeCategories(): Response<ApiBaseItem<RootCategoriesItem>>

    /**********************************************************
     *
     *                  Members/Home/Videos x 2
     *
     ***********************************************************/
    @GET("/v1/Members/Home/Videos/Search")
    suspend fun searchHomeVideos(
        @Query("category") category: String?,
        @Query("q") q: String?,
        @Query("country") country: String?,
        @Query("years") years: Int?,
        @Query("isAdult") isAdult: Boolean?,
        @Query("offset") offset: String?,
        @Query("limit") limit: String?
    ): Response<ApiBasePagingItem<VideoSearchItem>>

    @GET("/v1/Members/Home/Videos/Statistics")
    suspend fun statisticsHomeVideos(
        @Query("statisticsType") statisticsType: Int,
        @Query("tag") tag: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<StatisticsItem>>>

    /**********************************************************
     *
     *                  Members/Me x 9
     *
     ***********************************************************/
    @GET("/v1/Members/Me")
    suspend fun getMe(): Response<ApiBaseItem<MeItem>>

    @GET("/v1/Members/Me/Chat")
    suspend fun getMeChat(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<MeChatItem>>>

    @GET("/v1/Members/Me/Message/{chatId}")
    suspend fun getMeMessage(
        @Path("chatId") chatId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBaseItem<List<MeMessageItem>>>

    @GET("/v1/Members/Me/Order")
    suspend fun getMeOrder(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBaseItem<List<MeOrderItem>>>

    @POST("/v1/Members/Me/Playlist")
    suspend fun addMePlaylist(
        @Body request: PlayListRequest
    ): Response<Void>

    @DELETE("/v1/Members/Me/Playlist")
    suspend fun deleteMePlaylist(
        @Body ids: List<Int>
    ): Response<Void>

    @GET("/v1/Members/Me/Playlist/{playlistType}")
    suspend fun getMePlaylist(
        @Path("playlistType") playlistType: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<PlayListItem>>

    @GET("/v1/Members/Me/Profile")
    suspend fun getMeProfile(): Response<ApiBaseItem<MeProfileItem>>

    @PUT("/v1/Members/Me/Profile")
    suspend fun updatedMeProfile(
        @Body body: MeProfileItem
    ): Response<Void>

    /**********************************************************
     *
     *                  Ordering x 1
     *
     ***********************************************************/
    @GET("/v1/Ordering/Agent")
    suspend fun getAgent(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<AgentItem>>

    /**********************************************************
     *
     *                  Player x 3
     *
     ***********************************************************/
    @GET("/v1/Player/{videoId}")
    suspend fun getVideoInfo(
        @Path("videoId") videoId: Int
    ): Response<ApiBaseItem<VideoItem>>

    @GET("/v1/Player/{videoId}/{episodeId}")
    suspend fun getVideoEpisode(
        @Path("videoId") videoId: Int,
        @Path("episodeId") episodeId: Int
    ): Response<ApiBaseItem<VideoEpisodeItem>>

    @GET("/v1/Player/{videoId}/{episodeId}/{streamId}")
    suspend fun getVideoStreamOfEpisode(
        @Path("videoId") videoId: Int,
        @Path("episodeId") episodeId: Int,
        @Path("streamId") streamId: Int
    ): Response<Void>
}