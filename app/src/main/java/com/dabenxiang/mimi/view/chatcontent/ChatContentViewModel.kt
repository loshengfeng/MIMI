package com.dabenxiang.mimi.view.chatcontent

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
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
import java.net.URLConnection
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class ChatContentViewModel : BaseViewModel() {

    companion object {
        const val PREFIX_CHAT = "/chat/"
    }

    private val _chatListResult = MutableLiveData<PagedList<ChatContentItem>>()
    val chatListResult: LiveData<PagedList<ChatContentItem>> = _chatListResult

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private var _postAttachmentResult = MutableLiveData<ApiResult<UploadPicItem>>()
    val postAttachmentResult: LiveData<ApiResult<UploadPicItem>> = _postAttachmentResult

    private var _fileAttachmentTooLarge = MutableLiveData<Boolean>()
    val fileAttachmentTooLarge: LiveData<Boolean> = _fileAttachmentTooLarge

    private val FILE_LIMIT = 5
    private val mqttManager: MQTTManager by inject()
    private val serverUrl = BuildConfig.MQTT_HOST
    private val clientId = UUID.randomUUID().toString()
    var topic: String = ""
    var messageType: Int = ChatMessageType.TEXT.ordinal


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
    }

    private fun isImageFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("image")
    }

    private fun getTimeFormatForPush(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())
    }

    fun getChatContent(chatId: Long) {
        viewModelScope.launch {
            val dataSrc = ChatContentListDataSource(
                    viewModelScope,
                    domainManager,
                    chatId,
                    pagingCallback
            )
            dataSrc.isInvalid
            val factory = ChatContentListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                    .setPageSize(ChatContentListDataSource.PER_LIMIT.toInt())
                    .build()

            LivePagedListBuilder(factory, config).build().asFlow()
                    .collect { _chatListResult.postValue(it) }
        }
    }

    fun setLastRead() {

    }

    fun getAttachment(id: String, position: Int) {
        Timber.d("neo, id = ${id}")
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                        id = id,
                        bitmap = bitmap,
                        position = position
                )
                emit(ApiResult.success(item))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _attachmentResult.value = it }
        }
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