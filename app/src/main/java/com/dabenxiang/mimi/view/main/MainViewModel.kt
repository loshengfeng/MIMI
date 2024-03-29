package com.dabenxiang.mimi.view.main

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.emit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.PROJECT_NAME
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
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainViewModel : BaseViewModel() {

    var needCloseApp = false // 判斷是否需要離開 app
    var isFromPlayer = false

    val messageListenerMap = hashMapOf<String, MessageListener>()

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

    private var _orderItem = MutableLiveData<OrderItem>().also { it.postValue(null) }
    val orderItem: LiveData<OrderItem> = _orderItem

    private val _dailyCheckInItem = MutableLiveData<DailyCheckInItem>()
    val dailyCheckInItem: LiveData<DailyCheckInItem> = _dailyCheckInItem

    private val _picExtResult = MutableLiveData<PicParameter>()
    val picExtResult: LiveData<PicParameter> = _picExtResult

    private val _coverExtResult = MutableLiveData<PicParameter>()
    val coverExtResult: LiveData<PicParameter> = _coverExtResult

    private val _videoExtResult = MutableLiveData<String>()
    val videoExtResult: LiveData<String> = _videoExtResult

    private val _postPicResult = MutableLiveData<ApiResult<Long>>()
    val postPicResult: LiveData<ApiResult<Long>> = _postPicResult

    private val _postCoverResult = MutableLiveData<ApiResult<Long>>()
    val postCoverResult: LiveData<ApiResult<Long>> = _postCoverResult

    private val _postVideoResult = MutableLiveData<ApiResult<Long>>()
    val postVideoResult: LiveData<ApiResult<Long>> = _postVideoResult

    private val _postPicMemberResult = MutableLiveData<ApiResult<Long>>()
    val postPicMemberResult: LiveData<ApiResult<Long>> = _postPicMemberResult

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

    private val _isNavTransparent = MutableLiveData<Boolean>()
    val isNavTransparent: LiveData<Boolean> = _isNavTransparent

    private val _isStatusBardDark = MutableLiveData<Boolean>()
    val isStatusBardDark: LiveData<Boolean> = _isStatusBardDark

    private val _isShowSnackBar = MutableLiveData<Boolean>()
    val isShowSnackBar: LiveData<Boolean> = _isShowSnackBar

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

    val uploadData = MutableLiveData<Bundle>()

    val closeAppFromMqtt = MutableLiveData<String>()

    var onTabReselect: (() -> Unit) = {}

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

    fun getAd(code: String, width: Int, height: Int, count: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getAdRepository().getAD(code, width, height, count)
                if (!resp.isSuccessful) emit(ApiResult.success(AdItem()))
                else emit(ApiResult.success(resp.body()?.content?.get(0)?.ad?.first()))
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
                saveReportItemInDB(item)
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
                saveReportItemInDB(postItem)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

    fun saveReportItemInDB(postItem: MemberPostItem){
        mimiDB.postDBItemDao().getMemberPostItemById(postItem.id)?.let {
            it.reported = true
            mimiDB.postDBItemDao().insertMemberPostItem(it)
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

    fun startMQTT(isReconnection:Boolean =false) {

        if (!isMqttConnect() || isReconnection) {
            Timber.d("MQTT -startMQTT init  isReconnection: $isReconnection")
            // test serverUrl use: tcp://172.x.x.x:1883 // mqttManager.init("tcp://172.x.x.x:1883", clientId, extendedCallback)
            mqttManager.init(domainManager.getMqttDomain(isReconnection), clientId, extendedCallback)
            mqttManager.connect(object : ConnectCallback {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Timber.d("MQTT -Connection onSuccess")
                    SendLogManager.v(PROJECT_NAME, "MQTT - Connection onSuccess")
                    val topic = getNotificationTopic()
                    messageListenerMap[topic] = messageListener
                    subscribeToTopic(topic)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Timber.e("MQTT -Connection onFailure: $exception")
                    SendLogManager.e(PROJECT_NAME, "MQTT - Connection onFailure: $exception")
                    reconnection()
                }
            })
        }



    }

    fun testMqtt(){
        //Test
        val tickerChannel = ticker(delayMillis = 10_000, initialDelayMillis = 0)

        CoroutineScope(Dispatchers.IO).launch {
            repeat(10) {
                Timber.d("reconnection repeat$it")
                tickerChannel.receive()
                reconnection()
            }

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
            Timber.d("MQTT -Reconnect: $reconnect, ServerURI: $serverURI")
            SendLogManager.v(
                PROJECT_NAME,
                "MQTT - Reconnect: $reconnect, ServerURI: $serverURI"
            )
        }

        override fun onMessageArrived(topic: String, message: MqttMessage) {
            Timber.d("MQTT -Incoming topic: $topic")
            Timber.d("MQTT -Incoming message: ${String(message.payload)}")

            SendLogManager.v(
                PROJECT_NAME,
                "MQTT - Topic: $topic, Message: ${String(message.payload)}"
            )
            messageListenerMap[topic]?.onMsgReceive(message)
        }

        override fun onConnectionLost(cause: Throwable?) {
            Timber.e("MQTT -The Connection was lost: $cause")
            SendLogManager.e(
                PROJECT_NAME,
                "MQTT - The Connection was lost: $cause"
            )
            reconnection()
        }

        override fun onDeliveryComplete(token: IMqttDeliveryToken) {
            Timber.d("MQTT -DeliveryComplete message: ${String(token.message.payload)}")
            SendLogManager.e(
                PROJECT_NAME,
                "MQTT - DeliveryComplete message:: ${String(token.message.payload)}"
            )
        }

        override fun onInvalidHandle(cause: Throwable?) {
            SendLogManager.e(PROJECT_NAME,"MQTT -IllegalArgumentException:$cause")
            closeAppFromMqtt.postValue(cause?.toString() ?: "")
        }
    }

    fun reconnection()  {
        Timber.d("MQTT -reconnection startMQTT")
        mqttManager.destroyConnection()
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            startMQTT(true)
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
                NotifyType.CREATE_ORDER.value -> {
                    val data = gson.fromJson(String(message.payload), OrderItem::class.java)
                    _orderItem.postValue(data)
                }
            }
        }
    }

    fun postArticle(postClubItem: PostClubItem) {
        viewModelScope.launch {
            flow {
                val request = PostMemberRequest(
                    title = postClubItem.title,
                    content = postClubItem.request,
                    type = PostType.TEXT.value,
                    tags = postClubItem.tags
                )
                if (postClubItem.memberPostItem == null) {
                    val resp = domainManager.getApiRepository().postMembersPost(request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(resp.body()?.content))
                } else {
                    val resp = domainManager.getApiRepository().updatePost(postClubItem.memberPostItem.id, request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(postClubItem.memberPostItem.id))
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postArticleResult.value = it }
        }
    }

    fun postPicAttachment(localPath: String) {
        viewModelScope.launch(context = job) {
            flow {
                val fileName = getFileName(localPath)
                val ext = getFileExt(fileName)

                val picParameter = PicParameter(ext = ext) //Set extension
                _picExtResult.postValue(picParameter)

                val realPath = getPicCompressPath(localPath)

                Timber.d("Upload pic path : $realPath")
                Timber.d("Upload pic ext : $ext")

                val result = domainManager.getApiRepository().postAttachment(
                    File(realPath),
                    fileName = URLEncoder.encode(fileName, "UTF-8")
                )

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _postPicResult.postValue(it)
                }
        }
    }

    fun postCoverAttachment(localPath: String, context: Context) {
        viewModelScope.launch(context = job) {
            flow {
                var realPath = UriUtils.getPath(context, Uri.parse(localPath))
                val fileName = getFileName(localPath)
                val ext = getFileExt(fileName)

                val picParameter = PicParameter(ext = ext) //Set extension
                _coverExtResult.postValue(picParameter)

                realPath = getPicCompressPath(realPath!!)

                Timber.d("Upload cover path : $realPath")
                Timber.d("Upload cover ext : $ext")

                val result = domainManager.getApiRepository().postAttachment(
                    File(realPath),
                    fileName = URLEncoder.encode(fileName, "UTF-8")
                )

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _postCoverResult.postValue(it)
                }
        }
    }

    fun postVideoAttachment(localPath: String) {
        viewModelScope.launch(context = job) {
            flow {
                val fileName = getFileName(localPath)
                val ext = getFileExt(fileName)

                _videoExtResult.postValue(ext)
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(localPath)
                val mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

                Timber.d("Upload video path : $localPath")
                Timber.d("Upload video ext : $ext")

                val result = domainManager.getApiRepository().postAttachment(
                    File(localPath),
                    fileName = URLEncoder.encode(fileName, "UTF-8"),
                    type = mime!!
                )

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _postVideoResult.postValue(it)
                }
        }
    }

    private fun getFileName(path: String): String {
        val fileNameSplit = path.split("/")
        return fileNameSplit.last()
    }

    private fun getFileExt(fileName: String): String {
        val extSplit = fileName.split(".")
        return "." + extSplit.last()
    }

    private fun getPicCompressPath(path: String): String {
        val file = File(path)
        return if (file.length() >= 5242880) {
            val tempFile = FileUtil.getTakePhoto( "temp.jpg")
            file.copyTo(tempFile, true)
            val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
            FileUtil.saveBitmapToJpegFile(bitmap, bitmap.width, bitmap.height, destPath = tempFile.absolutePath)

            tempFile.absolutePath
        } else {
            path
        }
    }

    fun postAttachment(pic: String, context: Context, type: String) {
        viewModelScope.launch(context = job) {
            flow {
                var realPath = when (type) {
                    HomeViewModel.TYPE_VIDEO -> pic
                    HomeViewModel.TYPE_PIC -> pic
                    else -> UriUtils.getPath(context, Uri.parse(pic))
                }
                val fileNameSplit = realPath?.split("/")
                val fileName = fileNameSplit?.last()
                val extSplit = fileName?.split(".")
                val ext = "." + extSplit?.last()
                var mime: String? = null

                when (type) {
                    HomeViewModel.TYPE_PIC -> {
                        val picParameter = PicParameter(ext = ext) //Set extension
                        _picExtResult.postValue(picParameter)
                    }
                    HomeViewModel.TYPE_COVER -> {
                        val picParameter = PicParameter(ext = ext) //Set extension
                        _coverExtResult.postValue(picParameter)
                    }
                    HomeViewModel.TYPE_VIDEO -> {
                        _videoExtResult.postValue(ext)
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(realPath)
                        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                    }
                }

                val file = File(realPath)

                if (type == HomeViewModel.TYPE_PIC && file.length() >= 5242880) {
                    val tempFile = FileUtil.getTakePhoto( "temp.jpg")
                    file.copyTo(tempFile, true)
                    val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                    FileUtil.saveBitmapToJpegFile(bitmap, bitmap.width, bitmap.height, destPath = tempFile.absolutePath)

                    realPath = tempFile.absolutePath
                }

                Timber.d("Upload photo path : $realPath")
                Timber.d("Upload photo ext : $ext")

                val result = if (mime == null) { //Post pic type
                    domainManager.getApiRepository().postAttachment(
                        File(realPath!!),
                        fileName = URLEncoder.encode(fileName, "UTF-8")
                    )
                } else { //Post video type
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

    fun postPicClub(postClubItem: PostClubItem) {
        viewModelScope.launch(context = job) {
            flow {
                val request = PostMemberRequest(postClubItem.title, postClubItem.request, PostType.IMAGE.value, tags = postClubItem.tags)

                Timber.d("Post member request : $request")

                if (postClubItem.memberPostItem == null) {
                    val resp = domainManager.getApiRepository().postMembersPost(request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(resp.body()?.content))
                } else {
                    val resp = domainManager.getApiRepository().updatePost(postClubItem.memberPostItem.id, request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(postClubItem.memberPostItem.id))
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _postPicMemberResult.value = it
                }
        }
    }

    fun postVideoClub(postClubItem: PostClubItem) {
        viewModelScope.launch(context = job) {
            flow {
                val request = PostMemberRequest(postClubItem.title, postClubItem.request, PostType.VIDEO.value, tags = postClubItem.tags)

                Timber.d("Post member request : $request")

                if (postClubItem.memberPostItem == null) {
                    val resp = domainManager.getApiRepository().postMembersPost(request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(resp.body()?.content))
                } else {
                    val resp = domainManager.getApiRepository().updatePost(postClubItem.memberPostItem.id, request)
                    if (!resp.isSuccessful) throw HttpException(resp)
                    emit(ApiResult.success(postClubItem.memberPostItem.id))
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _postVideoMemberResult.value = it
                }
        }
    }

    fun postPicOrVideo(id: Long = 0, request: PostMemberRequest, content: String, type: String) {
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
                .collect {
                    if (type == HomeViewModel.TYPE_VIDEO) {
                        _postVideoMemberResult.value = it
                    } else {
                        _postPicMemberResult.value = it
                    }
                }
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

    fun setAdUrlToServer(target: String) {
        viewModelScope.launch(context = Dispatchers.IO) {
            try {
                val url = URL(target)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.doInput = true
                val status = connection.responseCode
                Timber.i("Send AD url to server status: $status")
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun clearLiveDataValue() {
        _postArticleResult.value = null
        _postPicResult.value = null
        _postCoverResult.value = null
        _postVideoResult.value = null
        _picExtResult.value = null
        _coverExtResult.value = null
        _postVideoMemberResult.value = null
        _postPicMemberResult.value = null
        _videoExtResult.value = null
    }

    fun clearPicResultValue() {
        _postPicResult.value = null
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

    fun deleteCacheFile(cacheFile: File) {
        viewModelScope.launch {
            cacheFile.listFiles()?.forEach { file ->
                file?.delete()
            }
        }
    }

    fun checkIsLogin() = accountManager.isLogin()

    fun clearOrderItem() {
        _orderItem.postValue(null)
    }

    fun setNavTransparent(isNavTransparent: Boolean = false) {
        _isNavTransparent.value = isNavTransparent
    }

    fun setStatusBarMode(isDark: Boolean = false) {
        _isStatusBardDark.value = isDark
    }

    fun setIsShowSnackBar(isShow: Boolean) {
        _isShowSnackBar.value = isShow
    }

    fun deleteClear(pageCode:String){
        Timber.i("deleteClear $pageCode")
        viewModelScope.launch {
            mimiDB.remoteKeyDao().deleteByPageCode(pageCode)
            mimiDB.postDBItemDao().deleteItemByPageCode(pageCode)
        }
    }

}