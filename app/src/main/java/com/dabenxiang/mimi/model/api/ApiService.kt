package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import retrofit2.Response
import retrofit2.http.*
interface ApiService {

    /**********************************************************
     *
     *                  Attachment
     *
     ***********************************************************/
    // 上傳檔案
    @POST("/v1/Attachment/")
    suspend fun postAttachment(
        @Body request: String
    ): Response<String>

    // 取得檔案
    @GET("/v1/Attachment/{id}")
    suspend fun getAttachment(
        @Path("id") id: String
    ): Response<String>

    // 修改檔案
    @PUT("/v1/Attachment/{id}")
    suspend fun putAttachment(
        @Path("id") id: String,
        @Body request: String
    ): Response<Void>

    // 刪除檔案
    @DELETE("/v1/Attachment/{id}")
    suspend fun deleteAttachment(
        @Path("id") id: String
    ): Response<Void>

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    // 修改密碼
    @PUT("/v1/Auth/ResetPassword")
    suspend fun resetPassword(@Body requestReset: ResetPasswordRequest): Response<Void>

    // 修改OTP
    @PUT("/v1/Auth/ResetTotp")
    suspend fun resetTotp(@Body request: ResetTotpRequest): Response<Void>

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    // 建立聊天室
    @POST("/v1/Chats/Chat")
    suspend fun postChat(
        @Body request: ChatRequest
    ): Response<Void>

    // 取得聊天室列表
    @GET("/v1/Chats/Chat")
    suspend fun getChat(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<String>>>

    // 發送訊息
    @POST("/v1/Chats/Message")
    suspend fun postMessage(
        @Body request: MsgRequest
    ): Response<ApiBaseItem<List<String>>>

    // 取得訊息
    @GET("/v1/Chats/Message")
    suspend fun getMessage(
        @Query("chatId") chatId: Int,
        @Query("lastReadTime") lastReadTime: String,
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<String>>>

    /**********************************************************
     *
     *                  Functions
     *
     ***********************************************************/
    // 取得角色功能列表
    @GET("/v1/Functions")
    suspend fun getFunctions(): Response<ApiBasePagingItem<List<FunctionItem>>>

    /**********************************************************
     *
     *                  Members/Home/Categories
     *
     ***********************************************************/
    // 取得影片類別清單
    @GET("/v1/Members/Home/Categories")
    suspend fun fetchHomeCategories(): Response<ApiBaseItem<CategoriesItem>>

    /**********************************************************
     *
     *                  Home/Videos
     *
     ***********************************************************/
    // 取得類別影片
    @GET("/v1/Home/Videos/Search")
    suspend fun searchHomeVideos(
        @Query("category") category: String,
        @Query("q") q: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<VideoSearchItem>>>

    // 取得熱門影片
    @GET("/v1/Home/Videos/Statistics")
    suspend fun statisticsHomeVideos(
        @Query("statisticsType") statisticsType: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<VideoSearchItem>>>

    /**********************************************************
     *
     *                  Members/Me
     *
     ***********************************************************/
    // 取得用者資訊
    @GET("/v1/Members/Me")
    suspend fun getMe(): Response<ApiBaseItem<MeItem>>

    // 取得聊天室列表
    @GET("/v1/Members/Me/Chat")
    suspend fun getMeChat(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<MeChatItem>>>

    // 取得聊天室內容
    @GET("/v1/Members/Me/Message/{chatId}")
    suspend fun getMeMessage(
        @Path("chatId") chatId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBaseItem<List<MeMessageItem>>>

    // 忘記密碼
    @PUT("/v1/Members/ForgetPassword")
    suspend fun forgetPassword(@Body body: ForgetPasswordRequest): Response<Void>

    // 取得使用者充值記錄
    @GET("/v1/Members/Me/Order")
    suspend fun getMeOrder(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBaseItem<List<MeOrderItem>>>

    // rutodo: 13/04/2020
    @POST("/v1/Members/Me/Playlist")
    suspend fun postMePlaylist(
        @Body ids: List<Int>
    ): Response<Void>

    // 刪除使用者列表影片
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
    suspend fun updatedMeProfile(@Body body: MeProfileItem): Response<Void>

    @POST("/v1/Members/SignUp")
    suspend fun signUp(@Body body: MembersAccountItem): Response<Void>

    @GET("/v1/Members/Me/ValidationEmail/{key}")
    suspend fun validationEmail(
        @Path("key") key: String
    ): Response<Void>

    /**********************************************************
     *
     *                  Player
     *
     ***********************************************************/
    @GET("/v1/Player/{videoId}")
    suspend fun getVideoInfo(@Path("videoId") videoId: Int): Response<ApiBaseItem<VideoItem>>

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