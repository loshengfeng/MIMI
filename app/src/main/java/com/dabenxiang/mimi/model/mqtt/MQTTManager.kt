package com.dabenxiang.mimi.model.mqtt

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import timber.log.Timber

class MQTTManager(val context: Context) {

    lateinit var client: MqttAndroidClient
    lateinit var options: MqttConnectOptions

    fun init(serverUrl: String, clientId: String) {
        client = MqttAndroidClient(context, serverUrl, clientId)
        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Timber.d("Connect: $serverURI")
                if (reconnect) {
                    // TODO:
                }
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                Timber.d("Incoming topic:: $topic")
                Timber.d("Incoming message:: ${String(message.payload)}")
            }

            override fun connectionLost(cause: Throwable) {
                Timber.e("The Connection was lost: $cause")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Timber.d("deliveryComplete message:: ${String(token.message.payload)}")
            }
        })

        options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = false
    }

    fun connect() {
        client.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Timber.d("Connection onSuccess")
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                client.setBufferOpts(disconnectedBufferOptions)

//                subscribeToTopic("topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Timber.e("Connection onFailure: $exception")
            }
        })
    }

    fun subscribeToTopic(subscriptionTopic: String) {
        client.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Timber.d("Subscribed")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Timber.d("Failed to subscribe")
            }
        })

        client.subscribe(subscriptionTopic, 0) { topic, message ->
            Timber.d("Message: $topic , : ${String(message.payload)}")
        }
    }

    fun publishMessage(publishTopic: String, publishMessage: String) {
        val message = MqttMessage()
        message.payload = publishMessage.toByteArray()
        client.publish(publishTopic, message)
        Timber.d("Message Published")

        if (!client.isConnected) {
            Timber.d("${client.bufferedMessageCount} messages in buffer.")
        }
    }
}