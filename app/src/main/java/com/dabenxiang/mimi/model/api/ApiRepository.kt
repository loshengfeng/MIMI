package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*
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
    suspend fun postAttachment(body: String) = apiService.postAttachment(body)

    suspend fun getAttachment(id: String) = apiService.getAttachment(id)

    suspend fun putAttachment(id: String, request: String) = apiService.putAttachment(id, request)

    suspend fun deleteAttachment(id: String) = apiService.deleteAttachment(id)

    /**********************************************************
     *
     *                  Auth
     *
     ***********************************************************/
    suspend fun resetPassword(requestReset: ResetPasswordRequest) = apiService.resetPassword(requestReset)

    suspend fun resetTotp(request: ResetTotpRequest) = apiService.resetTotp(request)

    /**********************************************************
     *
     *                  Chats
     *
     ***********************************************************/
    suspend fun postChats(request: ChatRequest) = apiService.postChat(request)

    suspend fun getChats(offset: String, limit: String) = apiService.getChat(offset, limit)

    suspend fun postMessage(request: MsgRequest) = apiService.postMessage(request)

    suspend fun getMessage(
        chatId: Int, lastReadTime: String, offset: String, limit: String
    ) = apiService.getMessage(chatId, lastReadTime, offset, limit)

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

    suspend fun getMeChatItem(
        offset: Int, limit: Int
    ) = apiService.getMeChat(offset, limit)

    suspend fun getMeMessage(
        chatId: String, offset: Int, limit: Int
    ) = apiService.getMeMessage(chatId, offset, limit)

    suspend fun getMeOrder(
        offset: Int, limit: Int
    ) = apiService.getMeOrder(offset, limit)

    suspend fun deleteMePlaylist(
        ids : List<Int>
    ) = apiService.deleteMePlaylist(ids)

    suspend fun getMePlaylist(
        playlistType: Int, offset: Int, limit: Int
    ) = apiService.getMePlaylist(playlistType, offset, limit)

    suspend fun getMeProfile() = apiService.getMeProfile()

    suspend fun updatedMeProfile(body: MeProfileItem) = apiService.updatedMeProfile(body)

    suspend fun forgetPassword(body: ForgetPasswordRequest) = apiService.forgetPassword(body)

    suspend fun signUp(body: MembersAccountItem) = apiService.signUp(body)

    suspend fun emailValidation(key: String) = apiService.validationEmail(key)

    /**********************************************************
     *
     *                  Merchants
     *
     ***********************************************************/

    /**********************************************************
     *
     *                  Operators
     *
     ***********************************************************/

    /**********************************************************
     *
     *                  Operators/Video
     *
     ***********************************************************/
//    suspend fun getOperatorsVideo(
//        id: Int, key: String, status: Int, offset: Int, limit: Int
//    ) = apiService.getOperatorsVideo(id, key, status, offset, limit)
//
//    suspend fun putOperatorsVideo(request: OperatorsVideoRequest) = apiService.putOperatorsVideo(request)
//
//    suspend fun postCrawler(
//        videoEpisodeIds : List<Int>
//    ) = apiService.postCrawler(videoEpisodeIds)

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
    suspend fun getVideoInfo(videoId: Int) = apiService.getVideoInfo(videoId)

    suspend fun getVideoEpisode(videoId: Int, episodeId: Int) = apiService.getVideoEpisode(videoId, episodeId)

    //TODO: ??
    suspend fun getVideoStreamOfEpisode(videoId: Int, episodeId: Int, streamId: Int) =
        apiService.getVideoStreamOfEpisode(videoId, episodeId, streamId)
}

