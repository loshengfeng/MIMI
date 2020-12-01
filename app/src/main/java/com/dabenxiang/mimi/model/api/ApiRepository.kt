package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.api.vo.error.TOKEN_NOT_FOUND
import com.dabenxiang.mimi.model.enums.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import timber.log.Timber
import java.io.File

class ApiRepository(private val apiService: ApiService) {

    companion object {
        const val MEDIA_TYPE_JSON = "application/json"
        const val AUTHORIZATION = "Authorization"
        const val X_DEVICE_ID = "X-Device-Id"
        const val BEARER = "Bearer "
        const val FILE = "file"
        const val MEDIA_TYPE_IMAGE = "image/*"
        const val X_REQUESTED_FROM = "X-Requested-From"
        const val NETWORK_PAGE_SIZE = 20
        fun isRefreshTokenFailed(code: String?): Boolean {
            return code == TOKEN_NOT_FOUND
        }
    }

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    suspend fun getToken() = apiService.getToken(
        "client_credentials",
        "3770511208570945536",
        "1d760dedf35a4a508ecd71b5013a1611"
    )

    /**
     * 更新Token
     */
    suspend fun refreshToken(token: String) =
        apiService.refreshToken(
            "refresh_token",
            token,
            "3770511208570945536",
            "1d760dedf35a4a508ecd71b5013a1611"
        )

    /**
     * 登入
     */
    suspend fun signIn(request: SignInRequest) = apiService.signIn(request)

    /**
     * 登出
     */
    suspend fun signOut() = apiService.signOut()

    /**********************************************************
     *
     *                  Attachment
     *
     ***********************************************************/
    /**
     * 上傳檔案
     */
//    suspend fun postAttachment(body: String) {
//        val requestFile = MultipartBody.Part.createFormData(
//            FILE, targetName, RequestBody.create(MediaType.parse(MEDIA_TYPE_IMAGE), file)
//        )
//        return apiService.postAttachment(requestFile)
//    }

    /**
     * 上傳檔案
     */
    suspend fun postAttachment(
        file: File,
        fileName: String,
        type: String = MEDIA_TYPE_IMAGE
    ): Response<ApiBaseItem<Long>> {
        val requestFile = MultipartBody.Part.createFormData(
            FILE, fileName, file.asRequestBody(type.toMediaTypeOrNull())
        )
        return apiService.postAttachment(requestFile)
    }

    /**
     * 修改檔案
     */
    suspend fun putAttachment(
        id: Long,
        file: File,
        fileName: String
    ): Response<Void> {
        val requestFile = MultipartBody.Part.createFormData(
            FILE, fileName, file.asRequestBody(MEDIA_TYPE_IMAGE.toMediaTypeOrNull())
        )
        return apiService.putAttachment(id, requestFile)
    }

    /**
     * 取得檔案
     */
    suspend fun getAttachment(
        id: String
    ) = apiService.getAttachment(id.toLong())

    /**
     * 刪除檔案
     */
    suspend fun deleteAttachment(
        id: String
    ) = apiService.deleteAttachment(id)

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    /**
     * 修改密碼(未登入)
     */
    suspend fun resetPassword(
        body: ResetPasswordRequest
    ) = apiService.resetPassword(body)

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    /**
     * 建立聊天室
     */
    suspend fun postChats(
        body: ChatRequest
    ) = apiService.postChat(body)

    /**
     * 取得聊天室列表
     */
    suspend fun getChats(
        offset: String,
        limit: String
    ) = apiService.getChat(offset, limit)

    /**
     * 發送訊息
     */
    suspend fun postMessage(
        body: MsgRequest
    ) = apiService.postMessage(body)

    /**
     * 取得訊息
     */
    suspend fun getMessage(
        chatId: Long,
        offset: String,
        limit: String
    ) = apiService.getMessage(chatId, offset, limit)

    /**
     * 調整訊息已讀時間
     */
    suspend fun setLastReadMessageTime(
        chatId: Long
    ): Response<Void> {
        val body = HashMap<String, Long>()
        body["chatId"] = chatId
        return apiService.setLastReadMessageTime(body)
    }

    /**
     * 取的未否有未讀訊息
     */
    suspend fun getUnread() = apiService.getUnread()

    /**********************************************************
     *
     *                  Functions
     *
     ***********************************************************/
    /**
     * 取得角色功能列表
     */
    suspend fun getFunctions() = apiService.getFunctions()

    /**********************************************************
     *
     *                  Members
     *
     ***********************************************************/
    /**
     * 修改密碼(已登入)
     */
    suspend fun changePassword(
        body: ChangePasswordRequest
    ) = apiService.changePassword(body)

    /**
     * 忘記密碼
     */
    suspend fun forgetPassword(
        body: ForgetPasswordRequest
    ) = apiService.forgetPassword(body)

    /**
     * 建立新使用者
     */
    suspend fun signUp(
        body: SingUpRequest
    ) = apiService.signUp(body)

    /**
     * 重發驗證信(需登入)
     */
    suspend fun resendEmail(
        body: EmailRequest
    ) = apiService.resendEmail(body)

    suspend fun followPost(userId: Long): Response<Void> {
        Timber.i("userId=$userId")
        return apiService.followPost(userId)
    }

    suspend fun cancelFollowPost(userId: Long): Response<Void> {
        return apiService.cancelFollowPost(userId)
    }

    suspend fun validateMessage(body: ValidateMessageRequest) = apiService.validateMessage(body)

    suspend fun getMemberVideoReport(
        videoId: Long,
        type: Int,
        unhealthy: Boolean = true
    ) = apiService.getMemberVideoReport(videoId, type, unhealthy)

    /**********************************************************
     *
     *                  Members/Post
     *
     ***********************************************************/
    suspend fun searchPostAll(
        type: PostType,
        keyword: String? = null,
        tag: String? = null,
        orderBy: StatisticsOrderType = StatisticsOrderType.LATEST,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.getMembersPost(
            type = type.value,
            keyword = keyword,
            tag = tag,
            orderBy = orderBy.value,
            offset = offset,
            limit = limit
        )
    }

    suspend fun getMembersPost(
        type: PostType,
        orderBy: OrderBy,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.getMembersPost(type.value, offset, limit, orderBy = orderBy.value)
    }

    suspend fun getMembersPost(
        type: PostType,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.getMembersPost(type.value, offset, limit)
    }

    suspend fun getMembersPost(
        offset: Int,
        limit: Int,
        creatorId: Long,
        isAdult: Boolean
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.getMembersPost(offset, limit, creatorId, isAdult)
    }

    suspend fun getMemberPostDetail(postId: Long): Response<ApiBaseItem<MemberPostItem>> {
        return apiService.getMemberPostDetail(postId)
    }

    suspend fun postMembersPost(
        body: PostMemberRequest
    ): Response<ApiBaseItem<Long>> {
        return apiService.postMembersPost(body)
    }

    suspend fun getMembersPostComment(
        postId: Long,
        parentId: Long? = null,
        sorting: Int = 1,
        offset: String,
        limit: String
    ) = apiService.getMembersPostComment(postId, parentId, sorting, offset, limit)

    suspend fun postMembersPostComment(
        postId: Long,
        body: PostCommentRequest
    ) = apiService.postMembersPostComment(postId, body)

    suspend fun postMembersPostCommentLike(
        postId: Long,
        commentId: Long,
        body: PostLikeRequest
    ) = apiService.postMembersPostCommentLike(postId, commentId, body)

    suspend fun deleteMembersPostCommentLike(
        postId: Long,
        commentId: Long
    ) = apiService.deleteMembersPostCommentLike(postId, commentId)

    suspend fun updatePost(
        postId: Long,
        request: PostMemberRequest
    ) = apiService.updatePost(postId, request)

    /**********************************************************
     *
     *                  Members/Club
     *
     ***********************************************************/

    suspend fun getMembersClub(
        tag: String,
        offset: Int?,
        limit: Int?
    ): Response<ApiBasePagingItem<ArrayList<MemberClubItem>>> {
        Timber.i("ClubTabFragment getMembersClub")
        return apiService.getMembersClub(tag, offset, limit)
    }

    suspend fun getMembersClub(
        clubId: Long
    ): Response<ApiBasePagingItem<MemberClubItem>> {
        return apiService.getMembersClub(clubId)
    }

    suspend fun getMembersClubPost(
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberClubItem>>> {
        return apiService.getMembersClubPost(offset, limit)
    }

    suspend fun getMembersClubPost(
        offset: Int,
        limit: Int,
        keyword: String
    ): Response<ApiBasePagingItem<ArrayList<MemberClubItem>>> {
        return apiService.getMembersClubPost(offset, limit, keyword)
    }

    suspend fun getMembersPost(
        offset: Int,
        limit: Int,
        tag: String,
        orderBy: Int,
        isAdult: Boolean = true,
        isFullContent: Boolean = false
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.getMembersPost(offset, limit, tag, orderBy, isAdult, isFullContent)
    }

    suspend fun followClub(clubId: Long): Response<Void> {
        return apiService.followClub(clubId)
    }

    suspend fun cancelFollowClub(clubId: Long): Response<Void> {
        return apiService.cancelFollowClub(clubId)
    }

    /**********************************************************
     *
     *                   Members/Home/Banner
     *
     ***********************************************************/
    /**
     * 取得影片Banner
     */
    suspend fun fetchHomeBanner(bannerCategory: Int) = apiService.fetchHomeBanner(bannerCategory)

    /**********************************************************
     *
     *                   Members/Home/Categories
     *
     ***********************************************************/
    /**
     * 取得影片類別清單
     */
    suspend fun fetchHomeCategories() = apiService.fetchHomeCategories()

    /**
     * 取得影片次類別清單
     */
    suspend fun fetchCategories() = apiService.fetchHomeCategories(202)

    /**********************************************************
     *
     *                   Members/Home/Videos
     *
     ***********************************************************/
    /**
     * 取得影片
     */
    suspend fun searchHomeVideos(
        category: String? = null,
        q: String? = null,
        country: String? = null,
        years: String? = null,
        isAdult: Boolean = true,
        offset: String,
        limit: String,
        tag: String = ""
    ) = apiService.searchHomeVideos(category, q, country, years, isAdult, offset, limit, tag)

    /**
     * 取得小视频影片(需Client Credentials|需登入帳號)
     */
    suspend fun searchShortVideo(
        q: String? = null,
        orderByType: StatisticsOrderType = StatisticsOrderType.LATEST,
        offset: String,
        limit: String
    ) = apiService.searchShortVideo(q, orderByType.value, offset, limit)

    /**
     * 取得類別影片
     */
    suspend fun searchWithCategory(
        category: String,
        isAdult: Boolean,
        offset: String,
        limit: String
    ) = apiService.searchWithCategory(category, isAdult, offset, limit)

    /**
     * 取得熱門影片
     */
    suspend fun statisticsHomeVideos(
        startTime: String = "2018-12-01T10:00:05Z",
        endTime: String = "2020-11-24T10:00:05Z",
        orderByType: Int = StatisticsOrderType.HOTTEST.value,
        category: String? = "",
        tags: String? = "",
        isAdult: Boolean = true,
        isRandom: Boolean = false,
        offset: Int,
        limit: Int
    ) = apiService.statisticsHomeVideos(
        startTime = startTime,
        endTime = endTime,
        orderByType = orderByType,
        category = category,
        tags = tags,
        isAdult = isAdult,
        isRandom = isRandom,
        offset = offset,
        limit = limit
    )

    /**
     * 取得影片排行
     */
    suspend fun getRankingList(
        statisticsType: StatisticsType = StatisticsType.TODAY,
        postType: PostType,
        offset: String,
        limit: String
    ) = apiService.getRankingList(
        statisticsType = statisticsType.value,
        postType = postType.value,
        offset = offset,
        limit = limit
    )

    /**
     * 影片回報
     */
    suspend fun sendVideoReport(
        body: ReportRequest
    ) = apiService.sendVideoReport(
        body
    )

    /**********************************************************
     *
     *                   Members/Home/Actors
     *
     ***********************************************************/
    /**
     * 取得女優頁面
     */
    suspend fun getActors() = apiService.getActors()

    /**
     * 取得女優分頁資料
     */
    suspend fun getActorsList(
        offset: String,
        limit: String
    ) = apiService.getActorsList(offset, limit)

    /**
     * 取得女優分頁資料
     */
    suspend fun getActorsList(
        id: Long
    ) = apiService.getActorsList(id)

    /**********************************************************
     *
     *                  Me
     *
     ***********************************************************/
    /**
     * 取得用者資訊
     */
    suspend fun getMe() = apiService.getMe()

    /**
     * 變更使用者頭像(需登入帳號)
     */
    suspend fun putAvatar(
        body: AvatarRequest
    ) = apiService.putAvatar(body)

    /**
     * 取得我關注的圈子
     */
    suspend fun getMyClubFollow(
        offset: String,
        limit: String
    ) = apiService.getMyClubFollow(offset, limit)

    /**
     * 移除我關注的圈子
     */
    suspend fun cancelMyClubFollow(
        clubId: Long
    ) = apiService.cancelMyClubFollow(clubId)

    /**
     * 取得我關注的人
     */
    suspend fun getMyMemberFollow(
        offset: String,
        limit: String
    ) = apiService.getMyMemberFollow(offset, limit)

    /**
     * 移除我關注的人
     */
    suspend fun cancelMyMemberFollow(
        userId: Long
    ) = apiService.cancelMyMemberFollow(userId)

    /**
     * 取得聊天室列表
     */
    suspend fun getMeChatItem(
        offset: Int,
        limit: Int
    ) = apiService.getMeChat(offset, limit)

    /**
     * 取得聊天室內容
     */
    suspend fun getMeMessage(
        chatId: String,
        offset: Int,
        limit: Int
    ) = apiService.getMeMessage(chatId, offset, limit)

    /**
     * 取得使用者充值記錄
     */
    suspend fun getMeOrder(
        offset: Int,
        limit: Int
    ) = apiService.getMeOrder(offset, limit)

    /**
     * 加入收藏
     */
    suspend fun postMePlaylist(
        body: PlayListRequest
    ) = apiService.addMePlaylist(body)

    /**
     * 刪除使用者列表影片
     */
    suspend fun deleteMePlaylist(
        videoId: String
    ) = apiService.deletePlaylist(videoId)

    /**
     * 取得使用者影片列表 0:History, 1:Favorite
     */
    suspend fun getPlaylist(
        playlistType: Int,
        isAdult: Boolean,
        offset: String,
        limit: String
    ) = apiService.getPlaylist(playlistType, isAdult, offset, limit)

    /**
     * 取得我的帖子收藏 1:postText, 2:postPic , 3:PostShortVideo , 7:postOther, 8:postLongVideoSmallVideo
     */
    suspend fun getPostFavorite(
        offset: Long,
        limit: Int,
        postType: Int = 1,
    ) = apiService.getPostFavorite(offset, limit, postType)

    suspend fun getPostVideoFavorite(
        offset: Long,
        limit: Int
    ) = apiService.getPostFavorite(offset, limit, 8)

    suspend fun getPostOtherFavorite(
        offset: Long,
        limit: Int
    ) = apiService.getPostFavorite(offset, limit, 7)

    /**
     * 移除我的帖子收藏
     */
    suspend fun deletePostFavorite(
        postFavoriteIds: String
    ) = apiService.deletePostFavorite(postFavoriteIds)

    /**
     * 取得我關注的所有帖子(不分人或圈子)
     */
    suspend fun getPostFollow(
        offset: Int,
        limit: Int
    ) = apiService.getPostFollow(offset = offset, limit = limit)

    /**
     * 搜尋我關注的所有帖子
     */
    suspend fun searchPostFollow(
        keyword: String? = null,
        tag: String? = null,
        offset: Int,
        limit: Int
    ) = apiService.getPostFollow(keyword = keyword, tag = tag, offset = offset, limit = limit)

    /**
     * 取得使用者資訊明細
     */
    suspend fun getProfile() = apiService.getProfile()

    /**
     * 修改使用者資訊
     */
    suspend fun updateProfile(
        body: ProfileRequest
    ) = apiService.updateProfile(body)

    /**
     * 取得推廣資訊
     */
    suspend fun getPromotionItem(
        url: String
    ): Response<ApiBaseItem<PromotionItem>> {
        return apiService.getPromotionItem(url)
    }

    /**
     * 邀請朋友名單
     */
    suspend fun getReferrerHistory(
        offset: String,
        limit: String
    ): Response<ApiBasePagingItem<ArrayList<ReferrerHistoryItem>>> {
        return apiService.getReferrerHistory(offset, limit)
    }

    /**********************************************************
     *
     *                  Members/Me/Post
     *
     ***********************************************************/
    suspend fun getMyPost(
        isAdult: Boolean = true,
        status: Int = 1,
        offset: Int,
        limit: Int
    ) = apiService.getMyPost(isAdult, status, offset, limit)

    suspend fun deleteMyPost(
        postId: Long
    ) = apiService.deleteMyPost(postId)

    /**********************************************************
     *
     *                  Members/Post
     *
     ***********************************************************/
    /**
     * 帖子加入收藏
     */
    suspend fun addFavorite(
        postId: Long
    ) = apiService.addFavorite(postId)

    /**
     * 帖子移除收藏
     */
    suspend fun deleteFavorite(
        postId: Long
    ) = apiService.deleteFavorite(postId)

    /**
     * 帖子喜歡/不喜歡
     */
    suspend fun like(
        postId: Long,
        body: LikeRequest
    ) = apiService.like(postId, body)

    /**
     * 帖子移除喜歡/不喜歡
     */
    suspend fun deleteLike(
        postId: Long
    ) = apiService.deleteLike(postId)

    /**
     * 帖子問題回報
     */
    suspend fun sendPostReport(
        postId: Long,
        body: ReportRequest
    ) = apiService.sendPostReport(postId, body)

    /**
     * 帖子評論問題回報
     */
    suspend fun sendPostCommentReport(
        postId: Long,
        commentId: Long,
        body: ReportRequest
    ): Response<Void> {
        return apiService.sendPostCommentReport(postId, commentId, body)
    }

    suspend fun searchPostByTag(
        type: PostType,
        tag: String,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.searchPostByTag(type.value, tag, offset, limit)
    }

    suspend fun searchPostFollowByTag(
        tag: String,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.searchPostFollowByTag(tag, offset, limit)
    }

    suspend fun searchPostByKeyword(
        type: PostType,
        keyword: String,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.searchPostByKeyword(type.value, keyword, offset, limit)
    }

    suspend fun searchPostFollowByKeyword(
        keyword: String,
        offset: Int,
        limit: Int
    ): Response<ApiBasePagingItem<ArrayList<MemberPostItem>>> {
        return apiService.searchPostFollowByKeyword(keyword, offset, limit)
    }

    /**********************************************************
     *
     *                  Ordering
     *
     ***********************************************************/
    /**
     * 取得在線客服列表
     */
    suspend fun getAgent(
        offset: Int,
        limit: Int
    ) = apiService.getAgent(offset, limit)

    /**********************************************************
     *
     *                  Player
     *
     ***********************************************************/
    /**
     * 取得影片資訊
     */
    suspend fun getVideoInfo(
        videoId: Long
    ) = apiService.getVideoInfo(videoId)

    /**
     * 取得影片集數資訊
     */
    suspend fun getVideoEpisode(
        videoId: Long,
        episodeId: Long
    ) = apiService.getVideoEpisode(videoId, episodeId)

    /**
     * 取得影片檔案
     */
    suspend fun getVideoStreamOfEpisode(
        videoId: Long,
        episodeId: Long,
        streamId: Long,
        userId: Long? = null,
        utcTime: Long? = null,
        sign: String? = null
    ) = apiService.getVideoStreamOfEpisode(videoId, episodeId, streamId, userId, utcTime, sign)

    suspend fun getVideoVideoStreamM3u8(
        streamId: Long,
        userId: Long? = null,
        utcTime: Long? = null,
        sign: String? = null
    ) = apiService.getVideoStreamM3u8(streamId, userId, utcTime, sign)

    /**
     * 取得m3u8播放列表檔案
     */
    suspend fun getVideoM3u8Source(
        streamId: Long,
        userId: Long? = null,
        utcTime: Long? = null,
        sign: String? = null
    ) = apiService.getVideoM3u8Source(streamId, userId, utcTime, sign)

    /**
     * 取得在線支付
     */
    suspend fun getOrderingPackage(): Response<ApiBaseItem<ArrayList<OrderingPackageItem>>> {
        return apiService.getOrderingPackage()
    }

    /**
     * 依據PaymentType, 取得在線支付
     */
    suspend fun getOrderingPackageByPaymentType(paymentType: PaymentType): Response<ApiBaseItem<ArrayList<OrderingPackageItem>>> {
        return apiService.getOrderingPackageByPaymentType(paymentType.value)
    }

    suspend fun getPackageStatus(): Response<ApiBaseItem<PackageStatusItem>> {
        return apiService.getPackageStatus()
    }

    /**
     * 建立訂單
     */
    suspend fun createOrder(request: CreateOrderRequest): Response<Void> {
        return apiService.createOrder(request)
    }

    /**
     * 取得充值管理
     */
    suspend fun getOrder(
        offset: String,
        limit: String
    ): Response<ApiBasePagingItem<OrderContentItem>> {
        return apiService.getOrder(offset, limit)
    }

    /**
     * 依據isOnline, 取得充值管理
     */
    suspend fun getOrderByType(
        type: OrderType,
        offset: String,
        limit: String
    ): Response<ApiBasePagingItem<OrderContentItem>> {
        return apiService.getOrderByType(type.value, offset, limit)
    }

    /**
     * 取得未完成訂單
     */
    suspend fun getPendingOrderCount(): Response<ApiBaseItem<PendingOrderItem>> {
        return apiService.getPendingOrderCount()
    }

    /**
     * 建立工單聊天室
     */
    suspend fun createOrderChat(request: CreateChatRequest): Response<ApiBaseItem<CreateOrderChatItem>> {
        return apiService.createOrderChat(request)
    }

    /**
     * 取得未讀工單數量
     */
    suspend fun getUnReadOrderCount(): Response<ApiBaseItem<Int>> {
        return apiService.getUnReadOrderCount()
    }

    /**
     * 取得工單聊天室內容
     */
    suspend fun getOrderChatContent(
        id: Long,
        offset: String,
        limit: String
    ): Response<ApiBasePagingItem<OrderChatContentItem>> {
        return apiService.getOrderChatContent(id, offset, limit)
    }

    /**
     * 更新工單聊天室最後讀取時間
     */
    suspend fun updateOrderChatLastReadTime(id: Long): Response<Void> {
        return apiService.updateOrderChatLastReadTime(id)
    }

    /**
     * 更新工單狀態
     */
    suspend fun updateOrderChatStatus(id: Long): Response<Void> {
        return apiService.updateOrderChatStatus(id)
    }

    /**
     * 取得訪客資訊
     */
    suspend fun getGuestInfo() = apiService.getGuestInfo()

    /**********************************************************
     *
     *                  Members/Home/Menu
     *
     ***********************************************************/
    suspend fun getMenu() = apiService.getMenu()

    /**
     * 取得各來源解碼key
     */
    suspend fun getDecryptSetting() = apiService.getDecryptSetting()
}

