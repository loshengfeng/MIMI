package com.dabenxiang.mimi.model.manager.mqtt

import android.content.Context
import com.dabenxiang.mimi.model.manager.mqtt.callback.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.SubscribeCallback
import com.dabenxiang.mimi.model.pref.Pref
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import timber.log.Timber

class MQTTManager(val context: Context, private val pref: Pref) {

    companion object {
        const val PREFIX_CHAT = "/chat/"
        const val PREFIX_NOTIFICATION = "/notification/"
    }

    private var client: MqttAndroidClient? = null
    private var options: MqttConnectOptions? = null

    fun init(serverUrl: String, clientId: String, extendedCallback: ExtendedCallback) {
        client = MqttAndroidClient(context, serverUrl, clientId)
        client?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                extendedCallback.onConnectComplete(reconnect, serverURI)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                if(!message.isDuplicate) {
                    extendedCallback.onMessageArrived(topic, message)
                }
            }

            override fun connectionLost(cause: Throwable) {
                extendedCallback.onConnectionLost(cause)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                extendedCallback.onDeliveryComplete(token)
            }
        })

        options = MqttConnectOptions()
        options?.password = pref.memberToken.accessToken.toCharArray()
        options?.userName = pref.profileItem.userId.toString()
        options?.isAutomaticReconnect = true
        options?.isCleanSession = false

    }

    fun connect(connectCallback: ConnectCallback) {
        client?.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                client?.setBufferOpts(disconnectedBufferOptions)

                connectCallback.onSuccess(asyncActionToken)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                connectCallback.onFailure(asyncActionToken, exception)
            }
        })
    }

    fun disconnect() {
        if (client != null) {
            client?.disconnect()
        }
    }

    fun subscribeToTopic(subscriptionTopic: String, subscribeCallback: SubscribeCallback) {
        client?.subscribe(subscriptionTopic, 1, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                subscribeCallback.onSuccess(asyncActionToken)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                subscribeCallback.onFailure(asyncActionToken, exception)
            }
        })
    }


    private val defaultMqttCallback by lazy {
        object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {}
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {}
        }
    }

    fun publishMessage(publishTopic: String, publishMessage: String, callback: IMqttActionListener = defaultMqttCallback) {
        val message = MqttMessage()
        message.payload = publishMessage.toByteArray()
        client?.publish(publishTopic, message, null, callback)
        Timber.d("Message Published")

        if (!client!!.isConnected) {
            Timber.d("${client?.bufferedMessageCount} messages in buffer.")
        }
    }
}