package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
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
    suspend fun signIn(@Body request: SignInRequest): Response<ApiBaseItem<SignInItem>>

    @GET("/v1/auth/signout")
    suspend fun signOut(): Response<Void>

    /**********************************************************
     *
     *                  Attachment
     *
     ***********************************************************/
    @Streaming
    @GET("/v1/Attachments/{id}")
    suspend fun getAttachment(
        @Path("id") id: Long
    ): Response<ResponseBody>

    @Multipart
    @PUT("/v1/Attachments/{id}")
    suspend fun putAttachment(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Response<Void>

//    @Multipart
//    @POST("/v1/Attachments")
//    suspend fun postAttachment(
//        @Part file: MultipartBody.Part
//    ): Response<ApiBaseItem<Long>>

    @Multipart
    @POST("/v1/Attachments")
    suspend fun postAttachment(
        @Part file: MultipartBody.Part
    ): Response<ApiBaseItem<Long>>

    @DELETE("/v1/Attachments/{id}")
    suspend fun deleteAttachment(
        @Path("id") id: String
    ): Response<Void>

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    @PUT("/v1/Auth/ResetPassword")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<Void>

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    @POST("/v1/Members/Me/Chats")
    suspend fun postChat(
        @Body request: ChatRequest
    ): Response<Void>

    @GET("/v1/Members/Me/Chats")
    suspend fun getChat(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<ChatListItem>>>

    @POST("/v1/Members/Me/Chats/Message")
    suspend fun postMessage(
        @Body request: MsgRequest
    ): Response<ApiBaseItem<List<String>>>

    @GET("/v1/Members/Me/Chats/{chatId}")
    suspend fun getMessage(
        @Path("chatId") chatId: Long,
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<ChatContent>>

    @PUT("/v1/Members/Me/Chats/LastRead")
    suspend fun setLastReadMessageTime(
        @Body body: HashMap<String, Long>
    ): Response<Void>

    @GET("/v1/Members/Me/Chats/UnRead")
    suspend fun getUnread(): Response<ApiBaseItem<Int>>

    /**********************************************************
     *
     *                  Functions
     *
     ***********************************************************/
    @GET("/v1/Functions")
    suspend fun getFunctions(): Response<ApiBasePagingItem<List<FunctionItem>>>

    /**********************************************************
     *
     *                  Members
     *
     ***********************************************************/
    @PUT("/v1/Members/ChangePassword")
    suspend fun changePassword(
        @Body body: ChangePasswordRequest
    ): Response<Void>

    @PUT("/v1/Members/ForgetPassword")
    suspend fun forgetPassword(
        @Body body: ForgetPasswordRequest
    ): Response<Void>

    @POST("/v1/Members/SignUp")
    suspend fun signUp(
        @Body body: SingUpRequest
    ): Response<Void>

    @POST("/v1/Members/Me/Email")
    suspend fun resendEmail(
        @Body body: EmailRequest
    ): Response<Void>

    @POST("/v1/Members/{userId}/Follow")
    suspend fun followPost(
        @Path("userId") userId: Long
    ): Response<Void>

    @DELETE("/v1/Members/{userId}/Follow")
    suspend fun cancelFollowPost(
        @Path("userId") userId: Long
    ): Response<Void>

    /**********************************************************
     *
     *                  Members/Post
     *
     ***********************************************************/
    @GET("/v1/Members/Post")
    suspend fun getMembersPost(
        @Query("type") type: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("isAdult") isAdult: Boolean = true,
        @Query("orderBy") orderBy: Int = 1
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    @GET("/v1/Members/Post")
    suspend fun getMembersPost(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("creatorId") creatorId: Long,
        @Query("isAdult") isAdult: Boolean = true,
        @Query("orderBy") orderBy: Int = 1
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    @GET("/v1/Members/Post/{id}")
    suspend fun getMemberPostDetail(@Path("id") postId: Long): Response<ApiBaseItem<MemberPostItem>>

    @POST("/v1/Members/Post")
    suspend fun postMembersPost(
        @Body request: PostMemberRequest
    ): Response<ApiBaseItem<Long>>

    @GET("/v1/Members/Post/{postId}/Comment")
    suspend fun getMembersPostComment(
        @Path("postId") postId: Long,
        @Query("parentId") parentId: Long?,
        @Query("sorting") sorting: Int,
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<MembersPostCommentItem>>>

    @POST("/v1/Members/Post/{postId}/Comment")
    suspend fun postMembersPostComment(
        @Path("postId") postId: Long,
        @Body request: PostCommentRequest
    ): Response<Void>

    @POST("/v1/Members/Post/{postId}/Comment/{commentId}/Like")
    suspend fun postMembersPostCommentLike(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Body request: PostLikeRequest
    ): Response<Void>

    @DELETE("/v1/Members/Post/{postId}/Comment/{commentId}/Like")
    suspend fun deleteMembersPostCommentLike(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long
    ): Response<Void>

    @PUT("/v1/Members/Post/{id}")
    suspend fun updatePost(
        @Path("id") postId: Long,
        @Body request: PostMemberRequest
    ): Response<ApiBaseItem<Long>>

    /**********************************************************
     *
     *                  Members/Club
     *
     ***********************************************************/
    @GET("/v1/Members/Club")
    suspend fun getMembersClub(
        @Query("tag") tag: String
    ): Response<ApiBasePagingItem<ArrayList<MemberClubItem>>>

    @GET("/v1/Members/Club/{id}")
    suspend fun getMembersClub(
        @Path("id") clubId: Long
    ): Response<ApiBasePagingItem<MemberClubItem>>

    @GET("/v1/Members/Club/Post")
    suspend fun getMembersClubPost(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberClubItem>>>

    @GET("/v1/Members/Club/Post")
    suspend fun getMembersClubPost(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("keyword") keyword: String
    ): Response<ApiBasePagingItem<ArrayList<MemberClubItem>>>

    @GET("/v1/Members/Post")
    suspend fun getMembersPost(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("tag") tag: String = "",
        @Query("orderBy") orderBy: Int = 1,
        @Query("isAdult") isAdult: Boolean = true,
        @Query("isFullContent") isFullContent: Boolean = false
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    @POST("/v1/Members/Club/{clubId}/Follow")
    suspend fun followClub(@Path("clubId") clubId: Long): Response<Void>

    @DELETE("/v1/Members/Club/{clubId}/Follow")
    suspend fun cancelFollowClub(@Path("clubId") clubId: Long): Response<Void>

    /**********************************************************
     *
     *                  Members/Home/Categories
     *
     ***********************************************************/
    @GET("/v1/Members/Home/Categories")
    suspend fun fetchHomeCategories(): Response<ApiBaseItem<RootCategoriesItem>>

    /**********************************************************
     *
     *                  Members/Home/Videos
     *
     ***********************************************************/
    @GET("/v1/Members/Home/Videos/Search")
    suspend fun searchHomeVideos(
        @Query("category") category: String?,
        @Query("q") q: String?,
        @Query("country") country: String?,
        @Query("years") years: String?,
        @Query("isAdult") isAdult: Boolean?,
        @Query("offset") offset: String?,
        @Query("limit") limit: String?,
        @Query("tag") tag: String?
    ): Response<ApiBasePagingItem<VideoSearchItem>>

    @GET("/v1/Members/Home/Videos/SearchWithCategory")
    suspend fun searchWithCategory(
        @Query("category") category: String?,
        @Query("isAdult") isAdult: Boolean?,
        @Query("offset") offset: String?,
        @Query("limit") limit: String?
    ): Response<ApiBasePagingItem<List<SimpleVideoItem>>>

    @GET("/v1/Members/Home/Videos/Statistics")
    suspend fun statisticsHomeVideos(
        @Query("statisticsType") statisticsType: Int?,
        @Query("category") category: String?,
        @Query("isAdult") isAdult: Boolean,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<StatisticsItem>>>

    @GET("/v1/Members/Home/Videos/PostStatistics")
    suspend fun getRankingList(
        @Query("statisticsType") statisticsType: Int,
        @Query("postType") postType: Int,
        @Query("offset") offset: String?,
        @Query("limit") limit: String?
    ): Response<ApiBasePagingItem<List<PostStatisticsItem>>>


    /**********************************************************
     *
     *                  Members/Me
     *
     ***********************************************************/
    @GET("/v1/Members/Me")
    suspend fun getMe(): Response<ApiBaseItem<MeItem>>

    @PUT("/v1/Members/Me/Avatar")
    suspend fun putAvatar(
        @Body request: AvatarRequest
    ): Response<Void>

    @GET("/v1/Members/Me/ClubFollow")
    suspend fun getMyClubFollow(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<ClubFollowItem>>>

    @DELETE("/v1/Members/Me/ClubFollow/{clubId}")
    suspend fun cancelMyClubFollow(
        @Path("clubId") id: Long
    ): Response<Void>

    @GET("/v1/Members/Me/MemberFollow")
    suspend fun getMyMemberFollow(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<MemberFollowItem>>>

    @DELETE("/v1/Members/Me/MemberFollow/{userId}")
    suspend fun cancelMyMemberFollow(
        @Path("userId") id: Long
    ): Response<Void>

    @GET("/v1/Members/Me/Order")
    suspend fun getOrder(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<OrderItem>>>

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

    @DELETE("/v1/Members/Me/Playlist/{videoId}")
    suspend fun deletePlaylist(
        @Path("videoId") videoId: String
    ): Response<Void>

    @GET("/v1/Members/Me/Playlist/{playlistType}")
    suspend fun getPlaylist(
        @Path("playlistType") playlistType: Int,
        @Query("isAdult") isAdult: Boolean,
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<PlayItem>>>

    @GET("/v1/Members/Me/PostFavorite")
    suspend fun getPostFavorite(
        @Query("offset") offset: String,
        @Query("limit") limit: String
    ): Response<ApiBasePagingItem<List<PostFavoriteItem>>>

    @DELETE("/v1/Members/Me/PostFavorite/{postFavoriteId}")
    suspend fun deletePostFavorite(
        @Path("postFavoriteId") postFavoriteId: String
    ): Response<Void>

    @GET("/v1/Members/Me/PostFollow")
    suspend fun getPostFollow(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    @GET("/v1/Members/Me/Profile")
    suspend fun getProfile(): Response<ApiBaseItem<ProfileItem>>

    @PUT("/v1/Members/Me/Profile")
    suspend fun updateProfile(
        @Body body: ProfileRequest
    ): Response<Void>

    /**********************************************************
     *
     *                  Members/Me/Post
     *
     ***********************************************************/
    @GET("/v1/Members/Me/Post")
    suspend fun getMyPost(
        @Query("isAdult") isAdult: Boolean = true,
        @Query("status") status: Int = 1,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<MemberPostItem>>>

    @DELETE("/v1/Members/Me/Post/{postId}")
    suspend fun deleteMyPost(
        @Path("postId") postId: Long
    ): Response<Void>

    /**********************************************************
     *
     *                  Members/Post
     *
     ***********************************************************/
    @POST("/v1/Members/Post/{postId}/Favorite")
    suspend fun addFavorite(
        @Path("postId") postId: Long
    ): Response<Void>

    @DELETE("/v1/Members/Post/{postId}/Favorite")
    suspend fun deleteFavorite(
        @Path("postId") postId: Long
    ): Response<Void>

    @POST("/v1/Members/Post/{postId}/Like")
    suspend fun like(
        @Path("postId") postId: Long,
        @Body body: LikeRequest
    ): Response<Void>

    @POST("/v1/Members/Post/{postId}/PostReport")
    suspend fun sendPostReport(
        @Path("postId") postId: Long,
        @Body body: ReportRequest
    ): Response<Void>

    @POST("/v1/Members/Post/{postId}/Comment/{commentId}/CommentReport")
    suspend fun sendPostCommentReport(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Body body: ReportRequest
    ): Response<Void>

    @GET("/v1/Members/Post")
    suspend fun searchPostByTag(
        @Query("type") type: Int,
        @Query("tag") tag: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("orderBy") orderBy: Int = 1,
        @Query("isFullContent") isFullContent: Boolean = false,
        @Query("isAdult") isAdult: Boolean = true
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    @GET("/v1/Members/Post")
    suspend fun searchPostFollowByTag(
        @Query("tag") tag: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("orderBy") orderBy: Int = 1,
        @Query("isFullContent") isFullContent: Boolean = false,
        @Query("isAdult") isAdult: Boolean = true
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>


    @GET("/v1/Members/Post")
    suspend fun searchPostByKeyword(
        @Query("type") type: Int,
        @Query("key") key: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("orderBy") orderBy: Int = 1,
        @Query("isFullContent") isFullContent: Boolean = false,
        @Query("isAdult") isAdult: Boolean = true
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    @GET("/v1/Members/Post")
    suspend fun searchPostFollowByKeyword(
        @Query("key") key: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("orderBy") orderBy: Int = 1,
        @Query("isFullContent") isFullContent: Boolean = false,
        @Query("isAdult") isAdult: Boolean = true
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>>

    /**********************************************************
     *
     *                  Ordering
     *
     ***********************************************************/
    @GET("/v1/Ordering/Agent")
    suspend fun getAgent(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ApiBasePagingItem<List<AgentItem>>>

    /**********************************************************
     *
     *                  Player
     *
     ***********************************************************/
    @GET("/v1/Player/{videoId}")
    suspend fun getVideoInfo(
        @Path("videoId") videoId: Long
    ): Response<ApiBaseItem<VideoItem>>

    @GET("/v1/Player/{videoId}/{episodeId}")
    suspend fun getVideoEpisode(
        @Path("videoId") videoId: Long,
        @Path("episodeId") episodeId: Long
    ): Response<ApiBaseItem<VideoEpisodeItem>>

    @GET("/v1/Player/{videoId}/{episodeId}/{streamId}")
    suspend fun getVideoStreamOfEpisode(
        @Path("videoId") videoId: Long,
        @Path("episodeId") episodeId: Long,
        @Path("streamId") streamId: Long,
        @Query("userId") userId: Long?,
        @Query("utcTime") utcTime: Long?,
        @Query("sign") sign: String?
    ): Response<Void>

    @GET("/v1/Player/VideoStreamM3u8/{streamId}/{userId}/{utcTime}/{sign}/index.m3u8")
    suspend fun getVideoStreamM3u8(
        @Path("streamId") streamId: Long,
        @Path("userId") userId: Long?,
        @Path("utcTime") utcTime: Long?,
        @Path("sign") sign: String?
    ): Response<Void>


    /**********************************************************
     *
     *                  Ordering
     *
     ***********************************************************/
    @GET("/v1/Ordering/Package")
    suspend fun getOrderingPackage(): Response<ApiBaseItem<ArrayList<OrderingPackageItem>>>

    @GET("/v1/Ordering/Package")
    suspend fun getOrderingPackageByPaymentType(
        @Query("paymentType") paymentType: Int
    ): Response<ApiBaseItem<ArrayList<OrderingPackageItem>>>

    /**********************************************************
     *
     *                  Create Order
     *
     ***********************************************************/
    @POST("/v1/Members/Me/Order")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Void>

}