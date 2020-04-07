package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.MeProfileItem
import com.dabenxiang.mimi.model.api.vo.MembersAccountItem
import com.dabenxiang.mimi.model.api.vo.PasswordRequest
import com.dabenxiang.mimi.model.api.vo.ResetTotpRequest
import org.json.JSONObject

class ApiRepository(private val apiService: ApiService) {

    companion object {
        const val MEDIA_TYPE_JSON = "application/json"
    }

    /**********************************************************
     *
     *                  Attachment
     *
     ***********************************************************/
    suspend fun getAttachment(id: Int) = apiService.getAttachment(id)

    suspend fun postAttachment(id: Int, body: String) = apiService.postAttachment(id, body)

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    suspend fun resetPassword(request: PasswordRequest) = apiService.resetPassword(request)

    suspend fun resetTotp(request: ResetTotpRequest) = apiService.resetTotp(request)

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    suspend fun getChats(targetUserId: String) = apiService.getChats(targetUserId)

    /**********************************************************
     *
     *                  Functions
     *
     ***********************************************************/
    suspend fun getFunctions() = apiService.getFunctions()

    /**********************************************************
     *
     *                  Home/Categories
     *
     ***********************************************************/
    suspend fun fetchHomeCategories() = apiService.fetchHomeCategories()

    suspend fun searchHomeVideos(
        category: String, q: String, offset: Int, limit: Int
    ) = apiService.searchHomeVideos(category, q, offset, limit)

    //TODO: statisticsType?
    suspend fun statisticsHomeVideos(
        statisticsType: Int, offset: Int, limit: Int
    ) = apiService.statisticsHomeVideos(statisticsType, offset, limit)

    /**********************************************************
     *
     *                  Me
     *
     ***********************************************************/
    suspend fun getMe() = apiService.getMe()

    suspend fun getMeChatItem() = apiService.getMeChat()

    suspend fun getMeMessage(chatId: String) = apiService.getMeMessage(chatId)

    suspend fun getMeOrder() = apiService.getMeOrder()

    //TODO: body內容待確認
    suspend fun deleteMePlaylist(body: JSONObject) = apiService.deleteMePlaylist(body)

    //TODO: playlistType?
    suspend fun getMePlaylist(playlistType: Int) = apiService.getMePlaylist(playlistType)

    //TODO: 沒有用戶參數!!
    suspend fun getMeProfile() = apiService.getMeProfile()

    //TODO: 沒有用戶參數!!, Resp內容？
    suspend fun updatedMeProfile(body: MeProfileItem) = apiService.updatedMeProfile(body)

    /**********************************************************
     *
     *                  Members
     *
     ***********************************************************/
    suspend fun forgetPassword(body: PasswordRequest) = apiService.forgetPassword(body)

    suspend fun signUp(body: MembersAccountItem) = apiService.signUp(body)

    /**********************************************************
     *
     *                  Notification/Email
     *
     ***********************************************************/
    //TODO: 沒有用戶參數!!
    suspend fun emailValidation() = apiService.emailValidation()

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

    /**********************************************************
     *
     *                  Player
     *
     ***********************************************************/
    suspend fun getVideoInfo(videoId: Int) = apiService.getVideoInfo(videoId)

    suspend fun getVideoEpisode(videoId: Int, episodeId: Int) = apiService.getVideoEpisode(videoId, episodeId)

    //TODO: ??
    suspend fun getVideoStreamOfEpisode(videoId: Int, episodeId: Int, streamId: Int) =
        apiService.getVideoStreamOfEpisode(videoId, episodeId, streamId)
}

