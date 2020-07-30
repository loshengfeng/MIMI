package com.dabenxiang.mimi.view.chatcontent

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.api.vo.MQTTChatItem
import com.dabenxiang.mimi.model.enums.ChatMessageType
import com.dabenxiang.mimi.model.manager.mqtt.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.manager.mqtt.SubscribeCallback
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.model.vo.UploadPicItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URLConnection
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatContentViewModel : BaseViewModel() {

    companion object {
        const val PREFIX_CHAT = "/chat/"
    }

    private val PER_LIMIT = "10"
    val TAG_IMAGE = 0
    val TAG_VIDEO = 1

    private val _chatListResult = MutableLiveData<ApiResult<ArrayList<ChatContentItem>>>()
    val chatListResult: LiveData<ApiResult<ArrayList<ChatContentItem>>> = _chatListResult

    private var _attachmentResult = MutableLiveData<ApiResult<out Any>>()
    val attachmentResult: LiveData<ApiResult<out Any>> = _attachmentResult

    private var _postAttachmentResult = MutableLiveData<ApiResult<UploadPicItem>>()
    val postAttachmentResult: LiveData<ApiResult<UploadPicItem>> = _postAttachmentResult

    private var _fileAttachmentTooLarge = MutableLiveData<Boolean>()
    val fileAttachmentTooLarge: LiveData<Boolean> = _fileAttachmentTooLarge

    private var _remakeContentResult = MutableLiveData<Boolean>()
    val remakeContentResult: LiveData<Boolean> = _remakeContentResult

    private val FILE_LIMIT = 5
    private val mqttManager: MQTTManager by inject()
    private val serverUrl = BuildConfig.MQTT_HOST
    private val clientId = UUID.randomUUID().toString()
    var topic: String = ""
    var messageType: Int = ChatMessageType.TEXT.ordinal
    var isLoading: Boolean = false
    var chatId: Long = -1
    var offset: Int = 0
    var noMore: Boolean = false

    var videoCache: HashMap<String, ChatContentItem> = HashMap()


    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {

        }

        override fun onLoaded() {

        }

        override fun onThrowable(throwable: Throwable) {

        }

        override fun onSucceed() {
            super.onSucceed()
        }

        override fun onTotalCount(count: Long) {
            super.onTotalCount(count)
            _remakeContentResult.value = true
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

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT.toInt() -> false
            offset >= total -> false
            else -> true
        }
    }

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

    fun getChatContent() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMessage(
                        chatId,
                        offset = offset.toString(),
                        limit = PER_LIMIT
                )
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val size = item?.content?.size ?: 0
                val messages = adjustData(item?.content as ArrayList<ChatContentItem>)
                val totalCount = item.paging.count
                val nextPageKey = when {
                    hasNextPage(totalCount, item.paging.offset, size) -> if (offset == 0) PER_LIMIT else (PER_LIMIT.toInt() + size)
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

    // todo 加上最後讀取時間api
    fun setLastRead() {

    }

    fun getAttachment(context: Context, id: String, position: Int, type: Int = TAG_IMAGE) {
        var fileName = ""
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                if (type == TAG_IMAGE) {
                    val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                    val item = AttachmentItem(
                            id = id,
                            bitmap = bitmap,
                            position = position
                    )
                    emit(ApiResult.success(item))
                } else {
                    fileName = result.headers()["Content-Disposition"]?.substringAfter("UTF-8''").toString()
                    if (fileName == null || fileName.isEmpty() || byteArray == null) throw Exception("File name or array error")

                    val path = "${FileUtil.getVideoFolderPath(context)}$fileName"
                    if (!File(path).exists()) {
                        convertByteToVideo(context, byteArray, fileName)
                    }
                    emit(ApiResult.success(path))
                }
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading(fileName)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _attachmentResult.value = it }
        }
    }

    /**
     * 將 byteArray 寫入 Video
     */
    private fun convertByteToVideo(context: Context, streamArray: ByteArray, fileName: String): String {
        val path = getVideoPath(context, fileName)
        val out = FileOutputStream(path)
        out.write(streamArray)
        out.close()
        return path
    }

    /**
     * 根據檔案名稱取得影片的路徑
     */
    fun getVideoPath(context: Context, fileName: String, ext: String = ""): String {
        return "${FileUtil.getVideoFolderPath(context)}$fileName$ext"
    }

    /**
     * 上傳影片 or 圖片
     */
    fun postAttachment(uri: Uri, context: Context) {
        viewModelScope.launch {
            val realPath = UriUtils.getPath(context, uri)
            val file = File(realPath)
            if (file.length() / 1024.0 / 1024.0 > FILE_LIMIT) {
                _fileAttachmentTooLarge.value = true
            } else {
                flow {
                    val fileNameSplit = realPath?.split("/")
                    val fileName = fileNameSplit?.last()
                    val extSplit = fileName?.split(".")
                    val ext = "." + extSplit?.last()

                    Timber.d("Upload photo path : $realPath")
                    Timber.d("Upload photo ext : $ext")
                    val result = if (isImageFile(realPath)) {
                        messageType = ChatMessageType.IMAGE.ordinal
                        domainManager.getApiRepository().postAttachment(File(realPath), fileName = URLEncoder.encode(fileName, "UTF-8"), type = "image/*")
                    } else {
                        messageType = ChatMessageType.BINARY.ordinal
                        domainManager.getApiRepository().postAttachment(File(realPath), fileName = URLEncoder.encode(fileName, "UTF-8"), type = "video/*")
                    }

                    if (!result.isSuccessful) throw HttpException(result)
                    val uploadPicItem = UploadPicItem(ext = ext, id = result.body()?.content ?: 0)
                    emit(ApiResult.success(uploadPicItem))
                }
                        .flowOn(Dispatchers.IO)
                        .onStart { emit(ApiResult.loading()) }
                        .onCompletion { emit(ApiResult.loaded()) }
                        .catch { e -> emit(ApiResult.error(e)) }
                        .collect {
                            _postAttachmentResult.value = it
                        }
            }
        }
    }

    fun initMQTT(id: String) {
        topic = PREFIX_CHAT + id
        mqttManager.init(serverUrl, clientId, object : ExtendedCallback {
            override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
                Timber.d("Connect: $serverURI")
                connect()
                if (reconnect) {
                    // TODO:
                }
            }

            override fun onMessageArrived(topic: String, message: MqttMessage) {
                Timber.d("Incoming topic:: $topic")
                Timber.d("Incoming message:: ${String(message.payload)}")
            }

            override fun onConnectionLost(cause: Throwable) {
                Timber.e("The Connection was lost: $cause")
            }

            override fun onDeliveryComplete(token: IMqttDeliveryToken) {
                Timber.d("deliveryComplete message:: ${String(token.message.payload)}")
            }
        })
    }

    fun connect() {
        mqttManager.connect(object : ConnectCallback {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Timber.d("Connection onSuccess")
                //TODO: test code
                subscribe(topic)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Timber.e("Connection onFailure: $exception")
            }
        })
    }

    fun subscribe(topic: String) {
        mqttManager.subscribeToTopic(topic, object : SubscribeCallback {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Timber.d("onSuccess: $asyncActionToken")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Timber.d("onFailure: $asyncActionToken, $exception")
            }

            override fun onSubscribe(topic: String, message: MqttMessage) {
                Timber.d("onSubscribe: $topic, $message")
            }
        })
    }

    fun publishMsg(message: String, ext: String = "") {
        val mqttData = MQTTChatItem(ext, message, getTimeFormatForPush(), messageType)
        mqttManager.publishMessage(topic, gson.toJson(mqttData))
    }
}