package com.dabenxiang.mimi.view.chatcontent

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.api.vo.ChatContentPayloadItem
import com.dabenxiang.mimi.model.api.vo.MQTTChatItem
import com.dabenxiang.mimi.model.enums.ChatMessageType
import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager.Companion.PREFIX_CHAT
import com.dabenxiang.mimi.model.vo.UploadPicItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.URLConnection
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatContentViewModel : BaseViewModel() {

    companion object {
        const val FILE_LIMIT = 5
        const val PER_LIMIT = "10"
    }

    private val _chatListResult = MutableLiveData<ApiResult<ArrayList<ChatContentItem>>>()
    val chatListResult: LiveData<ApiResult<ArrayList<ChatContentItem>>> = _chatListResult

    private val _setLastReadResult = MutableLiveData<ApiResult<Nothing>>()
    val setLastReadResult: LiveData<ApiResult<Nothing>> = _setLastReadResult

    private var _attachmentResult = MutableLiveData<ApiResult<String>>()
    val attachmentResult: LiveData<ApiResult<String>> = _attachmentResult

    private var _postAttachmentResult = MutableLiveData<ApiResult<UploadPicItem>>()
    val postAttachmentResult: LiveData<ApiResult<UploadPicItem>> = _postAttachmentResult

    private var _fileAttachmentTooLarge = MutableLiveData<Boolean>()
    val fileAttachmentTooLarge: LiveData<Boolean> = _fileAttachmentTooLarge

    private var _cachePushData = MutableLiveData<ChatContentItem>()
    val cachePushData: LiveData<ChatContentItem> = _cachePushData

    private var _updatePushData = MutableLiveData<ChatContentItem>()
    val updatePushData: LiveData<ChatContentItem> = _updatePushData

    private var _updateOrderChatStatusResult = MutableLiveData<ApiResult<Nothing>>()
    val updateOrderChatStatusResult: LiveData<ApiResult<Nothing>> = _updateOrderChatStatusResult

    private var _mqttSendErrorResult = MutableLiveData<Boolean>()
    val mqttSendErrorResult: LiveData<Boolean> = _mqttSendErrorResult

    var messageType: Int = ChatMessageType.TEXT.ordinal
    var isLoading: Boolean = false
    var chatId: Long = -1
    var traceLogId: Long = -1
    var offset: Int = 0
    var noMore: Boolean = false
    var isOnline: Boolean = false

    var videoCache: HashMap<String, ChatContentItem> = HashMap()
    var fileUploadCache: HashMap<String, Int> = HashMap()

    fun getChatContent() {
        viewModelScope.launch {
            flow {
                val result = if (isOnline) {
                    domainManager.getApiRepository().getOrderChatContent(
                        traceLogId,
                        offset = offset.toString(),
                        limit = PER_LIMIT
                    )
                } else {
                    domainManager.getApiRepository().getMessage(
                        chatId,
                        offset = offset.toString(),
                        limit = PER_LIMIT
                    )
                }
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val size = item?.content?.messages?.size ?: 0
                val messages = adjustData(item?.content?.messages ?: ArrayList())
                val totalCount = item?.paging?.count ?: 0
                val nextPageKey = when {
                    hasNextPage(totalCount, item?.paging?.offset ?: 0, size) -> {
                        if (offset == 0) size else (offset + size)
                    }
                    else -> null
                }

                if (nextPageKey != null) {
                    offset = nextPageKey.toString().toInt()
                } else {
                    noMore = true
                }
                emit(ApiResult.success(messages))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _chatListResult.value = it }
        }
    }

    fun setLastRead() {
        viewModelScope.launch {
            flow {
                val result = if (isOnline) {
                    domainManager.getApiRepository().updateOrderChatLastReadTime(traceLogId)
                } else {
                    domainManager.getApiRepository().setLastReadMessageTime(chatId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _setLastReadResult.value = it }
        }
    }

    fun getAttachment(context: Context, id: String) {
        var fileName = ""
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                fileName = result.headers()["Content-Disposition"]
                    ?.substringAfter("UTF-8''")
                    .toString()
                if (fileName == null || fileName.isEmpty() || byteArray == null) throw Exception(
                    "File name or array error"
                )

                val path = "${FileUtil.getVideoFolderPath(context)}$fileName"
                if (!File(path).exists()) {
                    convertByteToVideo(context, byteArray, fileName)
                }
                emit(ApiResult.success(path))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading(fileName)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult.value = it }
        }
    }

    /**
     * 判斷是檔案是否為圖像檔案
     */
    private fun isImageFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("image")
    }

    /**
     * 取得要送訊息的時間格式
     */
    private fun getTimeFormatForPush(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
    }

    /**
     * 計算是否有下一頁需要撈取聊天訊息
     */
    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT.toInt() -> false
            offset >= total -> false
            else -> true
        }
    }

    /**
     * insert Time Title
     */
    private fun adjustData(list: ArrayList<ChatContentItem>): ArrayList<ChatContentItem> {
        val result: ArrayList<ChatContentItem> = ArrayList()
        var lastDate: String = ""
        for (i: Int in list.indices) {
            val item = list[i]
            item.payload?.sendTime?.let { date ->
                val currentDate = SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date)
                if (lastDate.isNotEmpty() && lastDate != currentDate) {
                    result.add(ChatContentItem(dateTitle = lastDate))
                }
                result.add(item)
                lastDate = currentDate
                if (i == list.size - 1) {
                    result.add(ChatContentItem(dateTitle = lastDate))
                }
            }
        }
        return result
    }

    /**
     * 將 byteArray 寫入 Video
     */
    private fun convertByteToVideo(
        context: Context,
        streamArray: ByteArray,
        fileName: String
    ): String {
        val path = getVideoPath(context, fileName)
        val out = FileOutputStream(path)
        out.write(streamArray)
        out.close()
        return path
    }

    private fun convertStringToDate(dtStart: String): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        var date: Date? = null
        try {
            date = format.parse(dtStart)
            System.out.println(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    /**
     * 根據檔案名稱取得影片的路徑
     */
    fun getVideoPath(context: Context, fileName: String, ext: String = ""): String {
        return "${FileUtil.getVideoFolderPath(context)}$fileName$ext"
    }

    /**
     * 上傳影片 or 圖片
     * 流程：Step 1 -> 判斷是否大於可上傳限制
     *      Step 2 -> 根據圖片或是影片，做一個假的 Message 先放到列表上
     *      Step 3 -> 利用 AttachmentID 當作 key 去儲存上傳檔案 uri 的 HashCode
     *      Step 4 -> 上傳成功就 push message to MQTT Server
     *      Step 5 -> 接收到 MQTT message receiver 時,更新原本列表上的 cache item ( 在 Mqtt receiver 做 )
     */
    fun postAttachment(uri: Uri, context: Context) {
        viewModelScope.launch {
            val realPath = UriUtils.getPath(context, uri)
            val file = File(realPath)
            val fileNameSplit = realPath?.split("/")
            val fileName = fileNameSplit?.last()
            val extSplit = fileName?.split(".")
            val ext = "." + extSplit?.last()

            // Step 1
            if (file.length() / 1024.0 / 1024.0 > FILE_LIMIT) {
                _fileAttachmentTooLarge.value = true
            } else {
                val isImage = isImageFile(realPath)
                messageType = if (isImage) {
                    ChatMessageType.IMAGE.ordinal
                } else {
                    ChatMessageType.BINARY.ordinal
                }

                // Step 2
                genCacheData("", ext, "", uri.hashCode(), VideoDownloadStatusType.UPLOADING, realPath
                        ?: "")

                flow {
                    Timber.d("Upload photo path : $realPath")
                    Timber.d("Upload photo ext : $ext")
                    val type = if (isImage) "image/*" else "video/*"
                    val result = domainManager.getApiRepository().postAttachment(
                        File(realPath),
                        fileName = URLEncoder.encode(fileName, "UTF-8"),
                        type = type
                    )

                    if (!result.isSuccessful) throw HttpException(result)

                    // Step 3
                    fileUploadCache[result.body()?.content.toString()] = uri.hashCode()

                    // Step 4
                    publishMsg((result.body()?.content ?: 0).toString(), ext)

                    val uploadPicItem = UploadPicItem(
                        ext = ext, id = result.body()?.content
                            ?: 0
                    )
                    emit(ApiResult.success(uploadPicItem))
                }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _postAttachmentResult.value = it }
            }
        }
    }

    fun getChatTopic(): String {
        return StringBuilder(PREFIX_CHAT).append(chatId).toString()
    }

    fun processMessage(message: MqttMessage) {
        val messageItem: ChatContentItem = gson.fromJson(
            String(message.payload), ChatContentItem::class.java
        )
        if (!TextUtils.equals(messageItem.username, pref.profileItem.userId.toString())) {
            _cachePushData.value = messageItem
        } else {
            _updatePushData.value = messageItem
        }
    }

    /**
     * 傳送資料時，先做一個假的訊息顯示在列表上
     */
    fun pushMsgWithCacheData(message: String, ext: String = "") {
        val sendTime = getTimeFormatForPush()
        genCacheData(message, ext, sendTime)
        publishMsg(message, ext, sendTime)
    }

    private fun genCacheData(
        message: String,
        ext: String = "",
        sendTime: String = "",
        mediaHashcode: Int = 0,
        downloadStatusType: VideoDownloadStatusType = VideoDownloadStatusType.NORMAL,
        mediaPath: String = ""
    ) {
        val payload = ChatContentPayloadItem(
            messageType,
            message,
            if (TextUtils.isEmpty(sendTime)) null else convertStringToDate(sendTime),
            ext
        )
        val chatContentItem = ChatContentItem(
            pref.profileItem.userId.toString(),
            payload = payload,
            mediaHashCode = mediaHashcode,
            downloadStatus = downloadStatusType,
            cacheImagePath = mediaPath
        )
        _cachePushData.value = chatContentItem
    }

    private fun publishMsg(message: String, ext: String = "", sendTime: String = "") {
        val pushTime = if (TextUtils.isEmpty(sendTime)) {
            getTimeFormatForPush()
        } else {
            sendTime
        }
        val mqttChatItem = MQTTChatItem(ext, message, pushTime, messageType)
        mqttManager.publishMessage(getChatTopic(), gson.toJson(mqttChatItem), mqttCallback)
    }

    private val mqttCallback by lazy {
        object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Timber.d("mqttCallback onSuccess")
                if (isOnline) {
                    updateOrderChatStatus()
                    _mqttSendErrorResult.postValue(false)
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Timber.e("mqttCallback onFailure: $exception")
                _mqttSendErrorResult.postValue(true)
            }
        }
    }

    fun updateOrderChatStatus() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().updateOrderChatStatus(traceLogId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _updateOrderChatStatusResult.value = it
                }
        }
    }
}