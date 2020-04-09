package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import retrofit2.Response
import retrofit2.http.*
interface ApiService {

    /**********************************************************
     *
     *                  Attachment 附檔相關的API Ex: 上傳附檔、取附檔
     *
     ***********************************************************/
    @POST("/v1/Attachment/")
    suspend fun postAttachment(
        @Body request: String
    ): Response<String>

    @GET("/v1/Attachment/{id}")
    suspend fun getAttachment(
        @Path("id") id: String
    ): Response<String>

    @PUT("/v1/Attachment/{id}")
    suspend fun putAttachment(
        @Path("id") id: String,
        @Body request: String
    ): Response<Void>

    @DELETE("/v1/Attachment/{id}")
    suspend fun deleteAttachment(
        @Path("id") id: String
    ): Response<Void>

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    @PUT("/v1/Auth/ResetPassword")
    suspend fun resetPassword(@Body requestReset: ResetPasswordRequest): Response<Void>

    @PUT("/v1/Auth/ResetTotp")
    suspend fun resetTotp(@Body request: ResetTotpRequest): Response<Void>

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    // todo: not ready...
    @POST("/v1/Chats/Chat")
    suspend fun postChat(
        @Body request: ChatRequest
    ): Response<Void>

    // todo: not ready...
    @GET("/v1/Chats/Chat")
    suspend fun getChat(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<String>>>

    // todo: not ready...
    @POST("/v1/Chats/Message")
    suspend fun postMessage(
        @Body request: MsgRequest
    ): Response<ApiBaseItem<List<String>>>

    // todo: not ready...
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
    // todo: not ready...
    @GET("/v1/Functions")
    suspend fun getFunctions(): Response<ApiBasePagingItem<List<FunctionItem>>>

    /**********************************************************
     *
     *                  Members/Home/Categories
     *
     ***********************************************************/
    @GET("/v1/Members/Home/Categories")
    suspend fun fetchHomeCategories(): Response<ApiBaseItem<CategoriesItem>>

    /**********************************************************
     *
     *                  Home/Videos
     *
     ***********************************************************/
    @GET("/v1/Home/Videos/Search")
    suspend fun searchHomeVideos(
        @Query("category") category: String,
        @Query("q") q: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<VideoSearchItem>>>

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
    @GET("/v1/Members/Me")
    suspend fun getMe(): Response<ApiBaseItem<MeItem>>

    @GET("/v1/Members/Me/Chat")
    suspend fun getMeChat(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<MeChatItem>>>

    @PUT("/v1/Members/ForgetPassword")
    suspend fun forgetPassword(@Body body: ForgetPasswordRequest): Response<Void>

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
     *                  Merchants 代理
     *
     ***********************************************************/

    /**********************************************************
     *
     *                  Operators Operators
     *
     ***********************************************************/

    /**********************************************************
     *
     *                  Operators/Video
     *
     ***********************************************************/
//    @GET("/v1/Operators/Video")
//    suspend fun getOperatorsVideo(
//        @Query("id") id: Int,
//        @Query("key") key: String,
//        @Query("status") status: Int,
//        @Query("offset") offset: Int,
//        @Query("limit") limit: Int
//    ): Response<Void>
//
//    @PUT("/v1/Operators/Video")
//    suspend fun putOperatorsVideo(request: OperatorsVideoRequest): Response<Void>
//
//    @POST("/v1/Operators/Video/Crawler")
//    suspend fun postCrawler(
//        @Body videoEpisodeIds: List<Int>
//    ) : Response<Void>

    /**********************************************************
     *
     *                  Ordering 建訂單用
     *
     ***********************************************************/

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