package com.dabenxiang.mimi.view.messenger

import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.model.mqtt.MQTTManager
import com.dabenxiang.mimi.view.base.BaseViewModel
import org.koin.core.inject

class MessengerViewModel : BaseViewModel() {

    private val mqttManager: MQTTManager by inject()

    fun connect() {
        val serverUrl = "tcp://" + BuildConfig.LOCAL_SOCKET_HOST + ":1883"
        val clientId = "123456"
        mqttManager.init(serverUrl, clientId)
        mqttManager.connect()
    }

    fun publishMsg() {
        mqttManager.publishMessage("s/t/1", "Good")
    }
}