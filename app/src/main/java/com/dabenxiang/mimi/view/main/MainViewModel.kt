package com.dabenxiang.mimi.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.MQTT_HOST_URL
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.manager.mqtt.callback.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.MessageListener
import com.dabenxiang.mimi.model.manager.mqtt.callback.SubscribeCallback
import com.dabenxiang.mimi.model.vo.CheckStatusItem
import com.dabenxiang.mimi.model.vo.StatusItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class MainViewModel : BaseViewModel() {

    var needCloseApp = false // 判斷是否需要離開 app
    var isFromPlayer = false
    var isMqttConnect = false

    val messageListenerMap = hashMapOf<String, MessageListener>()

    private val clientId = UUID.randomUUID().toString()

    private val _adultMode = MutableLiveData(false)
    val adultMode: LiveData<Boolean> = _adultMode

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

    private var _normal: CategoriesItem? = null
    val normal
        get() = _normal

    private var _adult: CategoriesItem? = null
    val adult
        get() = _adult

    var isVersionChecked = false

    fun setupNormalCategoriesItem(item: CategoriesItem?) {
        _normal = item
    }

    fun setupAdultCategoriesItem(item: CategoriesItem?) {
        _adult = item
    }

    fun setAdultMode(isAdult: Boolean) {
        if (_adultMode.value != isAdult) {
            _adultMode.value = isAdult
        }
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
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
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
        mqttManager.init(MQTT_HOST_URL, clientId, extendedCallback)
        mqttManager.connect(connectCallback)
    }

    fun subscribeToTopic(topic: String) {
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

    fun publishMessageByTopic(topic: String, msg: String) {
        mqttManager.publishMessage(topic, msg)
    }

    private val extendedCallback = object : ExtendedCallback {
        override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
            Timber.d("Reconnect: $reconnect")
            Timber.d("Connect: $serverURI")
        }

        override fun onMessageArrived(topic: String, message: MqttMessage) {
            Timber.d("Incoming topic:: $topic")
            Timber.d("Incoming message:: ${String(message.payload)}")
            messageListenerMap[topic]?.onMsgReceive(message)
        }

        override fun onConnectionLost(cause: Throwable) {
            Timber.e("The Connection was lost: $cause")
        }

        override fun onDeliveryComplete(token: IMqttDeliveryToken) {
            Timber.d("DeliveryComplete message:: ${String(token.message.payload)}")
        }
    }

    private val connectCallback = object : ConnectCallback {
        override fun onSuccess(asyncActionToken: IMqttToken) {
            Timber.d("Connection onSuccess")
            isMqttConnect = true
        }

        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
            Timber.e("Connection onFailure: $exception")
            isMqttConnect = false
        }
    }

}