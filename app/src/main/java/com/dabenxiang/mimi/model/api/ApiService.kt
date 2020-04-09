package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /**********************************************************
     *
     *                  Attachment
     *
     ***********************************************************/
    @GET("/v1/Attachment/{id}")
    suspend fun getAttachment(
        @Path("id") id: Int
    ): Response<String>

    @POST("/v1/Attachment/{id}")
    suspend fun postAttachment(
        @Path("id") id: Int,
        @Body request: String
    ): Response<String>

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    @PUT("/v1/Auth/ResetPassword")
    suspend fun resetPassword(@Body request: PasswordRequest): Response<Void>

    @PUT("/v1/Auth/ResetTotp")
    suspend fun resetTotp(@Body request: ResetTotpRequest): Response<Void>

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    @POST("/v1/Chats")
    suspend fun getChats(@Query("targetUserId") targetUserId: String): Response<ApiBaseItem<String>>

    /**********************************************************
     *
     *                  Functions
     *
     ***********************************************************/
    @GET("/v1/Functions")
    suspend fun getFunctions(): Response<ApiBaseItem<List<String>>>

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
     *                  Me
     *
     ***********************************************************/
    @GET("/v1/Me")
    suspend fun getMe(): Response<ApiBaseItem<MeItem>>

    @GET("/v1/MeChat")
    suspend fun getMeChat(): Response<ApiBasePagingItem<List<MeChatItem>>>

    @GET("/v1/Me/Message/{chatId}")
    suspend fun getMeMessage(@Path("chatId") chatId: String): Response<ApiBaseItem<List<MeMessageItem>>>

    @GET("/v1/Me/Order")
    suspend fun getMeOrder(): Response<ApiBaseItem<List<MeOrderItem>>>

    @DELETE("/v1/Me/Playlist")
    suspend fun deleteMePlaylist(@Body body: JSONObject): Response<Void>

    @GET("/v1/Me/Playlist/{playlistType}")
    suspend fun getMePlaylist(@Path("playlistType") playlistType: Int): Response<ApiBasePagingItem<PlayListItem>>

    @GET("/v1/Me/Profile")
    suspend fun getMeProfile(): Response<ApiBaseItem<MeProfileItem>>

    @PUT("/v1/Me/Profile")
    suspend fun updatedMeProfile(@Body body: MeProfileItem): Response<Void>

    /**********************************************************
     *
     *                  Members
     *
     ***********************************************************/
    @PUT("/v1/Members/ForgetPassword")
    suspend fun forgetPassword(@Body body: PasswordRequest): Response<Void>

    @POST("/v1/Members/SignUp")
    suspend fun signUp(@Body body: MembersAccountItem): Response<Void>

    /**********************************************************
     *
     *                  Notification/Email
     *
     ***********************************************************/
    @GET("/v1/Notification/Email/Validation")
    suspend fun emailValidation(): Response<Void>

    /**********************************************************
     *
     *                  Operators
     *
     ***********************************************************/

    /**********************************************************
     *
     *                  Ordering
     *
     ***********************************************************/
    @POST("/v1/Ordering")
    suspend fun uppdatedOrdering()

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