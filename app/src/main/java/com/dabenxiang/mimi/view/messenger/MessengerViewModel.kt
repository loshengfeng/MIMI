package com.dabenxiang.mimi.view.messenger

import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.model.manager.mqtt.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.manager.mqtt.SubscribeCallback
import com.dabenxiang.mimi.view.base.BaseViewModel
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class MessengerViewModel : BaseViewModel() {

    companion object {
        const val PREFIX_CHAT = "/chat/"
    }

    private val mqttManager: MQTTManager by inject()
    private val serverUrl = BuildConfig.MQTT_HOST
    private val clientId = UUID.randomUUID().toString()

    fun init() {
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
                subscribe(PREFIX_CHAT + "3777788128132071424")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Timber.e("Connection onFailure: $exception")
            }
        })
    }

    fun subscribe(topic: String) {
        mqttManager.subscribeToTopic(topic, object : SubscribeCallback{
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

    fun publishMsg(topic: String, message: String) {
        mqttManager.publishMessage(topic, message)
    }
}