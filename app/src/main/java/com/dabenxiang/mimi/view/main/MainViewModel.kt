package com.dabenxiang.mimi.view.main

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.MQTT_HOST_URL
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.NotifyType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.manager.mqtt.callback.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.MessageListener
import com.dabenxiang.mimi.model.manager.mqtt.callback.SubscribeCallback
import com.dabenxiang.mimi.model.vo.CheckStatusItem
import com.dabenxiang.mimi.model.vo.StatusItem
import com.dabenxiang.mimi.model.vo.mqtt.DailyCheckInItem
import com.dabenxiang.mimi.model.vo.mqtt.OrderItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.HomeViewModel
import com.dabenxiang.mimi.view.mypost.MyPostViewModel
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.net.URLEncoder
import java.util.*

class MainViewModel : BaseViewModel() {

    var needCloseApp = false // 判斷是否需要離開 app
    var isFromPlayer = false

    val messageListenerMap = hashMapOf<String, MessageListener>()

    private val clientId = UUID.randomUUID().toString()

    private val _categoriesData = MutableLiveData<ApiResult<ApiBaseItem<RootCategoriesItem>>>()
    val categoriesData: LiveData<ApiResult<ApiBaseItem<RootCategoriesItem>>> = _categoriesData

    private val _getAdResult = MutableLiveData<ApiResult<AdItem>>()
    val getAdResult: LiveData<ApiResult<AdItem>> = _getAdResult

    private val _getAdHomeResult = MutableLiveData<Pair<Int, ApiResult<AdItem>>>()
    val getAdHomeResult: LiveData<Pair<Int, ApiResult<AdItem>>> = _getAdHomeResult

    private val _checkStatusResult by lazy { MutableLiveData<ApiResult<CheckStatusItem>>() }
    val checkStatusResult: LiveData<ApiResult<CheckStatusItem>> get() = _checkStatusResult

    private val _postReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postReportResult: LiveData<ApiResult<Nothing>> = _postReportResult

    private val _orderItem = MutableLiveData<OrderItem>()
    val orderItem: LiveData<OrderItem> = _orderItem

    private val _dailyCheckInItem = MutableLiveData<DailyCheckInItem>()
    val dailyCheckInItem: LiveData<DailyCheckInItem> = _dailyCheckInItem

    private val _uploadPicItem = MutableLiveData<PicParameter>()
    val uploadPicItem: LiveData<PicParameter> = _uploadPicItem

    private val _uploadCoverItem = MutableLiveData<PicParameter>()
    val uploadCoverItem: LiveData<PicParameter> = _uploadCoverItem

    private val _postPicResult = MutableLiveData<ApiResult<Long>>()
    val postPicResult: LiveData<ApiResult<Long>> = _postPicResult

    private val _postCoverResult = MutableLiveData<ApiResult<Long>>()
    val postCoverResult: LiveData<ApiResult<Long>> = _postCoverResult

    private val _postVideoResult = MutableLiveData<ApiResult<Long>>()
    val postVideoResult: LiveData<ApiResult<Long>> = _postVideoResult

    private val _postVideoMemberResult = MutableLiveData<ApiResult<Long>>()
    val postVideoMemberResult: LiveData<ApiResult<Long>> = _postVideoMemberResult

    private val _postDeleteAttachment = MutableLiveData<ApiResult<Nothing>>()
    val postDeleteAttachment: LiveData<ApiResult<Nothing>> = _postDeleteAttachment

    private val _postDeleteCoverAttachment = MutableLiveData<ApiResult<Nothing>>()
    val postDeleteCoverAttachment: LiveData<ApiResult<Nothing>> = _postDeleteCoverAttachment

    private val _postDeleteVideoAttachment = MutableLiveData<ApiResult<Nothing>>()
    val postDeleteVideoAttachment: LiveData<ApiResult<Nothing>> = _postDeleteVideoAttachment

    private val _totalUnreadResult = MutableLiveData<ApiResult<Int>>()
    val totalUnreadResult: LiveData<ApiResult<Int>> = _totalUnreadResult

    private var _normal: CategoriesItem? = null
    val normal
        get() = _normal

    private var _adult: CategoriesItem? = null
    val adult
        get() = _adult

    var isVersionChecked = false

    private val _postArticleResult = MutableLiveData<ApiResult<Long>>()
    val postArticleResult: LiveData<ApiResult<Long>> = _postArticleResult

    val switchBottomTap = MutableLiveData<Int>()
    val changeNavigationPosition = MutableLiveData<Int>()
    val refreshBottomNavigationBadge = MutableLiveData<Int>()

    private var job = Job()

    fun setupNormalCategoriesItem(item: CategoriesItem?) {
        _normal = item
    }

    fun setupAdultCategoriesItem(item: CategoriesItem?) {
        _adult = item
    }

    fun getHomeCategories() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().fetchHomeCategories()
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _categoriesData.value = it }
        }
    }

    fun getAd(position: Int, width: Int, height: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getAdRepository().getAD(width, height)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _getAdHomeResult.value = Pair(position, it) }
        }
    }

    fun getAd(width: Int, height: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getAdRepository().getAD(width, height)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _getAdResult.value = it }
        }
    }

    /**
     * 按下 back 離開的 timer
     */
    fun startBackExitAppTimer() {
        needCloseApp = true
        viewModelScope.launch {
            for (second in 2 downTo 0) {
                delay(1000)
            }
            needCloseApp = false
        }
    }

    fun getCategory(title: String, isAdult: Boolean): CategoriesItem? {
        val item = if (isAdult) _adult else _normal
        var result: CategoriesItem? = null
        item?.categories?.forEach {
            if (it.name == title) {
                result = it
            }
        }
        return result
    }

    fun sendPostReport(item: MemberPostItem, content: String) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val result = domainManager.getApiRepository().sendPostReport(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                item.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

    fun sendCommentPostReport(
        postItem: MemberPostItem,
        postCommentItem: MembersPostCommentItem,
        content: String
    ) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.sendPostCommentReport(
                    postItem.id, postCommentItem.id!!, request
                )
                if (!result.isSuccessful) throw HttpException(result)
                postCommentItem.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

    fun checkStatus(onConfirmed: () -> Unit) {
        viewModelScope.launch {
            flow {
                //TODO: 目前先不判斷是否有驗證過
//                var status = StatusItem.NOT_LOGIN
//                if (accountManager.isLogin()) {
//                    val result = domainManager.getApiRepository().getMe()
//                    val isEmailConfirmed = result.body()?.content?.isEmailConfirmed ?: false
//                    status = if (result.isSuccessful && isEmailConfirmed) {
//                        StatusItem.LOGIN_AND_EMAIL_CONFIRMED
//                    } else {
//                        StatusItem.LOGIN_BUT_EMAIL_NOT_CONFIRMED
//                    }
//                }
                val status = when {
                    accountManager.isLogin() -> StatusItem.LOGIN_AND_EMAIL_CONFIRMED
                    else -> StatusItem.NOT_LOGIN
                }
                emit(ApiResult.success(CheckStatusItem(status, onConfirmed)))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _checkStatusResult.value = it }
        }
    }

    fun startMQTT() {
        if (isMqttConnect()) {
            // test serverUrl use: tcp://172.x.x.x:1883
            mqttManager.init(MQTT_HOST_URL, clientId, extendedCallback)
            mqttManager.connect(connectCallback)
        }
    }

    fun subscribeToTopic(topic: String) {
        mqttManager.subscribeToTopic(topic, object : SubscribeCallback {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Timber.d("onSuccess: $topic, $asyncActionToken")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Timber.d("onFailure: $asyncActionToken, $exception")
            }

            override fun onSubscribe(topic: String, message: MqttMessage) {
                Timber.d("onSubscribe: $topic, $message")
            }
        })
    }

    fun publishMessageByTopic(topic: String, msg: String) {
        mqttManager.publishMessage(topic, msg)
    }

    fun isMqttConnect(): Boolean {
        return mqttManager.isMqttConnect()
    }

    fun getNotificationTopic(): String {
        val userId = accountManager.getProfile().userId
        return StringBuilder(MQTTManager.PREFIX_NOTIFICATION).append(userId).toString()
    }

    private val extendedCallback = object : ExtendedCallback {
        override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
            Timber.d("Reconnect: $reconnect, ServerURI: $serverURI")
        }

        override fun onMessageArrived(topic: String, message: MqttMessage) {
            Timber.d("Incoming topic:: $topic")
            Timber.d("Incoming message:: ${String(message.payload)}")
            messageListenerMap[topic]?.onMsgReceive(message)
        }

        override fun onConnectionLost(cause: Throwable?) {
            Timber.e("The Connection was lost: $cause")
        }

        override fun onDeliveryComplete(token: IMqttDeliveryToken) {
            Timber.d("DeliveryComplete message:: ${String(token.message.payload)}")
        }
    }

    private val connectCallback = object : ConnectCallback {
        override fun onSuccess(asyncActionToken: IMqttToken) {
            Timber.d("Connection onSuccess")
            val topic = getNotificationTopic()
            messageListenerMap[topic] = messageListener
            subscribeToTopic(topic)
        }

        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
            Timber.e("Connection onFailure: $exception")
        }
    }

    private val messageListener = object : MessageListener {
        override fun onMsgReceive(message: MqttMessage) {
            Timber.d("onMsgReceive: ${String(message.payload)}")
            val data = JSONObject(String(message.payload))
            val payload = data.optJSONObject("payload")
            when (payload.optInt("type", 0)) {
                NotifyType.DAILY_CHECK_IN.value -> {
                    val data = gson.fromJson(String(message.payload), DailyCheckInItem::class.java)
                    val profileItem = accountManager.getProfile()
                    data.dailyCheckInPayLoadItem?.videoCount?.let { profileItem.videoCount = it }
                    data.dailyCheckInPayLoadItem?.videoOnDemandCount?.let {
                        profileItem.videoOnDemandCount = it
                    }
                    accountManager.setupProfile(profileItem)
                    _dailyCheckInItem.postValue(data)
                }
                NotifyType.CREATE_ORDER.value,
                NotifyType.TX_SUCCESS.value -> {
                    val data = gson.fromJson(String(message.payload), OrderItem::class.java)
                    _orderItem.postValue(data)
                }
            }
        }
    }

    fun postArticle(title: String, content: String, tags: ArrayList<String>, item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                val request = PostMemberRequest(
                    title = title,
                    content = content,
                    type = PostType.TEXT.value,
                    tags = tags
                )

                if (item.id.toInt() == 0) {
                    val resp = domainManager.getApiRepository().postMembersPost(request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(resp.body()?.content))
                } else {
                    val resp = domainManager.getApiRepository().updatePost(item.id, request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(item.id))
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postArticleResult.value = it }
        }
    }

    fun postAttachment(pic: String, context: Context, type: String) {
        viewModelScope.launch(context = job) {
            flow {
                val realPath = when (type) {
                    HomeViewModel.TYPE_VIDEO -> pic
                    HomeViewModel.TYPE_PIC -> pic
                    else -> UriUtils.getPath(context, Uri.parse(pic))
                }
                val fileNameSplit = realPath?.split("/")
                val fileName = fileNameSplit?.last()
                val extSplit = fileName?.split(".")
                val ext = "." + extSplit?.last()
                var mime: String? = null

                if (type == HomeViewModel.TYPE_PIC) {
                    val picParameter = PicParameter(ext = ext)
                    _uploadPicItem.postValue(picParameter)
                } else if (type == HomeViewModel.TYPE_COVER) {
                    val picParameter = PicParameter(ext = ext)
                    _uploadCoverItem.postValue(picParameter)
                } else if (type == HomeViewModel.TYPE_VIDEO) {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(realPath)
                    mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                }

                Timber.d("Upload photo path : $realPath")
                Timber.d("Upload photo ext : $ext")

                val result = if (mime == null) {
                    domainManager.getApiRepository().postAttachment(
                        File(realPath!!),
                        fileName = URLEncoder.encode(fileName, "UTF-8")
                    )
                } else {
                    domainManager.getApiRepository().postAttachment(
                        File(realPath!!),
                        fileName = URLEncoder.encode(fileName, "UTF-8"),
                        type = mime
                    )
                }

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (type) {
                        HomeViewModel.TYPE_PIC -> _postPicResult.postValue(it)
                        HomeViewModel.TYPE_COVER -> _postCoverResult.postValue(it)
                        HomeViewModel.TYPE_VIDEO -> _postVideoResult.postValue(it)
                    }
                }
        }
    }

    fun postPic(id: Long = 0, request: PostMemberRequest, content: String) {
        viewModelScope.launch(context = job) {
            flow {
                request.content = content
                Timber.d("Post member request : $request")

                if (id.toInt() == 0) {
                    val resp = domainManager.getApiRepository().postMembersPost(request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(resp.body()?.content))
                } else {
                    val resp = domainManager.getApiRepository().updatePost(id, request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(id))
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postVideoMemberResult.value = it }
        }
    }

    fun deleteAttachment(id: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().deleteAttachment(id)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postDeleteAttachment.value = it }
        }
    }

    fun deleteVideoAttachment(id: String, type: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().deleteAttachment(id)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    if (type == MyPostViewModel.TYPE_COVER) {
                        _postDeleteCoverAttachment.postValue(it)
                    } else if (type == MyPostViewModel.TYPE_VIDEO) {
                        _postDeleteVideoAttachment.postValue(it)
                    }
                }
        }
    }

    fun clearLiveDataValue() {
        _postArticleResult.value = null
        _postPicResult.value = null
        _postCoverResult.value = null
        _postVideoResult.value = null
        _uploadPicItem.value = null
        _uploadCoverItem.value = null
    }

    fun cancelJob() {
        job.cancel()
    }

    fun getTotalUnread() {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val chatUnreadResult = apiRepository.getUnread()
                val chatUnread =
                    if (!chatUnreadResult.isSuccessful) 0 else chatUnreadResult.body()?.content ?: 0
                val orderUnreadResult = apiRepository.getUnReadOrderCount()
                val orderUnread =
                    if (!orderUnreadResult.isSuccessful) 0 else orderUnreadResult.body()?.content
                        ?: 0
                emit(ApiResult.success(chatUnread + orderUnread))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _totalUnreadResult.value = it }
        }
    }
}