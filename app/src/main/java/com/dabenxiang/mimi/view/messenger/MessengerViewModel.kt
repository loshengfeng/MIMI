package com.dabenxiang.mimi.view.messenger

import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.model.manager.mqtt.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.view.base.BaseViewModel
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.koin.core.inject
import timber.log.Timber

class MessengerViewModel : BaseViewModel() {

    private val mqttManager: MQTTManager by inject()

    fun connect() {
        val serverUrl = "tcp://" + BuildConfig.LOCAL_SOCKET_HOST + ":1883"
        val clientId = "123456"

        mqttManager.init(serverUrl, clientId, object : ExtendedCallback {
            override fun onConnectComplete(reconnect: Boolean, serverURI: String) {
                Timber.d("Connect: $serverURI")
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

        mqttManager.connect(object : ConnectCallback {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Timber.d("Connection onSuccess")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Timber.e("Connection onFailure: $exception")
            }
        })
    }

    fun publishMsg() {
        mqttManager.publishMessage("s/t/1", "Good")
    }
}