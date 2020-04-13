package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.api.vo.*

class ApiRepository(private val apiService: ApiService) {

    companion object {
        const val MEDIA_TYPE_JSON = "application/json"
    }

    /**********************************************************
     *
     *                  Attachment x 4
     *
     ***********************************************************/
    suspend fun postAttachment(
        body: String
    ) = apiService.postAttachment(body)

    suspend fun getAttachment(
        id: String
    ) = apiService.getAttachment(id)

    suspend fun putAttachment(
        id: String,
        body: String
    ) = apiService.putAttachment(id, body)

    suspend fun deleteAttachment(
        id: String
    ) = apiService.deleteAttachment(id)

    /**********************************************************
     *
     *                  Auth x 1
     *
     ***********************************************************/
    suspend fun resetPassword(
        body: ResetPasswordRequest
    ) = apiService.resetPassword(body)

    /**********************************************************
     *
     *                  Chats x 4
     *
     ***********************************************************/
    suspend fun postChats(
        body: ChatRequest
    ) = apiService.postChat(body)

    suspend fun getChats(
        offset: String,
        limit: String
    ) = apiService.getChat(offset, limit)

    suspend fun postMessage(
        body: MsgRequest
    ) = apiService.postMessage(body)

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
    suspend fun getFunctions() = apiService.getFunctions()

    /**********************************************************
     *
     *                   Members/Home/Categories x 1
     *
     ***********************************************************/
    suspend fun fetchHomeCategories() = apiService.fetchHomeCategories()

    /**********************************************************
     *
     *                   Members/Home/Videos x 2
     *
     ***********************************************************/
    suspend fun searchHomeVideos(
        category: String,
        q: String,
        offset: Int,
        limit: Int
    ) = apiService.searchHomeVideos(category, q, offset, limit)

    suspend fun statisticsHomeVideos(
        statisticsType: Int,
        offset: Int,
        limit: Int
    ) = apiService.statisticsHomeVideos(statisticsType, offset, limit)

    /**********************************************************
     *
     *                  Me x 14
     *
     ***********************************************************/
    suspend fun getMe() = apiService.getMe()

    suspend fun changePassword(
        password: String
    ) = apiService.changePassword(password)

    suspend fun getMeChatItem(
        offset: Int,
        limit: Int
    ) = apiService.getMeChat(offset, limit)

    suspend fun getMeMessage(
        chatId: String,
        offset: Int,
        limit: Int
    ) = apiService.getMeMessage(chatId, offset, limit)

    suspend fun getMeOrder(
        offset: Int,
        limit: Int
    ) = apiService.getMeOrder(offset, limit)

    suspend fun postMePlaylist(
        body: PlayListRequest
    ) = apiService.addMePlaylist(body)

    suspend fun deleteMePlaylist(
        ids : List<Int>
    ) = apiService.deleteMePlaylist(ids)

    suspend fun getMePlaylist(
        playlistType: Int, offset: Int, limit: Int
    ) = apiService.getMePlaylist(playlistType, offset, limit)

    suspend fun getMeProfile() = apiService.getMeProfile()

    suspend fun updatedMeProfile(
        body: MeProfileItem
    ) = apiService.updatedMeProfile(body)

    suspend fun forgetPassword(
        body: ForgetPasswordRequest
    ) = apiService.forgetPassword(body)

    suspend fun signUp(
        body: MembersAccountItem
    ) = apiService.signUp(body)

    suspend fun validationEmail(
        body: ValidateEmailRequest
    ) = apiService.validationEmail(body)

    suspend fun validationEmail(
        key: String
    ) = apiService.validationEmail(key)

    /**********************************************************
     *
     *                  Ordering x 1
     *
     ***********************************************************/
    suspend fun getAgent(
        offset: Int,
        limit: Int
    ) =  apiService.getAgent(offset, limit)

    /**********************************************************
     *
     *                  Player x 3
     *
     ***********************************************************/
    suspend fun getVideoInfo(
        videoId: Int
    ) = apiService.getVideoInfo(videoId)

    suspend fun getVideoEpisode(
        videoId: Int,
        episodeId: Int
    ) = apiService.getVideoEpisode(videoId, episodeId)

    suspend fun getVideoStreamOfEpisode(
        videoId: Int,
        episodeId: Int,
        streamId: Int
    ) = apiService.getVideoStreamOfEpisode(videoId, episodeId, streamId)
}

