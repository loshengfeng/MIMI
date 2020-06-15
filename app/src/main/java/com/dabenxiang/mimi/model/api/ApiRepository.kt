package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.StatisticsType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class ApiRepository(private val apiService: ApiService) {

    companion object {
        const val MEDIA_TYPE_JSON = "application/json"
        const val AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer "
        const val FILE = "file"
        const val MEDIA_TYPE_IMAGE = "image/*"
        fun isRefreshTokenFailed(code: String?): Boolean {
            return code == ErrorCode.TOKEN_NOT_FOUND
        }
    }

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    suspend fun getToken() = apiService.getToken("client_credentials", "3770511208570945536", "1d760dedf35a4a508ecd71b5013a1611")

    /**
     * 更新Token
     */
    suspend fun refreshToken(token: String) =
        apiService.refreshToken("refresh_token", token, "3770511208570945536", "1d760dedf35a4a508ecd71b5013a1611")

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
     *                  Attachment x 4
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
        fileName: String
    ): Response<ApiBaseItem<Long>> {
        val requestFile = MultipartBody.Part.createFormData(
            FILE, fileName, file.asRequestBody(MEDIA_TYPE_IMAGE.toMediaTypeOrNull())
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
        id: Long
    ) = apiService.getAttachment(id)

    /**
     * 刪除檔案
     */
    suspend fun deleteAttachment(
        id: String
    ) = apiService.deleteAttachment(id)

    /**********************************************************
     *
     *                  Auth x 1
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
     *                  Chats x 4
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
        chatId: Int,
        lastReadTime: String,
        offset: String,
        limit: String
    ) = apiService.getMessage(chatId, lastReadTime, offset, limit)

    /**********************************************************
     *
     *                  Functions x 1
     *
     ***********************************************************/
    /**
     * 取得角色功能列表
     */
    suspend fun getFunctions() = apiService.getFunctions()

    /**********************************************************
     *
     *                  Members x 5
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

    /**
     * 驗證信箱
     */
//    suspend fun validationEmail(
//        key: String
//    ) = apiService.validationEmail(key)

    /**********************************************************
     *
     *                   Members/Home/Categories x 1
     *
     ***********************************************************/
    /**
     * 取得影片類別清單
     */
    suspend fun fetchHomeCategories() = apiService.fetchHomeCategories()

    /**********************************************************
     *
     *                   Members/Home/Videos x 2
     *
     ***********************************************************/
    /**
     * 取得類別影片
     */
    suspend fun searchHomeVideos(
        category: String,
        q: String? = null,
        country: String? = null,
        years: Int? = null,
        isAdult: Boolean,
        offset: String,
        limit: String
    ) = apiService.searchHomeVideos(category, q, country, years, isAdult, offset, limit)

    /**
     * 取得熱門影片
     */
    suspend fun statisticsHomeVideos(
        statisticsType: StatisticsType = StatisticsType.Newest, category: String? = null, isAdult: Boolean, offset: Int, limit: Int
    ) = apiService.statisticsHomeVideos(
        statisticsType = statisticsType.ordinal,
        category = category,
        isAdult = isAdult,
        offset = offset,
        limit = limit
    )

    /**********************************************************
     *
     *                  Me x 9
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
        ids: List<Int>
    ) = apiService.deleteMePlaylist(ids)

    /**
     * 取得使用者影片列表 0:History, 1:Favorite
     */
    suspend fun getMePlaylist(
        playlistType: Int, offset: Int, limit: Int
    ) = apiService.getMePlaylist(playlistType, offset, limit)

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

    /**********************************************************
     *
     *                  Ordering x 1
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
     *                  Player x 3
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
}

